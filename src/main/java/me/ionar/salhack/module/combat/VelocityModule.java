package me.ionar.salhack.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerApplyCollision;
import me.ionar.salhack.events.player.EventPlayerPushOutOfBlocks;
import me.ionar.salhack.events.player.EventPlayerPushedByWater;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public final class VelocityModule extends Module
{

    public final Value<Integer> horizontal_vel = new Value<Integer>("Horizontal", new String[]
    { "Horizontal_Velocity", "HVel", "HV", "HorizontalVel", "Horizontal", "H" }, "The horizontal velocity you will take.", 0, 0, 100, 1);
    public final Value<Integer> vertical_vel = new Value<Integer>("Veritcal", new String[]
    { "Vertical_Velocity", "VVel", "VV", "VerticalVel", "Vertical", "Vert", "V" }, "The vertical velocity you will take.", 0, 0, 100, 1);
    public final Value<Boolean> explosions = new Value<Boolean>("Explosions", new String[]
    { "Explosions", "Explosion", "EXP", "EX", "Expl" }, "Apply velocity modifier on explosion velocity.", true);
    public final Value<Boolean> bobbers = new Value<Boolean>("Bobbers", new String[]
    { "Bobb", "Bob", "FishHook", "FishHooks" }, "Apply velocity modifier on fishing bobber velocity.", true);
    public final Value<Boolean> NoPush = new Value<Boolean>("NoPush", new String[]
    { "AntiPush" }, "Disable collision with entities, blocks and water", true);

    public VelocityModule()
    {
        super("Velocity", new String[]
        { "Vel", "AntiVelocity", "Knockback", "AntiKnockback" }, "Modify the velocity you take", "NONE", 0x9B24DB, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData()
    {
        return String.format("H:%s%% V:%s%%", this.horizontal_vel.getValue(), this.vertical_vel.getValue());
    }

    @EventHandler
    private Listener<EventPlayerPushOutOfBlocks> PushOutOfBlocks = new Listener<>(p_Event ->
    {
        if (!NoPush.getValue())
            return;

        p_Event.cancel();
    });
    
    @EventHandler
    private Listener<EventPlayerPushedByWater> PushByWater = new Listener<>(p_Event ->
    {
        if (!NoPush.getValue())
            return;

        p_Event.cancel();
    });

    @EventHandler
    private Listener<EventPlayerApplyCollision> ApplyCollision = new Listener<>(p_Event ->
    {
        if (!NoPush.getValue())
            return;

        p_Event.cancel();
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (mc.player == null)
            return;
        
        if (p_Event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue())
        {
            final SPacketEntityStatus packet = (SPacketEntityStatus) p_Event.getPacket();
            if (packet.getOpCode() == 31)
            {
                final Entity entity = packet.getEntity(Minecraft.getMinecraft().world);
                if (entity != null && entity instanceof EntityFishHook)
                {
                    final EntityFishHook fishHook = (EntityFishHook) entity;
                    if (fishHook.caughtEntity == Minecraft.getMinecraft().player)
                    {
                        p_Event.cancel();
                    }
                }
            }
        }
        if (p_Event.getPacket() instanceof SPacketEntityVelocity)
        {
            final SPacketEntityVelocity packet = (SPacketEntityVelocity) p_Event.getPacket();
            if (packet.getEntityID() == mc.player.getEntityId())
            {
                if (this.horizontal_vel.getValue() == 0 && this.vertical_vel.getValue() == 0)
                {
                    p_Event.cancel();
                    return;
                }

                if (this.horizontal_vel.getValue() != 100)
                {
                    packet.motionX = packet.motionX / 100 * this.horizontal_vel.getValue();
                    packet.motionZ = packet.motionZ / 100 * this.horizontal_vel.getValue();
                }

                if (this.vertical_vel.getValue() != 100)
                {
                    packet.motionY = packet.motionY / 100 * this.vertical_vel.getValue();
                }
            }
        }
        if (p_Event.getPacket() instanceof SPacketExplosion && this.explosions.getValue())
        {
            final SPacketExplosion packet = (SPacketExplosion) p_Event.getPacket();

            if (this.horizontal_vel.getValue() == 0 && this.vertical_vel.getValue() == 0)
            {
                p_Event.cancel();
                return;
            }

            if (this.horizontal_vel.getValue() != 100)
            {
                packet.motionX = packet.motionX / 100 * this.horizontal_vel.getValue();
                packet.motionZ = packet.motionZ / 100 * this.horizontal_vel.getValue();
            }

            if (this.vertical_vel.getValue() != 100)
            {
                packet.motionY = packet.motionY / 100 * this.vertical_vel.getValue();
            }
        }
    });

}
