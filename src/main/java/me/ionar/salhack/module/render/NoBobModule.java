package me.ionar.salhack.module.render;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

public class NoBobModule extends Module
{
    public NoBobModule()
    {
        super("NoBob", new String[] {"NoBob"}, "Prevents bobbing by setting distance walked modifier to a static number", "NONE", -1, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        mc.player.distanceWalkedModified = 4.0f;
    });
}
