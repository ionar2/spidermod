package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class EventRenderGameOverlay extends MinecraftEvent
{
    public float PartialTicks;

    public EventRenderGameOverlay(float p_PartialTicks)
    {
        super();
        PartialTicks = p_PartialTicks;
    }

}
