package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class EventRenderSetupFog extends MinecraftEvent
{
    public int StartCoords;
    public float PartialTicks;

    public EventRenderSetupFog(int startCoords, float partialTicks)
    {
        StartCoords = startCoords;
        PartialTicks = partialTicks;
    }

}
