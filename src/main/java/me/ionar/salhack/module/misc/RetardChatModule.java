package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerSendChatMessage;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;

public final class RetardChatModule extends Module
{
    public final Value<Modes> mode = new Value<Modes>("Mode", new String[] {"M"}, "The retard chat mode", Modes.Spongebob);
    
    public enum Modes
    {
        Spongebob,
    }
    
    public RetardChatModule()
    {
        super("RetardChat", new String[]
        { "Retard" }, "Makes your chat retarded", "NONE",
                0xDB2485, ModuleType.MISC);
    }

    @Override
    public String getMetaData()
    {
        return mode.getValue().toString();
    }

    @EventHandler
    private Listener<EventPlayerSendChatMessage> OnSendChatMsg = new Listener<>(p_Event ->
    {
        if (p_Event.Message.startsWith("/"))
            return;

        String l_Message = "";
        
        switch (mode.getValue())
        {
            case Spongebob:
            {
                boolean l_Flag = false;
                
                for (char l_Char : p_Event.Message.toCharArray())
                {
                    String l_Val = String.valueOf(l_Char);
                    
                    l_Message += l_Flag ? l_Val.toUpperCase() : l_Val.toLowerCase();
                    
                    if (l_Char != ' ')
                        l_Flag = !l_Flag;
                }
                break;
            }
        }
        
        p_Event.cancel();
        mc.getConnection().sendPacket(new CPacketChatMessage(l_Message));
    });
}
