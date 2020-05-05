package me.ionar.salhack.module.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.SalUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class AutoTrap extends Module
{
    private final Vec3d[] offsetsDefault = new Vec3d[]
    {
            new Vec3d(0.0, 0.0, -1.0), // left
            new Vec3d(1.0, 0.0, 0.0),  // right
            new Vec3d(0.0, 0.0, 1.0), // forwards
            new Vec3d(-1.0, 0.0, 0.0), // back
            new Vec3d(0.0, 1.0, -1.0), // +1 left
            new Vec3d(1.0, 1.0, 0.0), // +1 right
            new Vec3d(0.0, 1.0, 1.0), // +1 forwards
            new Vec3d(-1.0, 1.0, 0.0), // +1 back
            new Vec3d(0.0, 2.0, -1.0), // +2 left
            new Vec3d(1.0, 2.0, 0.0), // +2 right
            new Vec3d(0.0, 2.0, 1.0), // +2 forwards
            new Vec3d(-1.0, 2.0, 0.0), // +2 backwards
            new Vec3d(0.0, 3.0, -1.0), // +3 left
            new Vec3d(0.0, 3.0, 0.0) // +3 middle
    };
    private final Vec3d[]  offsetsTall = new Vec3d[]
    {
            new Vec3d(0.0, 0.0, -1.0), // left
            new Vec3d(1.0, 0.0, 0.0),  // right
            new Vec3d(0.0, 0.0, 1.0), // forwards
            new Vec3d(-1.0, 0.0, 0.0), // back
            new Vec3d(0.0, 1.0, -1.0), // +1 left
            new Vec3d(1.0, 1.0, 0.0), // +1 right
            new Vec3d(0.0, 1.0, 1.0), // +1 forwards
            new Vec3d(-1.0, 1.0, 0.0), // +1 back
            new Vec3d(0.0, 2.0, -1.0), // +2 left
            new Vec3d(1.0, 2.0, 0.0), // +2 right
            new Vec3d(0.0, 2.0, 1.0), // +2 forwards
            new Vec3d(-1.0, 2.0, 0.0), // +2 backwards
            new Vec3d(0.0, 3.0, -1.0), // +3 left
            new Vec3d(0.0, 3.0, 0.0), // +3 middle
            new Vec3d(0.0, 4.0, 0.0) // +4 middle
    };
    public final Value<Float> range = new Value<Float>("range", new String[]
    { "range" }, "Range", 5.5f, 0f, 10.0f, 1.0f);
    public final Value<Integer> blockPerTick = new Value<Integer>("blockPerTick", new String[]
    { "blockPerTick" }, "Blocks per Tick", 4, 1, 10, 1);
    public final Value<Boolean> rotate = new Value<Boolean>("rotate", new String[]
    { "rotate" }, "Rotate", true);
    public final Value<Boolean> announceUsage = new Value<Boolean>("announceUsage", new String[]
    { "announceUsage" }, "Announce Usage", true);
    public final Value<Boolean> EChests = new Value<Boolean>("EChests", new String[]
    { "EChests" }, "EChests", true);
    
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"Mode"}, "The mode to use for autotrap", Modes.Full);
    
    
    public enum Modes
    {
        Full,
        Tall,
    }

    public AutoTrap()
    {
        super("AutoTrap", new String[]
        { "AutoTrap" }, "AutoTrap", "NONE", 0x24DB43, ModuleType.COMBAT);
    }
    
    private String lastTickTargetName = "";
    private int playerHotbarSlot = -1;
    private int lastHotbarSlot = -1;
    private boolean isSneaking = false;
    private int offsetStep = 0;
    private boolean firstRun = true;

    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.player == null)
        {
            toggle();
            return;
        }
        
        firstRun = true;
        playerHotbarSlot = mc.player.inventory.currentItem;
        lastHotbarSlot = -1;

        if (findObiInHotbar() == -1)
        {
            SalHack.SendMessage("[AutoTrap] You do not have any obisidan in your hotbar!");
            toggle();
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1)
            mc.player.inventory.currentItem = playerHotbarSlot;

        if (isSneaking)
        {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }
        playerHotbarSlot = -1;
        lastHotbarSlot = -1;
        if (announceUsage.getValue())
            SalHack.SendMessage("[AutoTrap] Disabled!");
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        EntityPlayer closestTarget = SalUtil.findClosestTarget();
        if (closestTarget == null)
        {
            if (firstRun)
            {
                firstRun = false;
                if (announceUsage.getValue())
                {
                    SalHack.SendMessage("[AutoTrap] Enabled, waiting for target.");
                }
            }
            return;
        }
        if (firstRun)
        {
            firstRun = false;
            lastTickTargetName = closestTarget.getName();
            if (announceUsage.getValue())
            {
                SalHack.SendMessage("[AutoTrap] Enabled, target: " + lastTickTargetName);
            }
        }
        else if (!lastTickTargetName.equals(closestTarget.getName()))
        {
            lastTickTargetName = closestTarget.getName();
            offsetStep = 0;
            if (announceUsage.getValue())
            {
                SalHack.SendMessage("[AutoTrap] New target: " + lastTickTargetName);
            }
        }

        final List<Vec3d> placeTargets = new ArrayList<Vec3d>();
        
        switch (Mode.getValue())
        {
            case Full:
                Collections.addAll(placeTargets, offsetsDefault);
                break;
            case Tall:
                Collections.addAll(placeTargets, offsetsTall);
                break;
            default:
                break;
        }
        
        int blocksPlaced = 0;
        while (blocksPlaced < blockPerTick.getValue())
        {
            if (offsetStep >= placeTargets.size())
            {
                offsetStep = 0;
                break;
            }
            final BlockPos offsetPos = new BlockPos((Vec3d) placeTargets.get(offsetStep));
            final BlockPos targetPos = new BlockPos(closestTarget.getPositionVector()).down().add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
            boolean shouldTryToPlace = true;
            if (!mc.world.getBlockState(targetPos).getMaterial().isReplaceable())
                shouldTryToPlace = false;

            for (final Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity((Entity) null, new AxisAlignedBB(targetPos)))
            {
                if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb))
                {
                    shouldTryToPlace = false;
                    break;
                }
            }

            if (shouldTryToPlace && placeBlock(targetPos))
            {
                ++blocksPlaced;
            }
            ++offsetStep;
        }
        if (blocksPlaced > 0)
        {
            if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1)
            {
                mc.player.inventory.currentItem = playerHotbarSlot;
                lastHotbarSlot = playerHotbarSlot;
            }
            if (isSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                isSneaking = false;
            }
        }
    });

    private boolean placeBlock(final BlockPos pos)
    {
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable())
            return false;
        if (!BlockInteractionHelper.checkForNeighbours(pos))
            return false;
        final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        for (final EnumFacing side : EnumFacing.values())
        {
            final BlockPos neighbor = pos.offset(side);
            final EnumFacing side2 = side.getOpposite();
            if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
            {
                final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                if (eyesPos.distanceTo(hitVec) <= range.getValue())
                {
                    final int obiSlot = findObiInHotbar();
                    if (obiSlot == -1)
                    {
                        toggle();
                        return false;
                    }
                    if (lastHotbarSlot != obiSlot)
                    {
                        mc.player.inventory.currentItem = obiSlot;
                        lastHotbarSlot = obiSlot;
                    }
                    final Block neighborPos = mc.world.getBlockState(neighbor).getBlock();
                    if (BlockInteractionHelper.blackList.contains(neighborPos) || BlockInteractionHelper.shulkerList.contains(neighborPos))
                    {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        isSneaking = true;
                    }
                    if (rotate.getValue())
                    {
                        BlockInteractionHelper.faceVectorPacketInstant(hitVec);
                    }
                    mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    return true;
                }
            }
        }
        return false;
    }

    private int findObiInHotbar()
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock)
            {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();
                
                if (EChests.getValue())
                {
                    if (block instanceof BlockEnderChest)
                        return i;
                }
                else if (block instanceof BlockObsidian)
                {
                    return i;
                }
            }
        }
        return -1;
    }
}
