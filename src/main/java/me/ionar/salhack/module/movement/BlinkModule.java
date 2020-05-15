package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;

import java.util.LinkedList;

public class BlinkModule extends Module
{
    public final Value<Boolean> Visualize = new Value<Boolean>("Visualize", new String[] {"FakePlayer"}, "Visualizes your body while blink is enabled", true);
    public final Value<Boolean> EntityBlink = new Value<Boolean>("EntityBlink", new String[] {"Vehicles"}, "Holds the CPacketVehicleMove", true);

    public BlinkModule()
    {
        super("Blink", new String[]
                { "FakeLag" }, "Holds move packets until disabled", "NONE", -1, ModuleType.MOVEMENT);
    }

    private EntityOtherPlayerMP Original;
    private EntityDonkey RidingEntity;
    private LinkedList<Packet> Packets = new LinkedList<Packet>();

    @Override
    public void onEnable()
    {
        super.onEnable();

        Packets.clear();
        Original = null;
        RidingEntity = null;

        if (Visualize.getValue())
        {
            Original = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            Original.copyLocationAndAnglesFrom(mc.player);
            Original.rotationYaw = mc.player.rotationYaw;
            Original.rotationYawHead = mc.player.rotationYawHead;
            Original.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(-0xFFFFF, Original);

            if (mc.player.isRiding() && mc.player.getRidingEntity() instanceof EntityDonkey)
            {
                EntityDonkey l_Original = (EntityDonkey)mc.player.getRidingEntity();

                RidingEntity = new EntityDonkey(mc.world);
                RidingEntity.copyLocationAndAnglesFrom(l_Original);
                RidingEntity.setChested(l_Original.hasChest());
                mc.world.addEntityToWorld(-0xFFFFF+1, RidingEntity);

                Original.startRiding(RidingEntity, true);
            }
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (!Packets.isEmpty() && mc.world != null)
        {
            while (!Packets.isEmpty())
            {
                mc.getConnection().sendPacket(Packets.getFirst()); ///< front
                Packets.removeFirst(); ///< pop
            }
        }

        if (Original != null)
        {
            if (Original.isRiding())
                Original.dismountRidingEntity();

            mc.world.removeEntity(Original);
        }

        if (RidingEntity != null)
            mc.world.removeEntity(RidingEntity);
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof CPacketPlayer || p_Event.getPacket() instanceof CPacketConfirmTeleport || (EntityBlink.getValue() && p_Event.getPacket() instanceof CPacketVehicleMove))
        {
            p_Event.cancel();
            Packets.add(p_Event.getPacket());
        }
    });
}
