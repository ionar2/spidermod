package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import me.ionar.salhack.main.Wrapper;
import net.minecraft.client.gui.ScaledResolution;

public class EventRenderGameOverlay extends MinecraftEvent
{
    public float PartialTicks;
    public ScaledResolution scaledResolution = new ScaledResolution(Wrapper.GetMC());

    public EventRenderGameOverlay(float p_PartialTicks)
    {
        super();
        PartialTicks = p_PartialTicks;
    }

    public ScaledResolution getScaledResolution()
    {
        return scaledResolution;
    }

}
