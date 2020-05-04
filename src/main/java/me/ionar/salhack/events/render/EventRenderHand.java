package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;

public class EventRenderHand extends MinecraftEvent
{
    public float PartialTicks;
    public int Pass;

    public EventRenderHand(float partialTicks, int pass)
    {
        super();
        
        PartialTicks = partialTicks;
        Pass = pass;
    }

}
