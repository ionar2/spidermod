package me.ionar.salhack.module.render;

import java.util.Iterator;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerIsPotionActive;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.EventRenderArmorLayer;
import me.ionar.salhack.events.render.EventRenderBossHealth;
import me.ionar.salhack.events.render.EventRenderEntity;
import me.ionar.salhack.events.render.EventRenderHurtCameraEffect;
import me.ionar.salhack.events.render.EventRenderMap;
import me.ionar.salhack.events.render.EventRenderSign;
import me.ionar.salhack.events.render.EventRenderUpdateLightmap;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;

public class NoRenderModule extends Module
{
    public final Value<NoItemsMode> NoItems = new Value<NoItemsMode>("NoItemsMode", new String[] {"NoItems"}, "Prevents items from being rendered", NoItemsMode.Off);
    public final Value<Boolean> Fire = new Value<Boolean>("Fire", new String[] {"Fire"}, "Doesn't render Fire overlay", true);
    public final Value<Boolean> NoHurtCam = new Value<Boolean>("HurtCamera", new String[] {"NHC"}, "Doesn't render the Hurt camera", true);
    public final Value<Boolean> PumpkinOverlay = new Value<Boolean>("PumpkinOverlay", new String[] {"PO"}, "Doesn't render the pumpkin overlay", false);
    public final Value<Boolean> Blindness = new Value<Boolean>("Blindness", new String[] {"Blindness"}, "Doesn't render the blindness effect", true);
    public final Value<Boolean> TotemAnimation = new Value<Boolean>("TotemAnimation", new String[] {"TotemAnimation"}, "Doesn't render the totem animation", false);
    public final Value<Boolean> Skylight = new Value<Boolean>("Skylight", new String[] {"Skylight"}, "Doesn't render skylight updates", false);
    public final Value<Boolean> SignText = new Value<Boolean>("SignText", new String[] {"SignText"}, "Doesn't render SignText", false);
    public final Value<Boolean> NoArmor = new Value<Boolean>("NoArmor", new String[] {"NoArmor"}, "Doesn't render armor", false);
    public final Value<Boolean> NoArmorPlayers = new Value<Boolean>("NoArmorPlayers", new String[] {"NoArmorPlayers"}, "Use in conjunction with the above mod", false);
    public final Value<Boolean> Maps = new Value<Boolean>("Maps", new String[] {"Maps"}, "Doesn't render maps", false);
    public final Value<Boolean> BossHealth = new Value<Boolean>("BossHealth", new String[] {"WitherNames"}, "Doesn't render wither names, and other boss health", false);
    
    public enum NoItemsMode
    {
        Off,
        Remove,
        Hide,
    }
    
    public NoRenderModule()
    {
        super("NoRender", new String[] {"NR"}, "Doesn't render certain things, if enabled", "NONE", -1, ModuleType.RENDER);
    }
    
    private Timer timer = new Timer();
    
    @EventHandler
    private Listener<EventRenderEntity> OnRenderEntity = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof EntityItem && NoItems.getValue() == NoItemsMode.Hide)
            event.cancel();
    });
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        switch (NoItems.getValue())
        {
            case Remove:
                if (!timer.passed(5000))
                    return;
                
                timer.reset();
                
                Iterator<Entity> itr = mc.world.loadedEntityList.iterator();
                
                while (itr.hasNext())
                {
                    Entity entity = itr.next();
                    
                    if (entity != null)
                    {
                        if (entity instanceof EntityItem)
                            mc.world.removeEntity(entity);
                    }
                }
                break;
            default:
                break;
        }
    });

    @EventHandler
    private Listener<EventRenderHurtCameraEffect> OnHurtCameraEffect = new Listener<>(p_Event ->
    {
        if (NoHurtCam.getValue())
            p_Event.cancel();
    });
    
    @EventHandler
    private Listener<RenderBlockOverlayEvent> OnBlockOverlayEvent = new Listener<>(p_Event ->
    {
        if (Fire.getValue() && p_Event.getOverlayType() == OverlayType.FIRE)
            p_Event.setCanceled(true);
        if (PumpkinOverlay.getValue() && p_Event.getOverlayType() == OverlayType.BLOCK)
            p_Event.setCanceled(true);
    });

    @EventHandler
    private Listener<EventPlayerIsPotionActive> IsPotionActive = new Listener<>(p_Event ->
    {
        if (p_Event.potion == MobEffects.BLINDNESS && Blindness.getValue())
            p_Event.cancel();
    });
    
    /*@EventHandler
    private Listener<EventParticleEmitParticleAtEntity> OnEmitParticleAtEntity = new Listener<>(p_Event ->
    {
        if (p_Event.Type == EnumParticleTypes.TOTEM && TotemAnimation.getValue())
            p_Event.cancel();
    });*/

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (mc.world == null || mc.player == null)
            return;
        
        if (p_Event.getPacket() instanceof SPacketEntityStatus)
        {
            SPacketEntityStatus l_Packet = (SPacketEntityStatus)p_Event.getPacket();
            
            if (l_Packet.getOpCode() == 35)
            {
                if (TotemAnimation.getValue())
                    p_Event.cancel();
            }
        }
    });

    @EventHandler
    private Listener<EventRenderUpdateLightmap> OnSkylightUpdate = new Listener<>(p_Event ->
    {
        if (Skylight.getValue())
            p_Event.cancel();
    });

    @EventHandler
    private Listener<EventRenderSign> OnRenderSign = new Listener<>(p_Event ->
    {
        if (SignText.getValue())
            p_Event.cancel();
    });

    @EventHandler
    private Listener<EventRenderArmorLayer> OnRenderArmorLayer = new Listener<>(p_Event ->
    {
        if (NoArmor.getValue())
        {
            if (!(p_Event.Entity instanceof EntityPlayer) && NoArmorPlayers.getValue())
                return;
            
            p_Event.cancel();
        }
    });

    @EventHandler
    private Listener<EventRenderMap> OnRenderMap = new Listener<>(p_Event ->
    {
        if (Maps.getValue())
            p_Event.cancel();
    });
    
    @EventHandler
    private Listener<EventRenderBossHealth> OnRenderBossHealth = new Listener<>(p_Event ->
    {
       if (BossHealth.getValue())
           p_Event.cancel();
    });

}
