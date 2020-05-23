package me.ionar.salhack.module.misc;

import java.util.Comparator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AutoTendModule extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for and break torches", 4, 0, 10, 1);
    public final Value<Boolean> Harvest = new Value<Boolean>("Harvest", new String[] {"Harvest"} , "Automatically Harvests", true);
    public final Value<Boolean> Replant = new Value<Boolean>("Replant", new String[] {"Replants"} , "Automatically plants if not harvesting, and there's nothing to harvest", true);

    public final Value<Modes> Mode = new Value<Modes>("ReplantMode", new String[] {"Replant"}, "What crop to plant at empty plowed places", Modes.Wheat);

    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to harvest/replant", 1.0f, 0.0f, 10.0f, 0.1f);
    
    private enum Modes
    {
        Beetrot,
        Carrots,
        Potatos,
        Wheat
    }

    public AutoTendModule()
    {
        super("AutoTend", new String[] {""}, "Breaks and replants plants nearby", "NONE", -1, ModuleType.MISC);
    }

    private AutoBonemealModule Bonemeal;
    private Timer timer = new Timer();

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (Bonemeal == null)
            Bonemeal = (AutoBonemealModule) ModuleManager.Get().GetMod(AutoBonemealModule.class);
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        /// Bonemeal is still doing it's tasks, don't run both at same time.
        if (Bonemeal.IsRunning() && Bonemeal.isEnabled())
            return;

        if (Harvest.getValue())
        {
            if (!timer.passed(Delay.getValue() * 100))
                return;
            
            timer.reset();
            
            BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                    .filter(p_Pos -> IsHarvestBlock(p_Pos))
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

                mc.player.rotationYawHead = (float) l_Pos[0];

                PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);

                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.playerController.clickBlock(l_ClosestPos, EnumFacing.UP);
                return;
            }
        }

        if (Replant.getValue() && HasSeeds())
        {
            if (!timer.passed(Delay.getValue() * 100))
                return;
            
            timer.reset();
            
            BlockPos l_ClosestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                                    .filter(p_Pos -> IsReplantBlock(p_Pos))
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

                mc.player.rotationYawHead = (float) l_Pos[0];

                PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);

                SwitchToSeedSlot();
                mc.player.swingArm(IsItemStackSeed(mc.player.getHeldItemOffhand()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(l_ClosestPos, EnumFacing.UP,
                        IsItemStackSeed(mc.player.getHeldItemOffhand()) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
                return;
            }
        }
    });

    private boolean IsHarvestBlock(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockCrops)
        {
            BlockCrops l_Crop = (BlockCrops)l_State.getBlock();

            if (l_Crop.isMaxAge(l_State))
                return true;
        }
        else if (l_State.getBlock() instanceof BlockReed)
        {
            if (mc.world.getBlockState(p_Pos.down()).getBlock() == Blocks.REEDS)
                return true;
        }

        return false;
    }

    private boolean IsReplantBlock(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() instanceof BlockFarmland)
            return HasNoCropsAndIsPlantable(p_Pos);

        return false;
    }

    private boolean HasNoCropsAndIsPlantable(final BlockPos p_Pos)
    {
        Block block = mc.world.getBlockState(p_Pos.up()).getBlock();
        return block == Blocks.AIR;
    }

    private boolean HasSeeds()
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack.isEmpty())
                continue;

            if (IsItemStackSeed(l_Stack))
                return true;
        }

        return IsItemStackSeed(mc.player.getHeldItemOffhand());
    }

    private boolean IsItemStackSeed(ItemStack p_Stack)
    {
        return !p_Stack.isEmpty() && (p_Stack.getItem() == Items.BEETROOT_SEEDS || p_Stack.getItem() == Items.POTATO || p_Stack.getItem() == Items.WHEAT_SEEDS || p_Stack.getItem() == Items.CARROT);
    }

    private void SwitchToSeedSlot()
    {
        if (IsItemStackSeed(mc.player.getHeldItemOffhand()) || IsItemStackSeed(mc.player.getHeldItemMainhand()))
            return;

        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);

            if (l_Stack.isEmpty())
                continue;

            if (IsItemStackSeed(l_Stack))
            {
                mc.player.inventory.currentItem = l_I;
                mc.playerController.updateController();
                return;
            }
        }

    }
}
