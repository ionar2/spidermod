package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.renderer.chunk.RenderChunk;

public class EventRenderChunkContainer extends MinecraftEvent
{
    public RenderChunk RenderChunk;
    public EventRenderChunkContainer(RenderChunk renderChunk)
    {
        RenderChunk = renderChunk;
    }
}
