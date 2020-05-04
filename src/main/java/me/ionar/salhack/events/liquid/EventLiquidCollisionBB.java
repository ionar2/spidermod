package me.ionar.salhack.events.liquid;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class EventLiquidCollisionBB extends MinecraftEvent
{
    private AxisAlignedBB boundingBox;
    private BlockPos blockPos;

    public EventLiquidCollisionBB()
    {
        super();
    }

    public EventLiquidCollisionBB(BlockPos blockPos)
    {
        super();
        this.blockPos = blockPos;
    }

    public AxisAlignedBB getBoundingBox()
    {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox)
    {
        this.boundingBox = boundingBox;
    }

    public BlockPos getBlockPos()
    {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos)
    {
        this.blockPos = blockPos;
    }
}
