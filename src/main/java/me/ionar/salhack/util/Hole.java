package me.ionar.salhack.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Hole extends Vec3i
{
    private BlockPos blockPos;
    private boolean tall;
    private HoleTypes HoleType;
    
    public enum HoleTypes
    {
        None,
        Normal,
        Obsidian,
        Bedrock,
    }

    public Hole(int x, int y, int z, final BlockPos pos, HoleTypes p_Type)
    {
        super(x, y, z);
        blockPos = pos;
        SetHoleType(p_Type);
    }

    public Hole(int x, int y, int z, final BlockPos pos, HoleTypes p_Type, boolean tall)
    {
        super(x, y, z);
        blockPos = pos;
        this.tall = true;
        SetHoleType(p_Type);
    }

    public boolean isTall()
    {
        return tall;
    }
    
    public BlockPos GetBlockPos()
    {
        return blockPos;
    }

    /**
     * @return the holeType
     */
    public HoleTypes GetHoleType()
    {
        return HoleType;
    }

    /**
     * @param holeType the holeType to set
     */
    public void SetHoleType(HoleTypes holeType)
    {
        HoleType = holeType;
    }
}