package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;

public class EventRenderChunk extends MinecraftEvent
{
    public net.minecraft.util.math.BlockPos BlockPos;
    public net.minecraft.client.renderer.chunk.RenderChunk RenderChunk;
    public EventRenderChunk(RenderChunk renderChunk, BlockPos blockPos)
    {
        BlockPos = blockPos;
        RenderChunk = renderChunk;
    }
}