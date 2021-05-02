package me.ionar.salhack.module.combat;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import me.ionar.salhack.events.player.EventPlayerUpdate;

public final class FastFallModule extends Module {

    public FastFallModule()
    {
        super("FastFall", new String[] {"FastFall"}, "Makes you fall faster to get into holes easier", "NONE", 0x24CADB, ModuleType.COMBAT);
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
        {
            if (mc.player.onGround)
                --mc.player.motionY;
        });
}