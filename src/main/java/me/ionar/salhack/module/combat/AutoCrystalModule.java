package me.ionar.salhack.module.combat;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.misc.AutoMendArmorModule;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.CrystalUtils;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.RotationSpoof;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class AutoCrystalModule extends Module
{
    /// Values must be static
    public static final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "Mode of updating to use", Modes.ClientTick);
    public static final Value<PlaceModes> PlaceMode = new Value<PlaceModes>("PlaceMode", new String[] {""}, "Automatically place mode", PlaceModes.Nearest);
    public static final Value<DestroyModes> DestroyMode = new Value<DestroyModes>("DestroyMode", new String[] {""}, "Automatically Destroy mode", DestroyModes.Smart);
    
    public static final Value<Integer> Ticks = new Value<Integer>("Ticks" , new String[] {"Ticks"} , "Ticks", 1, 0, 10, 1);

    /// Range
    public static final Value<Float> DestroyDistance = new Value<Float>("DestroyDistance", new String[] {""}, "Destrou crystal range", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> PlaceDistance = new Value<Float>("PlaceDistance", new String[] {""}, "Place range", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> WallsRange = new Value<Float>("WallsRange", new String[] {""}, "Max distance through walls", 3.5f, 0.0f, 5.0f, 0.5f);
    
    /// Damage
    public static final Value<Float> MinDMG = new Value<Float>("MinDMG", new String[] {""}, "Minimum dmg for placing crystals near target", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> MaxSelfDMG = new Value<Float>("MaxSelfDMG", new String[] {""}, "Max self dmg for breaking crystals that will deal tons of dmg", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> FacePlace = new Value<Float>("FacePlace", new String[] {""}, "Required target health for faceplacing", 8.0f, 0.0f, 20.0f, 0.5f);
    
    public static final Value<Boolean> NoSuicide = new Value<Boolean>("NoSuicide", new String[] {"NS"}, "Doesn't commit suicide/pop if you are going to take fatal damage from self placed crystal", true);
    public static final Value<Boolean> PauseWhileEating = new Value<Boolean>("PauseWhileEating", new String[] {"PauseWhileEating"}, "Pause while eating", true);
    public static final Value<Boolean> AntiWeakness = new Value<Boolean>("AntiWeakness", new String[] {"AntiWeakness"}, "Uses a tool or sword to hit the crystals", true);
    public static final Value<Boolean> GhostHand = new Value<Boolean>("GhostHand", new String[] {"GhostHand"}, "Allows you to place crystals by spoofing item packets", false);
    public static final Value<Boolean> GhostHandWeakness = new Value<Boolean>("GhostHandWeakness", new String[] {"GhostHandWeakness"}, "Breaks crystals with sword with ghosthand", false);
    public static final Value<Boolean> ChatMsgs = new Value<Boolean>("ChatMsgs", new String[] {"ChatMsgs"}, "Displays ChatMsgs", false);
    
    /// Targets
    public static final Value<Boolean> Players = new Value<Boolean>("Players", new String[] {"Players"}, "Place on players", true);
    public static final Value<Boolean> Hostile = new Value<Boolean>("Hostile", new String[] {"Hostile"}, "Place on Hostile", false);
    public static final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] {"Animals"}, "Place on Animals", false);
    public static final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[] {"Tamed"}, "Place on Tamed", false);
    public static final Value<Boolean> ResetRotationNoTarget = new Value<Boolean>("ResetRotationNoTarget", new String[] {"ResetRotationNoTarget"}, "ResetRotationNoTarget", false);
    
    /// More options
    public static final Value<Boolean> Multiplace = new Value<Boolean>("Multiplace", new String[] {"Multiplace"}, "Multiplace", true);
    public static final Value<Boolean> OnlyPlaceWithCrystal = new Value<Boolean>("OnlyPlaceWithCrystal ", new String[] {"OPWC"}, "Only places when you're manually using a crystal in your main or offhand", false);
    public static final Value<Boolean> PlaceObsidianIfNoValidSpots = new Value<Boolean>("PlaceObsidianIfNoValidSpots ", new String[] {"POINVS"}, "Automatically places obsidian if there are no available crystal spots, so you can crystal your opponent", false);
    public static final Value<Boolean> MinHealthPause = new Value<Boolean>("MinHealthPause", new String[] {"MHP"}, "Automatically pauses if you are below RequiredHealth", false);
    public static final Value<Float> RequiredHealth = new Value<Float>("RequiredHealth", new String[] {""}, "RequiredHealth for autocrystal to function, must be above or equal to this amount.", 11.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Boolean> AutoMultiplace = new Value<Boolean>("AutoMultiplace", new String[] {""}, "Automatically enables/disables multiplace", false);
    public static final Value<Float> HealthBelowAutoMultiplace = new Value<Float>("HealthBelowAutoMultiplace", new String[] {""}, "RequiredHealth for target to be for automatic multiplace toggling.", 11.0f, 0.0f, 20.0f, 1.0f);
    
    
    public static final Value<Boolean> Render = new Value<Boolean>("Render", new String[] {"Render"}, "Allows for rendering of block placements", true);
    public static final Value<Integer> Red = new Value<Integer>("Red", new String[] {"Red"}, "Red for rendering", 0x33, 0, 255, 5);
    public static final Value<Integer> Green = new Value<Integer>("Green", new String[] {"Green"}, "Green for rendering", 0xFF, 0, 255, 5);
    public static final Value<Integer> Blue = new Value<Integer>("Blue", new String[] {"Blue"}, "Blue for rendering", 0xF3, 0, 255, 5);
    public static final Value<Integer> Alpha = new Value<Integer>("Alpha", new String[] {"Alpha"}, "Alpha for rendering", 0x99, 0, 255, 5);
    
    public AutoCrystalModule()
    {
        super("AutoCrystal", new String[] {"AC"}, "Automatically places and destroys crystals around targets, if they meet the requirements", "NONE", 0x24CADB, ModuleType.COMBAT);
    }
    
    /// Variables
    private int m_WaitTicks = 0;
    private ArrayList<CPacketPlayer.PositionRotation> Packets = new ArrayList<CPacketPlayer.PositionRotation>();
    private int m_SpoofTimerResetTicks = 0;
    private AimbotModule Aimbot;
    private ICamera camera = new Frustum();
    private EntityLivingBase m_Target = null;
    private ArrayList<BlockPos> PlacedCrystals = new ArrayList<BlockPos>();
    private SurroundModule Surround = null;
    private AutoTrapFeet AutoTrapFeet = null;
    private AutoMendArmorModule AutoMend = null;
    private SelfTrapModule SelfTrap = null;
    
    @Override
    public void SendMessage(String p_Msg)
    {
        if (ChatMsgs.getValue())
            super.SendMessage(p_Msg);
    }
    
    /// Overrides
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        Packets.clear();
        
        Aimbot = (AimbotModule)ModuleManager.Get().GetMod(AimbotModule.class);
        Surround = (SurroundModule)ModuleManager.Get().GetMod(SurroundModule.class);
        AutoTrapFeet = (AutoTrapFeet)ModuleManager.Get().GetMod(AutoTrapFeet.class);
        AutoMend = (AutoMendArmorModule)ModuleManager.Get().GetMod(AutoMendArmorModule.class);
        SelfTrap = (SelfTrapModule)ModuleManager.Get().GetMod(SelfTrapModule.class);
        
      //  if (!Holes.isEnabled())
       //     Holes.toggle();
        
        if (!Aimbot.isEnabled())
            Aimbot.toggle();
        
        Aimbot.m_RotationSpoof = null;
        m_WaitTicks = Ticks.getValue();
        
        PlacedCrystals.clear();
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        
        Packets.clear();
        Aimbot.m_RotationSpoof = null;
        m_WaitTicks = Ticks.getValue();
        
        PlacedCrystals.clear();
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public String getMetaData()
    {
        String l_Result = m_Target != null ? m_Target.getName() : null;
        
        if (AutoMultiplace.getValue() && Multiplace.getValue() && l_Result != null)
            l_Result += " Multiplacing";
        
        return l_Result;
    }

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event ->
    {
        if (Mode.getValue() != Modes.ClientTick)
            return;
        
        if (PauseWhileEating.getValue() && PlayerUtil.IsEating())
        {
            /// Reset ticks
            m_WaitTicks = 0;
            return;
        }
        
        if (NeedPause())
        {
            /// Reset ticks
            m_WaitTicks = 0;
            /// Reset rotation
            Aimbot.m_RotationSpoof = null;
            return;
        }
        
        EntityEnderCrystal l_Crystal = mc.world.loadedEntityList.stream()
                .filter(p_Entity -> IsValidCrystal(p_Entity))
                .map(p_Entity -> (EntityEnderCrystal) p_Entity)
                .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                .orElse(null);
       
        int l_WaitValue = Ticks.getValue();
        
        if (m_WaitTicks < l_WaitValue)
        {
            ++m_WaitTicks;
            return;
        }
        
        m_WaitTicks = 0;
        
        if (m_SpoofTimerResetTicks > 0)
        {
            --m_SpoofTimerResetTicks;
        }
        
        if (ResetRotationNoTarget.getValue())
        {
            if (m_Target == null && Aimbot.m_RotationSpoof != null)
                Aimbot.m_RotationSpoof = null;
        }
        else
        {
            if (m_SpoofTimerResetTicks == 0)
            {
                m_SpoofTimerResetTicks = 200;
                
                Aimbot.m_RotationSpoof = null;
            }
        }
        
        /*if (m_HoleToFill != null)
        {
            HoleFillerModule.FillHole(m_HoleToFill);
            m_HoleToFill = null;
            return;
        }*/
        
        if (Multiplace.getValue())
        {
            if (DestroyMode.getValue() != DestroyModes.None)
            {
                HandleBreakCrystals(l_Crystal, null);
    //            if (HandleBreakCrystals(l_Crystal))
      //              return;
            }
            
            if (PlaceMode.getValue() != PlaceModes.None)
                HandlePlaceCrystal(null);
        }
        else
        {
            if (!HandleBreakCrystals(l_Crystal, null))
                HandlePlaceCrystal(null);
        }
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerMotionUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;

        if (Mode.getValue() != Modes.MotionTick)
            return;
        
        if (PauseWhileEating.getValue() && PlayerUtil.IsEating())
        {
            /// Reset ticks
            m_WaitTicks = 0;
            return;
        }
        
        if (NeedPause())
        {
            /// Reset ticks
            m_WaitTicks = 0;
            /// Reset rotation
            Aimbot.m_RotationSpoof = null;
            return;
        }
        
        EntityEnderCrystal l_Crystal = mc.world.loadedEntityList.stream()
                .filter(p_Entity -> IsValidCrystal(p_Entity))
                .map(p_Entity -> (EntityEnderCrystal) p_Entity)
                .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                .orElse(null);

        int l_WaitValue = Ticks.getValue();
        
        if (m_WaitTicks < l_WaitValue)
        {
            ++m_WaitTicks;
            return;
        }
        
        if (Multiplace.getValue())
        {
            boolean l_Result = false;
            
            if (DestroyMode.getValue() != DestroyModes.None)
                l_Result = !HandleBreakCrystals(l_Crystal, p_Event);
            
            if (PlaceMode.getValue() != PlaceModes.None)
            {
                final BlockPos l_Pos = HandlePlaceCrystal(p_Event);
                
                if (!l_Result && l_Pos != BlockPos.ORIGIN)
                    l_Result = true;
            }
            
            if (l_Result)
                m_WaitTicks = Ticks.getValue();
        }
        else
        {
            if (!HandleBreakCrystals(l_Crystal, p_Event))
            {
                final BlockPos l_Pos = HandlePlaceCrystal(p_Event);
                
                if (l_Pos != BlockPos.ORIGIN)
                    m_WaitTicks = Ticks.getValue();
            }
        }
    });
    
    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketSoundEffect)
        {
            SPacketSoundEffect l_Packet = (SPacketSoundEffect) p_Event.getPacket();

            if (l_Packet.getCategory() == SoundCategory.BLOCKS
                    && l_Packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE)
            {
                for (Entity l_Entity : new ArrayList<Entity>(mc.world.loadedEntityList))
                {
                    if (l_Entity instanceof EntityEnderCrystal)
                    {
                        if (l_Entity.getDistance(l_Packet.getX(), l_Packet.getY(), l_Packet.getZ()) <= 6.0)
                        {
                            l_Entity.setDead();
                        }

                        
                        PlacedCrystals.removeIf(p_Pos -> p_Pos.getDistance((int)l_Packet.getX(), (int)l_Packet.getY(), (int)l_Packet.getZ()) <= 6.0);
                        /*Iterator<BlockPos> l_Itr = PlacedCrystals.iterator(); 
                        while (l_Itr.hasNext()) 
                        { 
                            BlockPos l_Pos = (BlockPos)l_Itr.next(); 
                            if (l_Pos.getDistance((int)l_Packet.getX(), (int)l_Packet.getY(), (int)l_Packet.getZ()) <= 6.0)
                                l_Itr.remove(); 
                        }*/
                    }
                }
            }
        }
    });

    private boolean IsValidCrystal(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntityEnderCrystal))
            return false;
        
        if (p_Entity.getDistance(mc.player) > (!mc.player.canEntityBeSeen(p_Entity) ? WallsRange.getValue() : DestroyDistance.getValue()))
            return false;
        
        switch (DestroyMode.getValue())
        {
            case Always:
                return true;
            case OnlyOwn:
                /// create copy
                for (BlockPos l_Pos : new ArrayList<BlockPos>(PlacedCrystals))
                {
                    if (l_Pos != null && l_Pos.getDistance((int)p_Entity.posX, (int)p_Entity.posY, (int)p_Entity.posZ) <= 3.0)
                        return true;
                }
                break;
            case Smart:
                EntityLivingBase l_Target = GetNearTarget(p_Entity);
                
                if (l_Target == null)
                    return false;
                
                float l_TargetDMG = CrystalUtils.calculateDamage(mc.world, p_Entity.posX + 0.5, p_Entity.posY + 1.0, p_Entity.posZ + 0.5, l_Target, 0);
                float l_SelfDMG = CrystalUtils.calculateDamage(mc.world, p_Entity.posX + 0.5, p_Entity.posY + 1.0, p_Entity.posZ + 0.5, mc.player, 0);
                
                if (l_TargetDMG > 0 && l_SelfDMG < MaxSelfDMG.getValue())
                    return true;
                
                break;
            default:
                break;
        }
        
        return false;
    }
    
    private boolean HandleBreakCrystals(EntityEnderCrystal p_Crystal, @Nullable EventPlayerMotionUpdate p_Event)
    {
        if (p_Crystal != null)
        {
            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    p_Crystal.posX + 0.5,
                    p_Crystal.posY - 0.5,
                    p_Crystal.posZ + 0.5,
                    mc.player);

            if (Mode.getValue() == Modes.ClientTick)
            {
                Aimbot.m_RotationSpoof = new RotationSpoof((float)l_Pos[0], (float)l_Pos[1]);
                
                Random rand = new Random(2);
                
                Aimbot.m_RotationSpoof.Yaw += (rand.nextFloat() / 100);
                Aimbot.m_RotationSpoof.Pitch += (rand.nextFloat() / 100);
            }
            
            int l_PrevSlot = -1;
            
            if (AntiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS))
            {
                if (mc.player.getHeldItemMainhand() == ItemStack.EMPTY || (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)))
                {
                    for (int l_I = 0; l_I < 9; ++l_I)
                    {
                        ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                        
                        if (l_Stack == ItemStack.EMPTY)
                            continue;
                        
                        if (l_Stack.getItem() instanceof ItemTool || l_Stack.getItem() instanceof ItemSword)
                        {
                            l_PrevSlot = mc.player.inventory.currentItem;
                            mc.player.inventory.currentItem = l_I;
                            mc.playerController.updateController();
                            break;
                        }
                    }
                }
            }

            if (Mode.getValue() == Modes.MotionTick && p_Event != null) ///< p_Event should not null
            {
                p_Event.cancel();
                
                SpoofRotationsTo((float)l_Pos[0], (float)l_Pos[1]);
            }
            
            mc.playerController.attackEntity(mc.player, p_Crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            
            if (GhostHandWeakness.getValue() && l_PrevSlot != -1)
            {
                mc.player.inventory.currentItem = l_PrevSlot;
                mc.playerController.updateController();
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean IsValidTarget(Entity p_Entity)
    {
        if (p_Entity == null)
            return false;
        
        if (!(p_Entity instanceof EntityLivingBase))
            return false;

        if (FriendManager.Get().IsFriend(p_Entity))
            return false;
        
        if (p_Entity.isDead || ((EntityLivingBase)p_Entity).getHealth() <= 0.0f)
            return false;
        
        if (p_Entity.getDistance(mc.player) > 20.0f)
            return false;
        
        if (p_Entity instanceof EntityPlayer && Players.getValue())
        {
            if (p_Entity == mc.player)
                return false;
            
            return true;
        }
        
        if (Hostile.getValue() && EntityUtil.isHostileMob(p_Entity))
            return true;
        if (Animals.getValue() && EntityUtil.isPassive(p_Entity))
            return true;
        if (Tamed.getValue() && p_Entity instanceof AbstractChestHorse && ((AbstractChestHorse)p_Entity).isTame())
            return true;
        
        return false;
    }
    
    private EntityLivingBase GetNearTarget(Entity p_DistanceTarget)
    {
        return mc.world.loadedEntityList.stream()
                .filter(p_Entity -> IsValidTarget(p_Entity))
                .map(p_Entity -> (EntityLivingBase) p_Entity)
                .min(Comparator.comparing(p_Entity -> p_DistanceTarget.getDistance(p_Entity)))
                .orElse(null);
    }
    
    private void FindNewTarget()
    {
        m_Target = GetNearTarget(mc.player);
    }
    
    private BlockPos HandlePlaceCrystal(@Nullable EventPlayerMotionUpdate p_Event)
    {
        if (OnlyPlaceWithCrystal.getValue())
        {
            /// if we don't have crystal in main or offhand, don't place, this was a request from issue #25
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                return BlockPos.ORIGIN;
        }
        
        List<BlockPos> l_AvailableBlockPositions = CrystalUtils.findCrystalBlocks(mc.player, PlaceDistance.getValue());

        
        switch (PlaceMode.getValue())
        {
            case Nearest:
                FindNewTarget();
                break;
            case Priority:
                if (m_Target == null || m_Target.getDistance(mc.player) > PlaceDistance.getValue() + 2f || m_Target.isDead || m_Target.getHealth() <= 0.0f) ///< Allow 2 tolerence
                    FindNewTarget();
                break;
            case MostDamage:
            {
                if (l_AvailableBlockPositions.isEmpty())
                {
                    FindNewTarget();
                }
                else
                {
                    EntityLivingBase l_Target = null;
                    
                    float l_MinDmg = MinDMG.getValue();
                    float l_MaxSelfDmg = MaxSelfDMG.getValue();
                    float l_DMG = 0.0f;
                    
                    /// Iterate through all players
                    for (EntityPlayer l_Player : mc.world.playerEntities)
                    {
                        if (!IsValidTarget(l_Player))
                            continue;
                        
                        /// Iterate block positions for this entity
                        for (BlockPos l_Pos : l_AvailableBlockPositions)
                        {
                            if (l_Player.getDistanceSq(l_Pos) >= 169.0D)
                                continue; 
                            
                            float l_TempDMG = CrystalUtils.calculateDamage(mc.world, l_Pos.getX() + 0.5, l_Pos.getY() + 1.0, l_Pos.getZ() + 0.5, l_Player, 0);
                            
                            if (l_TempDMG < l_MinDmg)
                                continue;
                            
                            float l_SelfTempDMG = CrystalUtils.calculateDamage(mc.world, l_Pos.getX() + 0.5, l_Pos.getY() + 1.0, l_Pos.getZ() + 0.5, mc.player, 0);
                            
                            if (l_SelfTempDMG > l_MaxSelfDmg)
                                continue;
                            
                            if (WallsRange.getValue() > 0)
                            {
                                if (!PlayerUtil.CanSeeBlock(l_Pos))
                                    if (l_Pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ) > WallsRange.getValue())
                                        continue;
                            }
                            
                            if (l_TempDMG > l_DMG)
                            {
                                l_DMG = l_TempDMG;
                                l_Target = l_Player;
                            }
                        }
                    }
                    
                    if (l_Target == null)
                        l_Target = GetNearTarget(mc.player);
                    
                    if (m_Target != null && l_Target != m_Target && l_Target != null)
                    {
                        SendMessage(String.format("Found new target %s", l_Target.getName()));
                    }
                    
                    m_Target = l_Target;
                }
                break;
            }
            default:
                break;
        }
        
        if (l_AvailableBlockPositions.isEmpty())
        {
            if (PlaceObsidianIfNoValidSpots.getValue() && m_Target != null)
            {
                int l_Slot = AutoTrapFeet.findStackHotbar(Blocks.OBSIDIAN);
                
                if (l_Slot != -1)
                {
                    if (mc.player.inventory.currentItem != l_Slot)
                    {
                        mc.player.inventory.currentItem = l_Slot;
                        mc.playerController.updateController();
                        return BlockPos.ORIGIN;
                    }
                    
                    float l_Range = PlaceDistance.getValue();
                    
                    float l_TargetDMG = 0.0f;
                    float l_MinDmg = MinDMG.getValue();
                    
                    /// FacePlace
                    if (m_Target.getHealth()+m_Target.getAbsorptionAmount() <= FacePlace.getValue())
                        l_MinDmg = 1f;
                    
                    BlockPos l_TargetPos = null;

                    for (BlockPos l_Pos : BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), PlaceDistance.getValue(), (int)l_Range, false, true, 0))
                    {
                        ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
                        
                        if (l_Result != ValidResult.Ok)
                            continue;
                        
                        if (!CrystalUtils.CanPlaceCrystalIfObbyWasAtPos(l_Pos))
                            continue;
                        
                        float l_TempDMG = CrystalUtils.calculateDamage(mc.world, l_Pos.getX() + 0.5, l_Pos.getY() + 1.0, l_Pos.getZ() + 0.5, m_Target, 0);
                        
                        if (l_TempDMG < l_MinDmg)
                            continue;
                        
                        if (l_TempDMG >= l_TargetDMG)
                        {
                            l_TargetPos = l_Pos;
                            l_TargetDMG = l_TempDMG;
                        }
                    }
                    
                    if (l_TargetPos != null)
                    {
                        BlockInteractionHelper.place(l_TargetPos, PlaceDistance.getValue(), true, false); ///< sends a new packet, might be bad for ncp flagging tomany packets..
                        SendMessage(String.format("Tried to place obsidian at %s would deal %s dmg", l_TargetPos.toString(), l_TargetDMG));
                    }
                }
            }
            
            return BlockPos.ORIGIN;
        }

        
        if (m_Target == null)
            return BlockPos.ORIGIN;
        
        if (AutoMultiplace.getValue())
        {
            if (m_Target.getHealth()+m_Target.getAbsorptionAmount() <= HealthBelowAutoMultiplace.getValue())
                Multiplace.setValue(true);
            else
                Multiplace.setValue(false);
        }
        
        float l_MinDmg = MinDMG.getValue();
        float l_MaxSelfDmg = MaxSelfDMG.getValue();
        float l_FacePlaceHealth = FacePlace.getValue();
        
        /// FacePlace
        if (m_Target.getHealth() <= l_FacePlaceHealth)
            l_MinDmg = 1f;
        
        /// AntiSuicide
        if (NoSuicide.getValue())
        {
            while (mc.player.getHealth()+mc.player.getAbsorptionAmount() < l_MaxSelfDmg)
                l_MaxSelfDmg /= 2;
        }
        
        BlockPos l_BestPosition = null;
        float l_DMG = 0.0f;
        
        /// todo: use this, but we will lose dmg... maybe new option, for LeastDMGToSelf? but seems useless
        float l_SelfDMG = 0.0f;
        
        for (BlockPos l_Pos : l_AvailableBlockPositions)
        {
            if (m_Target.getDistanceSq(l_Pos) >= 169.0D)
                continue; 
            
            float l_TempDMG = CrystalUtils.calculateDamage(mc.world, l_Pos.getX() + 0.5, l_Pos.getY() + 1.0, l_Pos.getZ() + 0.5, m_Target, 0);
            
            if (l_TempDMG < l_MinDmg)
                continue;
            
            float l_SelfTempDMG = CrystalUtils.calculateDamage(mc.world, l_Pos.getX() + 0.5, l_Pos.getY() + 1.0, l_Pos.getZ() + 0.5, mc.player, 0);
            
            if (l_SelfTempDMG > l_MaxSelfDmg)
                continue;
            
            if (WallsRange.getValue() > 0)
            {
                if (!PlayerUtil.CanSeeBlock(l_Pos))
                    if (l_Pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ) > WallsRange.getValue())
                        continue;
            }
            
            if (l_TempDMG > l_DMG)
            {
                l_DMG = l_TempDMG;
                l_SelfDMG = l_SelfTempDMG;
                l_BestPosition = l_Pos;
            }
        }
        
        if (l_BestPosition == null)
            return BlockPos.ORIGIN;
        
        /*for (Hole l_Hole : Holes.GetHoles())
        {
            float l_HoleFillDmg = CrystalUtils.calculateDamage(mc.world, l_Hole.getX() + 0.5, l_Hole.getY() + 1.0, l_Hole.getZ() + 0.5, l_Player, 0);
            
            if (l_HoleFillDmg > l_DMG)
            {
                m_HoleToFill = l_Hole;
                l_DMG = l_HoleFillDmg;
            }
        }
        
        if (m_HoleToFill != null)
        {
            SalHack.INSTANCE.logChat("Filling the hole at " + m_HoleToFill.toString() + " will deal " + l_DMG);
           // return;
        }*/
        
        int l_PrevSlot = -1;
        
        
        if (!GhostHand.getValue())
        {
            if (SwitchHandToItemIfNeed(Items.END_CRYSTAL))
                return BlockPos.ORIGIN;
        }
        else
        {
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
            {
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                    
                    if (l_Stack == ItemStack.EMPTY)
                        continue;
                    
                    if (l_Stack.getItem() == Items.END_CRYSTAL)
                    {
                        l_PrevSlot = mc.player.inventory.currentItem;
                        mc.player.inventory.currentItem = l_I;
                        mc.playerController.updateController();
                    }
                }
            }
        }

        final double l_Pos[] =  EntityUtil.calculateLookAt(
                l_BestPosition.getX() + 0.5,
                l_BestPosition.getY() - 0.5,
                l_BestPosition.getZ() + 0.5,
                mc.player);
        
        if (Mode.getValue() == Modes.ClientTick)
        {
            Aimbot.m_RotationSpoof = new RotationSpoof((float)l_Pos[0], (float)l_Pos[1]);
    
            Random rand = new Random(2);
            
            Aimbot.m_RotationSpoof.Yaw += (rand.nextFloat() / 100);
            Aimbot.m_RotationSpoof.Pitch += (rand.nextFloat() / 100);
        }
        
        RayTraceResult l_Result = mc.world.rayTraceBlocks(
                new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                new Vec3d(l_BestPosition.getX() + 0.5, l_BestPosition.getY() - 0.5,
                        l_BestPosition.getZ() + 0.5));

        EnumFacing l_Facing;

        if (l_Result == null || l_Result.sideHit == null)
            l_Facing = EnumFacing.UP;
        else
            l_Facing = l_Result.sideHit;
        
        if (Mode.getValue() == Modes.MotionTick && p_Event != null) ///< p_Event should not null
        {
            p_Event.cancel();
            
            SpoofRotationsTo((float)l_Pos[0], (float)l_Pos[1]);
        }
        
        mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(l_BestPosition, l_Facing,
                mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
       // mc.playerController.processRightClickBlock(mc.player, mc.world, l_BestPosition, EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
       // SalHack.INSTANCE.logChat(String.format("%s%s DMG and SelfDMG %s %s %S", ChatFormatting.LIGHT_PURPLE, l_DMG, l_SelfDMG, l_Facing, m_Target.getName()));
        
        PlacedCrystals.add(l_BestPosition);
        
        if (l_PrevSlot != -1 && GhostHand.getValue())
        {
            mc.player.inventory.currentItem = l_PrevSlot;
            mc.playerController.updateController();
        }
        
        return l_BestPosition;
    }
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;
        
        ArrayList<BlockPos> l_PlacedCrystalsCopy = new ArrayList<BlockPos>(PlacedCrystals);
        
        for (BlockPos l_Pos : l_PlacedCrystalsCopy) 
        {
            if (l_Pos == null)
                continue;
            
            final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() - mc.getRenderManager().viewerPosY, l_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                    l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    l_Pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    
            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);
    
            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ)))
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);
    
                int l_Color = (Alpha.getValue() << 24) | (Red.getValue() << 16) | (Green.getValue() << 8) | Blue.getValue(); 
                
                RenderUtil.drawBoundingBox(bb, 1.0f, l_Color);
                RenderUtil.drawFilledBox(bb, l_Color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });
    
    private boolean SwitchHandToItemIfNeed(Item p_Item)
    {
        if (mc.player.getHeldItemMainhand().getItem() == p_Item || mc.player.getHeldItemOffhand().getItem() == p_Item)
            return false;
        
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack == ItemStack.EMPTY)
                continue;
            
            if (l_Stack.getItem() == p_Item)
            {
                mc.player.inventory.currentItem = l_I;
                mc.playerController.updateController();
                return true;
            }
        }
        
        return true;
    }
    
    private void SpoofRotationsTo(float p_Yaw, float p_Pitch)
    {
        boolean l_IsSprinting = mc.player.isSprinting();

        if (l_IsSprinting != mc.player.serverSprintState)
        {
            if (l_IsSprinting)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = l_IsSprinting;
        }

        boolean l_IsSneaking = mc.player.isSneaking();

        if (l_IsSneaking != mc.player.serverSneakState)
        {
            if (l_IsSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = l_IsSneaking;
        }

        if (PlayerUtil.isCurrentViewEntity())
        {
            float l_Pitch = p_Pitch;
            float l_Yaw = p_Yaw;
            
            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double l_PosXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double l_PosYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double l_PosZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double l_YawDifference = (double)(l_Yaw - mc.player.lastReportedYaw);
            double l_RotationDifference = (double)(l_Pitch - mc.player.lastReportedPitch);
            ++mc.player.positionUpdateTicks;
            boolean l_MovedXYZ = l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean l_MovedRotation = l_YawDifference != 0.0D || l_RotationDifference != 0.0D;

            if (mc.player.isRiding())
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, l_Yaw, l_Pitch, mc.player.onGround));
                l_MovedXYZ = false;
            }
            else if (l_MovedXYZ && l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (l_MovedXYZ)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            }
            else if (l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (mc.player.prevOnGround != mc.player.onGround)
            {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (l_MovedXYZ)
            {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (l_MovedRotation)
            {
                mc.player.lastReportedYaw = l_Yaw;
                mc.player.lastReportedPitch = l_Pitch;
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
    }
    
    private enum Modes
    {
        ClientTick,
        MotionTick,
    }
    
    private enum DestroyModes
    {
        None,
        Smart,
        Always,
        OnlyOwn,
    }
    
    private enum PlaceModes
    {
        None,
        Nearest,
        Priority,
        MostDamage,
    }
    
    public boolean NeedPause()
    {
        /// We need to pause if we have surround enabled, and don't have obsidian
        if (Surround.isEnabled() && !Surround.IsSurrounded(mc.player) && Surround.HasObsidian())
            return true;
        
        if (AutoTrapFeet.isEnabled() && !AutoTrapFeet.IsCurrentTargetTrapped() && AutoTrapFeet.HasObsidian())
            return true;
        
        if (AutoMend.isEnabled())
            return true;
        
        if (SelfTrap.isEnabled() && !SelfTrap.IsSelfTrapped() && Surround.HasObsidian())
            return true;
        
        if (MinHealthPause.getValue() && (mc.player.getHealth()+mc.player.getAbsorptionAmount()) < RequiredHealth.getValue())
            return true;
        
        return false;
    }
}
