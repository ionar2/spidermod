package me.ionar.salhack.module.combat;

import java.util.ArrayList;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiCityBossModule extends Module
{
    public final Value<Boolean> TrapCheck = new Value<Boolean>("TrapCheck", new String[]
    { "HC" }, "Only functions if you're trapped", false);

    public AntiCityBossModule()
    {
        super("AntiCityBoss", new String[]
        { "AntiTrap" }, "Automatically places 4 obsidian in the direction your facing to prevent getting crystaled",
                "NONE", -1, ModuleType.COMBAT);
    }
    
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        if (TrapCheck.getValue() && !PlayerUtil.IsPlayerTrapped())
            return;
        
        BlockPos l_CenterPos = PlayerUtil.GetLocalPlayerPosFloored();
        ArrayList<BlockPos> BlocksToFill = new ArrayList<BlockPos>();
        
        switch (PlayerUtil.GetFacing())
        {
            case East:
                BlocksToFill.add(l_CenterPos.east().east());
                BlocksToFill.add(l_CenterPos.east().east().up());
                BlocksToFill.add(l_CenterPos.east().east().east());
                BlocksToFill.add(l_CenterPos.east().east().east().up());
                break;
            case North:
                BlocksToFill.add(l_CenterPos.north().north());
                BlocksToFill.add(l_CenterPos.north().north().up());
                BlocksToFill.add(l_CenterPos.north().north().north());
                BlocksToFill.add(l_CenterPos.north().north().north().up());
                break;
            case South:
                BlocksToFill.add(l_CenterPos.south().south());
                BlocksToFill.add(l_CenterPos.south().south().up());
                BlocksToFill.add(l_CenterPos.south().south().south());
                BlocksToFill.add(l_CenterPos.south().south().south().up());
                break;
            case West:
                BlocksToFill.add(l_CenterPos.west().west());
                BlocksToFill.add(l_CenterPos.west().west().up());
                BlocksToFill.add(l_CenterPos.west().west().west());
                BlocksToFill.add(l_CenterPos.west().west().west().up());
                break;
            default:
                break;
        }
        
        BlockPos l_PosToFill = null;
        
        for (BlockPos l_Pos : BlocksToFill)
        {
            ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
            
            if (l_Result != ValidResult.Ok)
                continue;
            
            l_PosToFill = l_Pos;
            break;
        }
        
        if (l_PosToFill != null)
        {
            int lastSlot;
            final int slot = findStackHotbar(Blocks.OBSIDIAN);
            lastSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
            
            p_Event.cancel();
            float[] rotations = BlockInteractionHelper
                    .getLegitRotations(new Vec3d(l_PosToFill.getX(), l_PosToFill.getY(), l_PosToFill.getZ()));
            PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
            BlockInteractionHelper.place(l_PosToFill, 5.0f, false, false);
            Finish(lastSlot);
        }
    });
    
    private void Finish(int p_LastSlot)
    {
        if (!slotEqualsBlock(p_LastSlot, Blocks.OBSIDIAN))
        {
            mc.player.inventory.currentItem = p_LastSlot;
        }
        mc.playerController.updateController();
    }

    public boolean hasStack(Block type)
    {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private int findStackHotbar(Block type)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type)
                {
                    return i;
                }
            }
        }
        return -1;
    }
}
