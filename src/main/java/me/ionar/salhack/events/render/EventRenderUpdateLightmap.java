package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class EventRenderUpdateLightmap extends MinecraftEvent
{
    public float PartialTicks;
    
    public EventRenderUpdateLightmap(float p_PartialTicks)
    {
        super();
        PartialTicks = p_PartialTicks;
    }
}
