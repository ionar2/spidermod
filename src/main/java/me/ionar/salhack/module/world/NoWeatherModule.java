package me.ionar.salhack.module.world;

import me.ionar.salhack.events.render.EventRenderRainStrength;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class NoWeatherModule extends Module
{

    public NoWeatherModule()
    {
        super("NoWeather", new String[]
        { "AntiWeather" }, "Allows you to control the weather client-side", "NONE", -1, ModuleType.WORLD);
    }

    @EventHandler
    private Listener<EventRenderRainStrength> OnRainStrength = new Listener<>(p_Event ->
    {
        if (mc.world == null)
            return;

        p_Event.cancel();
    });

}
