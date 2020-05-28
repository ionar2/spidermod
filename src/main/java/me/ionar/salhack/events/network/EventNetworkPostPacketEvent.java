package me.ionar.salhack.events.network;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.network.Packet;

public class EventNetworkPostPacketEvent extends EventNetworkPacketEvent
{
    public EventNetworkPostPacketEvent(Packet p_Packet)
    {
        super(p_Packet);
    }
}
