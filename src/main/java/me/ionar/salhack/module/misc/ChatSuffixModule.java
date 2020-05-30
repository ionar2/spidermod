package me.ionar.salhack.module.misc;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class ChatSuffixModule extends Module
{
    public ChatSuffixModule()
    {
        super("ChatSuffix", new String[]
        { "ChatSuffix", "ChatSuffix" }, "Adds a ChatSuffix onto the end of every message.", "NONE", 0xDB2450, ModuleType.MISC);
    }


        private final String SALHACK_SUFFIX = " \u23D0 \tꜱᴀʟʜᴀᴄᴋ";

        @EventHandler
        public Listener<EventNetworkPacketEvent.Send> listener = new Listener<>(event -> {
            if (event.getPacket() instanceof CPacketChatMessage) {
                String s = ((CPacketChatMessage) event.getPacket()).getMessage();
                if (s.startsWith("/") && !commands.getValue()) return;
                s += SALHACK_SUFFIX;
                if (s.length() >= 256) s = s.substring(0,256);
                ((CPacketChatMessage) event.getPacket()).message = s;
            }
        });

    }

}
