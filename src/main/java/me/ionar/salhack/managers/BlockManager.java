package me.ionar.salhack.managers;

import me.ionar.salhack.main.Wrapper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class BlockManager
{
    private static Minecraft mc = Wrapper.GetMC();
    
    private static BlockPos _currBlock = null;
    private static boolean _started = false;
    
    public static void SetCurrentBlock(BlockPos block)
    {
        _currBlock = block;
        _started = false;
    }
    
    public static BlockPos GetCurrBlock()
    {
        return _currBlock;
    }
    
    public static boolean GetState()
    {
        if (_currBlock != null)
            return IsDoneBreaking(mc.world.getBlockState(_currBlock));
        
        return false;
    }
    
    private static boolean IsDoneBreaking(IBlockState blockState)
    {
        return blockState.getBlock() == Blocks.BEDROCK
                || blockState.getBlock() == Blocks.AIR
                || blockState.getBlock() instanceof BlockLiquid;
    }
    
    public static boolean Update(float range, boolean rayTrace)
    {
        if (_currBlock == null)
            return false;
        
        IBlockState state = mc.world.getBlockState(_currBlock);
        
        if (IsDoneBreaking(state) || mc.player.getDistanceSq(_currBlock) > Math.pow(range, range))
        {
            _currBlock = null;
            return false;
        }
        
        // CPacketAnimation
        mc.player.swingArm(EnumHand.MAIN_HAND);
        
        EnumFacing facing = EnumFacing.UP;
        
        if (rayTrace)
        {
            RayTraceResult result = mc.world.rayTraceBlocks(
                    new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                    new Vec3d(_currBlock.getX() + 0.5, _currBlock.getY() - 0.5,
                            _currBlock.getZ() + 0.5));
            
            if (result != null && result.sideHit != null)
                facing = result.sideHit;
        }
        
        if (!_started)
        {
            _started = true;
            // Start Break
            
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, _currBlock, facing));
        }
        else
        {
            mc.playerController.onPlayerDamageBlock(_currBlock, facing);
        }
        
        return true;
    }
}
