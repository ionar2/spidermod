package me.ionar.salhack.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;

public class AlwaysEnabledModule implements Listenable
{
    public AlwaysEnabledModule()
    {
        SalHackMod.EVENT_BUS.subscribe(this);
    }
    
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
    });
}
