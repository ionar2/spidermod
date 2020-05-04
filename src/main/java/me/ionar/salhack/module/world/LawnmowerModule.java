package me.ionar.salhack.module.world;

import java.util.Comparator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class LawnmowerModule extends Module
{
    public static Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for and break tall grass", 4, 0, 10, 1);
    public static Value<Boolean> Flowers = new Value<Boolean>("Flowers", new String[] {"R"}, "Break Flowers", true);
    
    public LawnmowerModule()
    {
        super("Lawnmower", new String[] {""}, "", "NONE", -1, ModuleType.WORLD);
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                .filter(p_Pos -> IsValidBlockPos(p_Pos))
                                .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                                .orElse(null);
        
        if (l_ClosestPos != null)
        {
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_ClosestPos.getX() + 0.5,
                    l_ClosestPos.getY() + 0.5,
                    l_ClosestPos.getZ() + 0.5,
                    mc.player);
            
          //  SalHack.SendMessage(l_ClosestPos.toString());
            
            mc.player.rotationYawHead = (float) l_Pos[0];
            
            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);

            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.playerController.clickBlock(l_ClosestPos, EnumFacing.UP);
        }
    });
    
    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);
        
        if (l_State.getBlock() instanceof BlockTallGrass || l_State.getBlock() instanceof BlockDoublePlant)
            return true;
        
        if (Flowers.getValue() && l_State.getBlock() instanceof BlockFlower)
            return true;
        
        return false;
    }
}
