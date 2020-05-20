package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.util.MovementInputFromOptions;

public final class AutoWalkModule extends Module
{
    public AutoWalkModule()
    {
        super("AutoWalk", new String[]
        { "AW" }, "Automatically walks forward", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnUpdate = new Listener<>(p_Event ->
    {
        mc.player.movementInput.moveForward++;
    });
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        
        mc.gameSettings.keyBindForward.pressed = false;
    }
}
