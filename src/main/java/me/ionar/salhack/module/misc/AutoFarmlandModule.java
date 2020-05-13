package me.ionar.salhack.module.misc;

import java.util.Comparator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSpade;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AutoFarmlandModule extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for grass/dirt", 4, 0, 10, 1);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);
    
    public AutoFarmlandModule()
    {
        super("AutoFarmland", new String[] {""}, "Automatically sets grass or dirt to farmland", "NONE", -1, ModuleType.MISC);
    }

    private Timer timer = new Timer();
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (!timer.passed(Delay.getValue() * 100))
            return;
        
        BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                .filter(p_Pos -> IsValidBlockPos(p_Pos))
                                .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                .orElse(null);
        
        if (l_ClosestPos != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemHoe)
        {
            timer.reset();
            
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_ClosestPos.getX() + 0.5,
                    l_ClosestPos.getY() - 0.5,
                    l_ClosestPos.getZ() + 0.5,
                    mc.player);
            
            mc.player.rotationYawHead = (float) l_Pos[0];
            
            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);

            mc.player.swingArm(EnumHand.MAIN_HAND);
            
            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(l_ClosestPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
        }
    });
    
    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);
        
        if (l_State.getBlock() instanceof BlockDirt || l_State.getBlock() instanceof BlockGrass)
            return mc.world.getBlockState(p_Pos.up()).getBlock() == Blocks.AIR;
        
        return false;
    }
}
