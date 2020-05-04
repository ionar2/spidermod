package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;

public class RenderEvent extends MinecraftEvent
{
    private final Tessellator tessellator;
    private final Vec3d renderPos;

    public RenderEvent(Tessellator tessellator, Vec3d renderPos)
    {
        super();
        this.tessellator = tessellator;
        this.renderPos = renderPos;
    }

    public Tessellator getTessellator()
    {
        return tessellator;
    }

    public BufferBuilder getBuffer()
    {
        return tessellator.getBuffer();
    }

    public Vec3d getRenderPos()
    {
        return renderPos;
    }

    public void setTranslation(Vec3d translation)
    {
        getBuffer().setTranslation(-translation.x, -translation.y, -translation.z);
    }

    public void resetTranslation()
    {
        setTranslation(renderPos);
    }

}
