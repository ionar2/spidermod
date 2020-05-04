package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public final class NoRotateModule extends Module
{
    public NoRotateModule()
    {
        super("NoRotate", new String[]
        { "NoRot", "AntiRotate" }, "Prevents you from processing server rotations", "NONE", 0x24B2DB, ModuleType.MOVEMENT);
    }
    
    @Override
    public String getMetaData()
    {
        return "Packet";
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketPlayerPosLook)
        {
            if (mc.player != null)
            {
                final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) p_Event.getPacket();
                packet.yaw = mc.player.rotationYaw;
                packet.pitch = mc.player.rotationPitch;
            }
        }
    });

}
