package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;

public final class BuildHeightModule extends Module
{
    public BuildHeightModule()
    {
        super("BuildHeight", new String[]
        { "BuildH", "BHeight" }, "Allows you to interact with blocks at build height", "NONE", 0xDB246D, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock)
        {
            final CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) p_Event.getPacket();
            if (packet.getPos().getY() >= 255 && packet.getDirection() == EnumFacing.UP)
            {
                packet.placedBlockDirection = EnumFacing.DOWN;
            }
        }
    });

}
