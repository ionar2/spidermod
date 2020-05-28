package me.ionar.salhack.module.render;

import me.ionar.salhack.events.render.EventRenderOrientCamera;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public class ViewClipModule extends Module
{
    public ViewClipModule()
    {
        super("ViewClip", new String[] {"ViewC"}, "Prevents the third person camera from ray-tracing", "NONE", -1, ModuleType.RENDER);
    }

    @EventHandler
    private Listener<EventRenderOrientCamera> OnRenderOrientCamera = new Listener<>(event ->
    {
        event.cancel();
    });
}
