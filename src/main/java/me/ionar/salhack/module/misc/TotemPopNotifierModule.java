package me.ionar.salhack.module.misc;

import java.util.HashMap;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;

public class TotemPopNotifierModule extends Module
{
    private HashMap<String, Integer> TotemPopContainer = new HashMap<String, Integer>();
    
    public TotemPopNotifierModule()
    {
        super("TotemPopNotifier", new String[] {"TPN"}, "Notifys when someone pops a totem!", "NONE", 0x2482DB, ModuleType.MISC);
    }
    
    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketEntityStatus)
        {
            SPacketEntityStatus l_Packet = (SPacketEntityStatus)p_Event.getPacket();
            
            if (l_Packet.getOpCode() == 35) ///< Opcode check the packet 35 is totem, thxmojang
            {
                Entity l_Entity = l_Packet.getEntity(mc.world);
                
                if (l_Entity == null)
                    return;
                
                int l_Count = 1;
                
                if (TotemPopContainer.containsKey(l_Entity.getName()))
                {
                    l_Count = TotemPopContainer.get(l_Entity.getName()).intValue();
                    TotemPopContainer.put(l_Entity.getName(), ++l_Count);
                }
                else
                {
                    TotemPopContainer.put(l_Entity.getName(), l_Count);
                }
                
                NotificationManager.Get().AddNotification("TotemPop", l_Entity.getName() + " popped " + l_Count + " totem(s)!");
                SendMessage(l_Entity.getName() + " popped " + l_Count + " totem(s)!");
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        for (EntityPlayer l_Player : mc.world.playerEntities)
        {
            if (!TotemPopContainer.containsKey(l_Player.getName()))
                continue;
            
            if (l_Player.isDead || l_Player.getHealth() <= 0.0f)
            {
                int l_Count = TotemPopContainer.get(l_Player.getName()).intValue();
                
                TotemPopContainer.remove(l_Player.getName());

                NotificationManager.Get().AddNotification("TotemPop", l_Player.getName() + " died after popping " + l_Count + " totem(s)!");
                SendMessage(l_Player.getName() + " died after popping " + l_Count + " totem(s)!");
            }
        }
    });
}