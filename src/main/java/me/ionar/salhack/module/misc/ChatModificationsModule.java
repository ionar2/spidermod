package me.ionar.salhack.module.misc;

import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

public final class ChatModificationsModule extends Module
{

    public final Value<TimeModes> TimeMode = new Value<TimeModes>("TimeMode", new String[]
    { "TimeModes", "Time" }, "Time format, 12 hour (NA) or 24 hour (EU).", TimeModes.NA);
    public final Value<Boolean> AntiEZ = new Value<Boolean>("AntiEZ", new String[] {"NoEZ"}, "Prevents EZ from being rendered in chat, very useful for 2b2tpvp", true);
    public final Value<Boolean> NoDiscord = new Value<Boolean>("NoDiscord", new String[] {"NoEZ"}, "Prevents discord from being rendered in chat", true);

    private enum TimeModes
    {
        NA, EU
    }

    public ChatModificationsModule()
    {
        super("ChatModifications", new String[]
        { "ChatStamp", "ChatStamps" }, "Allows for chat modifications", "NONE", 0xDB2450, ModuleType.MISC);
    }

    @Override
    public String getMetaData()
    {
        return this.TimeMode.getValue().toString();
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

                String date = "";

                switch (this.TimeMode.getValue())
                {
                    case NA:
                        date = new SimpleDateFormat("h:mm a").format(new Date());
                        break;
                    case EU:
                        date = new SimpleDateFormat("k:mm").format(new Date());
                        break;
                }

                component.text = "\2477[" + date + "]\247r " + component.getText();
                
                if (component.getFormattedText().contains("> "))
                {
                    String l_Text = component.getFormattedText().substring(component.getFormattedText().indexOf("> "));
                    
                    if (l_Text.toLowerCase().contains("ez") && AntiEZ.getValue())
                        p_Event.cancel();
                    
                    if (NoDiscord.getValue() && l_Text.toLowerCase().contains("discord"))
                        p_Event.cancel();
                }
            }
        }
    });

}
