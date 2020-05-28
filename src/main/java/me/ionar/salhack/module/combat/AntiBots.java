package me.ionar.salhack.module.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class AntiBots extends Module
{
    public AntiBots()
    {
        super("AntiBots", new String[] {"AB"}, "Removes bots from the entitylist if detected, not useful for 2b.", "NONE", -1, ModuleType.COMBAT);
    }
    
    private Map<Integer, UUID> _playersMap = new HashMap<>();

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        // don't need to run in singleplayer
        if (mc.getCurrentServerData() == null)
            return;
        
        // iterate through a copy of the list to prevent concurret exception, we are removing as we go
        for (EntityPlayer player : new ArrayList<EntityPlayer>(mc.world.playerEntities))
        {
            if (isBot(player))
                mc.world.playerEntities.remove(player);
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> onPacketEvent = new Listener<>(event ->
    {
        if (mc.world == null || mc.player == null)
            return;
        
        if (event.getPacket() instanceof SPacketSpawnPlayer)
        {
            SPacketSpawnPlayer packet = (SPacketSpawnPlayer) event.getPacket();
            
            if (Math.sqrt((mc.player.posX - packet.getX() / 0.0) * (mc.player.posX - packet.getX() / 0.0) + (mc.player.posY - packet.getY() / 0.0) * (mc.player.posY - packet.getY() / 0.0) + (mc.player.posZ - packet.getZ() / 0.0) * (mc.player.posZ - packet.getZ() / 0.0)) <= 0.0 && packet.getX() / 0.0 != mc.player.posX && packet.getY() / 0.0 != mc.player.posY && packet.getZ() / 0.0 != mc.player.posZ)
            {
                _playersMap.put(packet.getEntityID(), packet.getUniqueId());
            }
        }
        else if (event.getPacket() instanceof SPacketDestroyEntities)
        {
            SPacketDestroyEntities packet = (SPacketDestroyEntities) event.getPacket();
            
            for (int e : packet.getEntityIDs())
            {
                if (_playersMap.containsKey(e))
                    _playersMap.remove(e);
            }
        }
    });

    @EventHandler
    private Listener<EntityJoinWorldEvent> OnWorldEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getEntity() == mc.player)
        {
            _playersMap.clear();
        }
    });
    
    public boolean isBot(EntityPlayer entity)
    {
        if (entity.getUniqueID().toString().startsWith(entity.getName()))
            return true;
        if (!StringUtils.stripControlCodes(entity.getGameProfile().getName()).equals(entity.getName()))
            return true;
        if (entity.getGameProfile().getId() != entity.getUniqueID())
            return true;

        return _playersMap.containsKey(entity.getEntityId());
    }
}
