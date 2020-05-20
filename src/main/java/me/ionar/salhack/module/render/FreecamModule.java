package me.ionar.salhack.module.render;

import me.ionar.salhack.events.blocks.EventSetOpaqueCube;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMove;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class FreecamModule extends Module
{   
    public final Value<Float> speed = new Value<Float>("Speed", new String[]
    { "Spd" }, "Speed of freecam flight, higher number equals quicker motion.", 1.0f, 0.0f, 10.0f, 0.1f);
    public final Value<Boolean> CancelPackes = new Value<Boolean>("Cancel Packets", new String[] {""}, "Cancels the packets, you won't be able to freely move without this.", true);
    
    public FreecamModule()
    {
        super("Freecam", new String[] {"OutOfBody"}, "Allows out of body movement", "NONE", -1, ModuleType.RENDER);
    }

    private Entity riding;
    private EntityOtherPlayerMP Camera;
    private Vec3d position;
    private float yaw;
    private float pitch;

    @Override
    public void toggleNoSave()
    {

    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.world == null)
            return;
        
        riding = null;
        
        if (mc.player.getRidingEntity() != null)
        {
            this.riding = mc.player.getRidingEntity();
            mc.player.dismountRidingEntity();
        }
        
        Camera = new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile());
        Camera.copyLocationAndAnglesFrom(mc.player);
        Camera.prevRotationYaw = mc.player.rotationYaw;
        Camera.rotationYawHead = mc.player.rotationYawHead;
        Camera.inventory.copyInventory(mc.player.inventory);
        mc.world.addEntityToWorld(-69, Camera);

        this.position = mc.player.getPositionVector();
        this.yaw = mc.player.rotationYaw;
        this.pitch = mc.player.rotationPitch;
        mc.player.noClip = true;
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        
        if (mc.world != null)
        {
            if (this.riding != null)
            {
                mc.player.startRiding(this.riding, true);
                riding = null;
            }
            if (this.Camera != null)
            {
                mc.world.removeEntity(this.Camera);
            }
            if (this.position != null)
            {
                mc.player.setPosition(this.position.x, this.position.y, this.position.z);
            }
            mc.player.rotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.noClip = false;
            mc.player.setVelocity(0, 0, 0);
        }
    }

    @EventHandler
    private Listener<EventPlayerMove> OnPlayerMove = new Listener<>(p_Event ->
    {
        mc.player.noClip = true;
    });

    @EventHandler
    private Listener<EventSetOpaqueCube> OnEventSetOpaqueCube  = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        mc.player.noClip = true;

        mc.player.setVelocity(0, 0, 0);

        final double[] dir = MathUtil.directionSpeed(this.speed.getValue());

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
        {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];
        }
        else
        {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        mc.player.setSprinting(false);

        if (mc.gameSettings.keyBindJump.isKeyDown())
        {
            mc.player.motionY += this.speed.getValue();
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown())
        {
            mc.player.motionY -= this.speed.getValue();
        }
    });
    
    @EventHandler
    private Listener<EntityJoinWorldEvent> OnWorldEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getEntity() == mc.player)
        {
            toggle();
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (!CancelPackes.getValue())
            return;
        
        if ((p_Event.getPacket() instanceof CPacketUseEntity)
                || (p_Event.getPacket() instanceof CPacketPlayerTryUseItem)
                || (p_Event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock)
                || (p_Event.getPacket() instanceof CPacketPlayer)
                || (p_Event.getPacket() instanceof CPacketVehicleMove)
                || (p_Event.getPacket() instanceof CPacketChatMessage))
        {
            p_Event.cancel();
        }
    });
}
