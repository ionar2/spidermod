package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.util.ResourceLocation;

public class EventPlayerGetLocationSkin extends MinecraftEvent
{
    private ResourceLocation m_Location = null;
    
    public EventPlayerGetLocationSkin()
    {
        super();
    }
    
    public void SetResourceLocation(ResourceLocation p_Location)
    {
        m_Location = p_Location;
    }

    public ResourceLocation GetResourceLocation()
    {
        return m_Location;
    }
}
