package me.ionar.salhack.module.render;

import me.ionar.salhack.events.render.EventRenderChunk;
import me.ionar.salhack.events.render.EventRenderChunkContainer;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkAnimator extends Module
{
    public final Value<Integer> AnimationLength = new Value<Integer>("Length", new String[]
            {"L"}, "Controls how long (ms) the chunk animation is", 1000, 250, 5000, 1);
    public final Value<Boolean> EasingEnabled = new Value<Boolean>("Easing", new String[]
            {"E"}, "Control if easing is enabled for the chunk animation", true);

    private final WeakHashMap<RenderChunk, AtomicLong> lifespans = new WeakHashMap<>();

    public ChunkAnimator()
    {
        super("ChunkAnimator", new String[] {"ChunkAnimation", "ChunkAnimate"}, "Animates chunks so they rise from the ground", "NONE", 0xDB24AB, ModuleType.RENDER);
    }

    private double easeOutCubic(double t)
    {
        return (--t) * t * t + 1;
    }

    @EventHandler
    private final Listener<EventRenderChunk> RenderChunk = new Listener<>(event ->
    {
        if (Minecraft.getMinecraft().player != null) {
            if (!lifespans.containsKey(event.RenderChunk)) {
                lifespans.put(event.RenderChunk, new AtomicLong(-1L));
            }
        }
    });

    @EventHandler
    private final Listener<EventRenderChunkContainer> RenderChunkContainer = new Listener<>(event ->
    {
        if (lifespans.containsKey(event.RenderChunk)) {
            AtomicLong timeAlive = lifespans.get(event.RenderChunk);
            long timeClone = timeAlive.get();
            if (timeClone == -1L) {
                timeClone = System.currentTimeMillis();
                timeAlive.set(timeClone);
            }

            long timeDifference = System.currentTimeMillis() - timeClone;
            if (timeDifference <= AnimationLength.getValue()) {
                double chunkY = event.RenderChunk.getPosition().getY();
                double offsetY = chunkY / AnimationLength.getValue() * timeDifference;
                if (EasingEnabled.getValue()) {
                    offsetY = chunkY * easeOutCubic(timeDifference / AnimationLength.getValue().doubleValue());
                }
                GlStateManager.translate(0.0, -chunkY + offsetY, 0.0);
            }
        }
    });
}
