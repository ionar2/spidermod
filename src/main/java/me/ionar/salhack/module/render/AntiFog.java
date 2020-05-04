package me.ionar.salhack.module.render;

import me.ionar.salhack.events.blocks.EventCanCollideCheck;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

public class AntiFog extends Module
{
    public AntiFog()
    {
        super("AntiFog", new String[] {"NoFog"}, "Prevents fog from being rendered", "NONE", 0xDB24AB, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventRenderSetupFog> SetupFog = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });
}
