package me.ionar.salhack.module.misc;

import java.util.Comparator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AutoBonemealModule extends Module
{
    public static Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for not fully grown seeds", 4, 0, 10, 1);
    
    public AutoBonemealModule()
    {
        super("AutoBonemeal", new String[] {""}, "", "NONE", -1, ModuleType.MISC);
    }
    
    private boolean IsRunning = false;

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                .filter(p_Pos -> IsValidBlockPos(p_Pos))
                                .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                .orElse(null);
        
        if (l_ClosestPos != null && UpdateBonemealIfNeed())
        {
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_ClosestPos.getX() + 0.5,
                    l_ClosestPos.getY() + 0.5,
                    l_ClosestPos.getZ() + 0.5,
                    mc.player);
            
            mc.player.rotationYawHead = (float) l_Pos[0];
            
            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);

            mc.player.swingArm(EnumHand.MAIN_HAND);
            
            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(l_ClosestPos, EnumFacing.UP,
                mc.player.getHeldItemOffhand().getItem() == Items.DYE ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
            IsRunning = true;
        }
        else
            IsRunning = false;
    });
    
    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);
        
        if (l_State.getBlock() instanceof BlockCrops)
        {
            BlockCrops l_Crop = (BlockCrops)l_State.getBlock();
            
            if (!l_Crop.isMaxAge(l_State))
                return true;
        }
        
        return false;
    }
    
    public boolean IsRunning()
    {
        return IsRunning;
    }
    
    private boolean UpdateBonemealIfNeed()
    {
        ItemStack l_Main = mc.player.getHeldItemMainhand();
        ItemStack l_Off = mc.player.getHeldItemOffhand();
        
        if (!l_Main.isEmpty() && l_Main.getItem() instanceof ItemDye)
        {
            if (IsBoneMealItem(l_Main))
                return true;
        }
        else if (!l_Off.isEmpty() && l_Off.getItem() instanceof ItemDye)
        {
            if (IsBoneMealItem(l_Off))
                return true;
        }
        
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack.isEmpty() || !(l_Stack.getItem() instanceof ItemDye))
                continue;
            
            if (IsBoneMealItem(l_Stack))
            {
                mc.player.inventory.currentItem = l_I;
                mc.playerController.updateController();
                return true;
            }
        }
        
        return false;
    }
    
    private boolean IsBoneMealItem(ItemStack p_Stack)
    {
        return EnumDyeColor.byDyeDamage(p_Stack.getMetadata()) == EnumDyeColor.WHITE;
    }
}
