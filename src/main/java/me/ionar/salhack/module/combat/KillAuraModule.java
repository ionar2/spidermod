package me.ionar.salhack.module.combat;

import java.util.Comparator;

import javax.annotation.Nullable;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.RotationSpoof;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

public class KillAuraModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"Mode"}, "The KillAura Mode to use", Modes.Closest);
    public final Value<Float> Distance = new Value<Float>("Distance", new String[] {"Range"}, "Range for attacking a target", 5.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> HitDelay = new Value<Boolean>("Hit Delay", new String[] {"Hit Delay"}, "Use vanilla hit delay", true);
    public final Value<Boolean> TPSSync = new Value<Boolean>("TPSSync", new String[] {"TPSSync"}, "Use TPS Sync for hit delay", false);
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] {"Players"}, "Should we target Players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[] {"Players"}, "Should we target Monsters", true);
    public final Value<Boolean> Neutrals = new Value<Boolean>("Neutrals", new String[] {"Players"}, "Should we target Neutrals", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] {"Players"}, "Should we target Animals", false);
    public final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[] {"Players"}, "Should we target Tamed", false);
    public final Value<Boolean> Projectiles = new Value<Boolean>("Projectile", new String[] {"Projectile"}, "Should we target Projectiles (shulker bullets, etc)", false);
    public final Value<Boolean> SwordOnly = new Value<Boolean>("SwordOnly", new String[] {"SwordOnly"}, "Only activate on sword", false);
    public final Value<Integer> Ticks = new Value<Integer>("Ticks", new String[] {"Ticks"}, "If you don't have HitDelay on, how fast the kill aura should be hitting", 10, 0, 40, 1);
    
    public enum Modes
    {
        Closest,
        Priority,
        Switch,
    }

    public KillAuraModule()
    {
        super("KillAura", new String[] {"Aura"}, "Automatically faces and hits entities around you", "NONE", 0xFF0000, ModuleType.COMBAT);
    }

    private Entity CurrentTarget;
    private AutoCrystalModule AutoCrystal;
    private AimbotModule Aimbot;
    private Timer AimbotResetTimer = new Timer();
    private int RemainingTicks = 0;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        RemainingTicks = 0;
        
        if (AutoCrystal == null)
        {
            AutoCrystal = (AutoCrystalModule) ModuleManager.Get().GetMod(AutoCrystalModule.class);
        }
        if (Aimbot == null)
        {
            Aimbot = (AimbotModule) ModuleManager.Get().GetMod(AimbotModule.class);
            
            if (!Aimbot.isEnabled())
                Aimbot.toggle();
        }
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        
        if (Aimbot != null)
            Aimbot.m_RotationSpoof = null;
    }
    
    @Override
    public String getMetaData()
    {
        return Mode.getValue().toString();
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    private boolean IsValidTarget(Entity p_Entity, @Nullable Entity p_ToIgnore)
    {
        if (!(p_Entity instanceof EntityLivingBase))
        {
            boolean l_IsProjectile = (p_Entity instanceof EntityShulkerBullet || p_Entity instanceof EntityFireball);
            
            if (!l_IsProjectile)
                return false;
            
            if (l_IsProjectile && !Projectiles.getValue())
                return false;
        }
        
        if (p_ToIgnore != null && p_Entity == p_ToIgnore)
            return false;
        
        if (p_Entity instanceof EntityPlayer)
        {
            /// Ignore if it's us
            if (p_Entity == mc.player)
                return false;
            
            if (!Players.getValue())
                return false;
            
            /// They are a friend, ignore it.
            if (FriendManager.Get().IsFriend(p_Entity))
                return false;
        }
        
        if (EntityUtil.isHostileMob(p_Entity) && !Monsters.getValue())
            return false;
        
        if (EntityUtil.isPassive(p_Entity))
        {
            if (p_Entity instanceof AbstractChestHorse)
            {
                AbstractChestHorse l_Horse = (AbstractChestHorse)p_Entity;
                
                if (l_Horse.isTame() && !Tamed.getValue())
                    return false;
            }
            
            if (!Animals.getValue())
                return false;
        }
        
        if (EntityUtil.isHostileMob(p_Entity) && !Monsters.getValue())
            return false;
        
        if (EntityUtil.isNeutralMob(p_Entity) && !Neutrals.getValue())
            return false;
        
        boolean l_HealthCheck = true;
        
        if (p_Entity instanceof EntityLivingBase)
        {
            EntityLivingBase l_Base = (EntityLivingBase)p_Entity;
            
            l_HealthCheck = !l_Base.isDead && l_Base.getHealth() > 0.0f;
        }
        
        return l_HealthCheck && p_Entity.getDistance(p_Entity) <= Distance.getValue();
    }

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event ->
    {
        if (SwordOnly.getValue() && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword))
            return;
        
        if (AimbotResetTimer.passed(5000))
        {
            AimbotResetTimer.reset();
            Aimbot.m_RotationSpoof = null;
        }
        
        if (RemainingTicks > 0)
        {
            --RemainingTicks;
        }
        
        /// Chose target based on current mode
        Entity l_TargetToHit = CurrentTarget;
        
        switch (Mode.getValue())
        {
            case Closest:
                l_TargetToHit = mc.world.loadedEntityList.stream()
                        .filter(p_Entity -> IsValidTarget(p_Entity, null))
                        .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                        .orElse(null);
                break;
            case Priority:
                if (l_TargetToHit == null)
                {
                    l_TargetToHit = mc.world.loadedEntityList.stream()
                        .filter(p_Entity -> IsValidTarget(p_Entity, null))
                        .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                        .orElse(null);
                }
                break;
            case Switch:
                l_TargetToHit = mc.world.loadedEntityList.stream()
                    .filter(p_Entity -> IsValidTarget(p_Entity, null))
                    .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                    .orElse(null);
                
                if (l_TargetToHit == null)
                    l_TargetToHit = CurrentTarget;
                
                break;
            default:
                break;
            
        }
        
        /// nothing to hit - return until next tick for searching
        if (l_TargetToHit == null || l_TargetToHit.getDistance(mc.player) > Distance.getValue())
        {
            CurrentTarget = null;
            return;
        }
        
        float[] l_Rotation = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), l_TargetToHit.getPositionEyes(mc.getRenderPartialTicks()));
        Aimbot.m_RotationSpoof = new RotationSpoof(l_Rotation[0], l_Rotation[1]);

        final float l_Ticks = 20.0f - TickRateManager.Get().getTickRate();

        final boolean l_IsAttackReady = this.HitDelay.getValue() ? (mc.player.getCooledAttackStrength(TPSSync.getValue() ? -l_Ticks : 0.0f) >= 1) : true;
        
        if (!l_IsAttackReady)
            return;

        if (!HitDelay.getValue() && RemainingTicks > 0)
            return;
        
        RemainingTicks = Ticks.getValue();
        
      //  mc.playerController.attackEntity(mc.player, l_TargetToHit);
        mc.player.connection.sendPacket(new CPacketUseEntity(l_TargetToHit));
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.resetCooldown();
    });
}