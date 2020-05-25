package me.ionar.salhack.main;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;

public class AlwaysEnabledModule implements Listenable
{
    public AlwaysEnabledModule()
    {
    }
    
    public void init()
    {
        SalHackMod.EVENT_BUS.subscribe(this);
    }
    
    public static String LastIP = null;
    public static int LastPort = -1;
    
    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketChat)
        {
            final SPacketChat packet = (SPacketChat) p_Event.getPacket();

            if (packet.getChatComponent() instanceof TextComponentString)
            {
                final TextComponentString component = (TextComponentString) packet.getChatComponent();

                if (component.getFormattedText().toLowerCase().contains("polymer") || component.getFormattedText().toLowerCase().contains("veteranhack"))
                    p_Event.cancel();
            }
        }
        else if (p_Event.getPacket() instanceof C00Handshake)
        {
            final C00Handshake packet = (C00Handshake) p_Event.getPacket();
            if (packet.getRequestedState() == EnumConnectionState.LOGIN)
            {
                LastIP = packet.ip;
                LastPort = packet.port;
            }
        }
    });
}
