package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketCloseWindow;

public final class XCarryModule extends Module
{
    public XCarryModule()
    {
        super("XCarry", new String[]
        { "XCarry", "MoreInventory" }, "Allows you to carry items in your crafting and dragging slot", "NONE", 0x24ADDB, ModuleType.MISC);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (mc.world != null)
        {
            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
        }
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof CPacketCloseWindow)
        {
            final CPacketCloseWindow packet = (CPacketCloseWindow) p_Event.getPacket();
            if (packet.windowId == mc.player.inventoryContainer.windowId)
            {
                p_Event.cancel();
            }
        }
    });
}
