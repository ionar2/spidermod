package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class AutoWalkModule extends Module
{
    public AutoWalkModule()
    {
        super("AutoWalk", new String[]
        { "AW" }, "Automatically walks forward", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnUpdate = new Listener<>(p_Event ->
    {
        mc.gameSettings.keyBindForward.pressed = true;
    });
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        mc.gameSettings.keyBindForward.pressed = false;
    }
}
