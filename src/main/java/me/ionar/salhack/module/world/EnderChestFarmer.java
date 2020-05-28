package me.ionar.salhack.module.world;

import java.util.Comparator;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class EnderChestFarmer extends Module
{
    public static Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for enderchests, and place them", 4, 0, 10, 1);
    public Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "D" }, "Timed delay for each place of ender chest", 1f, 0f, 10f, 1f);

    public EnderChestFarmer()
    {
        super("EnderChestFarmer", new String[]{ "EChestFarmer" }, "Autoamatically places enderchests around you, and attempts to mine it", "NONE", -1, ModuleType.WORLD);
    }
    
    private Timer PlaceTimer = new Timer();
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;

        BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                .filter(p_Pos -> IsValidBlockPos(p_Pos))
                                .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                .orElse(null);

        if (l_ClosestPos != null)
        {
            boolean hasPickaxe = mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_PICKAXE;

            if (!hasPickaxe)
            {
                for (int i = 0; i < 9; ++i)
                {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    
                    if (stack.isEmpty())
                        continue;
                    
                    if (stack.getItem() == Items.DIAMOND_PICKAXE)
                    {
                        hasPickaxe = true;
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }
            
            if (!hasPickaxe)
                return;
            
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_ClosestPos.getX() + 0.5,
                    l_ClosestPos.getY() - 0.5,
                    l_ClosestPos.getZ() + 0.5,
                    mc.player);

            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);
            
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK, l_ClosestPos, EnumFacing.UP));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                    l_ClosestPos, EnumFacing.UP));
        }
        else
        {
            if (!PlaceTimer.passed(Delay.getValue()*1000))
                return;
            
            PlaceTimer.reset();
            
            if (!IsCurrItemEnderChest())
            {
                int slot = GetEnderChestSlot();
                
                if (slot == -1)
                    return;
                
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
            }
            
            for (BlockPos pos : BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0))
            {
                PlaceResult result = BlockInteractionHelper.place(pos, Radius.getValue(), true, false);
                
                if (result == PlaceResult.Placed)
                {
                    p_Event.cancel();

                    final double rotations[] =  EntityUtil.calculateLookAt(
                            pos.getX() + 0.5,
                            pos.getY() - 0.5,
                            pos.getZ() + 0.5,
                            mc.player);

                    PlayerUtil.PacketFacePitchAndYaw((float)rotations[1], (float)rotations[0]);
                    return;
                }
            }
        }
    });

    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockEnderChest)
            return true;

        return false;
    }
    
    private boolean IsCurrItemEnderChest()
    {
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.getHeldItemMainhand().getItem();
            return block.getBlock() == Blocks.ENDER_CHEST;
        }

        return false;
    }
    
    private int GetEnderChestSlot()
    {
        for (int i = 0; i < 9; ++i)
        {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock))
                continue;
            
            final ItemBlock block = (ItemBlock) stack.getItem();
            if (block.getBlock() == Blocks.ENDER_CHEST)
                return i;
        }
        
        return -1;
    }
}
