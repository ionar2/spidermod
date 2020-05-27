package me.ionar.salhack.main;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerJoin;
import me.ionar.salhack.events.player.EventPlayerLeave;
import me.ionar.salhack.managers.UUIDManager;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketPlayerListItem;
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
        else if (p_Event.getPacket() instanceof SPacketPlayerListItem)
        {
            final SPacketPlayerListItem packet = (SPacketPlayerListItem) p_Event.getPacket();
            final Minecraft mc = Wrapper.GetMC();
            if (mc.player != null && mc.player.ticksExisted >= 1000)
            {
                if (packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER)
                {
                    packet.getEntries().forEach(playerData ->
                    {
                        if (playerData.getProfile().getId() != mc.session.getProfile().getId())
                        {
                            new Thread(() ->
                            {
                                final String name = UUIDManager.Get().resolveName(playerData.getProfile().getId().toString());
                                if (name != null)
                                    SalHackMod.EVENT_BUS.post(new EventPlayerJoin(name, playerData.getProfile().getId().toString()));
                            }).start();
                        }
                    });
                }
                if (packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER)
                {
                    packet.getEntries().forEach(playerData ->
                    {
                        if (playerData.getProfile().getId() != mc.session.getProfile().getId())
                        {
                            new Thread(() ->
                            {
                                final String name = UUIDManager.Get().resolveName(playerData.getProfile().getId().toString());
                                if (name != null)
                                    SalHackMod.EVENT_BUS.post(new EventPlayerLeave(name, playerData.getProfile().getId().toString()));
                            }).start();
                        }
                    });
                }
            }
        }
    });
}
