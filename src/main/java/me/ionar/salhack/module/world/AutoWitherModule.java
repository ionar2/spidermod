package me.ionar.salhack.module.world;

import java.util.ArrayList;
import java.util.Iterator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.Pair;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class AutoWitherModule extends Module
{
    public AutoWitherModule()
    {
        super("AutoWither", new String[] {""}, "Automatically places a wither at the location of your selection if available", "NONE", -1, ModuleType.WORLD);
    }
    
    private BlockPos WitherFeetBlock = null;
    private ArrayList<Pair<BlockPos, Block>> Positions = new ArrayList<Pair<BlockPos, Block>>();
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        Positions.clear();
        WitherFeetBlock = null;
        
        final RayTraceResult l_Ray = mc.objectMouseOver;

        if (l_Ray == null)
            return;

        if (l_Ray.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        IBlockState l_State = mc.world.getBlockState(l_Ray.getBlockPos());
        
        if (l_State.getBlock() == Blocks.SOUL_SAND || l_State.getBlock() == Blocks.AIR || l_State.getBlock() == Blocks.WATER || l_State.getBlock() == Blocks.LAVA)
        {
            /// Check blocks around it
            if (IsValidLocationForWitherBlocks(l_Ray.getBlockPos()))
                WitherFeetBlock = l_Ray.getBlockPos();
        }
        else
        {
            l_State = mc.world.getBlockState(l_Ray.getBlockPos().up());
            
            if (l_State.getBlock() == Blocks.SOUL_SAND || l_State.getBlock() == Blocks.AIR || l_State.getBlock() == Blocks.WATER || l_State.getBlock() == Blocks.LAVA)
                if (IsValidLocationForWitherBlocks(l_Ray.getBlockPos().up()))
                {
                    WitherFeetBlock = l_Ray.getBlockPos().up();

                    Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock, Blocks.SOUL_SAND));                    
                }
        }
        
        if (WitherFeetBlock == null)
        {
            SendMessage("Not a valid location for a wither.");
            toggle();
            return;
        }

        switch (PlayerUtil.GetFacing())
        {
            case East:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().south(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().north(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().south(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().north(), Blocks.SKULL));
                break;
            case North:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().east(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().west(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().east(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().west(), Blocks.SKULL));
                break;
            case South:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().west(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().east(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().west(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().east(), Blocks.SKULL));
                break;
            case West:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().north(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().south(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().north(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().south(), Blocks.SKULL));
                break;
            default:
                break;
        }
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnMotionUpdate = new Listener<>(p_Event ->
    {
        if (Positions.isEmpty())
        {
            SendMessage("Positions is empty");
            toggle();
            return;
        }
        
        Iterator<Pair<BlockPos, Block>> l_Itr = Positions.iterator();
        
        Pair<BlockPos, Block> l_Pos = null;
        boolean l_Placed = false;
        
        while (l_Itr.hasNext())
        {
            l_Pos = l_Itr.next();

            int l_Slot = -1;
            
            if (l_Pos.getSecond() == Blocks.SOUL_SAND)
                l_Slot = GetSoulsandInHotbar();
            else if (l_Pos.getSecond() == Blocks.SKULL)
                l_Slot = GetSkullInHotbar();
            
            if (l_Slot != -1 && mc.player.inventory.currentItem != l_Slot)
            {
                mc.player.inventory.currentItem = l_Slot;
                mc.playerController.updateController();
                return;
            }
            
            PlaceResult l_Place = BlockInteractionHelper.place (l_Pos.getFirst(), 5.0f, false, false);
            
            if (l_Place != PlaceResult.Placed)
                continue;

            final double l_Pos2[] =  EntityUtil.calculateLookAt(
                    l_Pos.getFirst().getX() + 0.5,
                    l_Pos.getFirst().getY() + 0.5,
                    l_Pos.getFirst().getZ() + 0.5,
                    mc.player);
            
            mc.player.rotationYawHead = (float) l_Pos2[0];
            
            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos2[1], (float)l_Pos2[0]);
           
            l_Placed = true;
            break;
        }
        
        if (l_Pos != null && l_Placed)
            Positions.remove(l_Pos);
    });
    
    private boolean IsValidLocationForWitherBlocks(BlockPos p_Pos)
    {
        BlockPos[] l_Positions = {p_Pos.up(), p_Pos.up().east(), p_Pos.up().west(), p_Pos.up().up(), p_Pos.up().up().east(), p_Pos.up().up().west()};
        
        for (BlockPos l_Pos : l_Positions)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State.getBlock() != Blocks.AIR && l_State.getBlock() != Blocks.WATER && l_State.getBlock() != Blocks.LAVA)
                return false;
        }
        
        return true;
    }
    
    private int GetSoulsandInHotbar()
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock)
            {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();
                
                if (block instanceof BlockSoulSand)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    private int GetSkullInHotbar()
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemSkull)
                return i;
        }
        return -1;
    }
}
