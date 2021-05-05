package me.ionar.salhack.module.world;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerClickBlock;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.managers.BlockManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class NukerBypassModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "Mode of breaking to use, Creative will get you kicked on most servers.", Modes.Survival);
    public final Value<Boolean> ClickSelect = new Value<Boolean>("Click Select", new String[] {"CS"}, "Click blocks you want nuker to only target", false);
    public final Value<Boolean> Flatten = new Value<Boolean>("Flatten", new String[] {"F"}, "Flattens at your feet", false);
    public final Value<Boolean> Rotates = new Value<Boolean>("Rotates", new String[] {"R"}, "Rotates towards selected blocks, you won't bypass NCP without this", true);
    public final Value<Boolean> Raytrace = new Value<Boolean>("Raytrace", new String[] {"Ray"}, "Performs a raytrace calculation in order to determine the best facing towards the block", true);
    public final Value<Float> Range = new Value<Float>("Range", new String[] {"Range"}, "The range to search for blocks", 3.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Blahks> Block = new Value<Blahks>("Blocks", new String[] {"Range"}, "Blocks", Blahks.threeXthree);
    public enum Modes
    {
        Survival,
        Creative,
    }

    public enum Blahks
    {
        threeXthree,
        twoXthree,
        oneXthree,
        Highway2,
        Highway3,
        Highway4,
    }

    public NukerBypassModule()
    {
        super("NukerBypass", new String[] {"NukerBypass"}, "Attempting to fix packet spam kick on nuker", "NONE", -1, ModuleType.HIGHWAY);
    }

    private Block _clickSelectBlock = null;
    private BlockPos _lastPlayerPos = null;

    @Override
    public void onEnable()
    {
        super.onEnable();

        _clickSelectBlock = null;
    }

    @EventHandler
    private Listener<EventPlayerClickBlock> onClickBlock = new Listener<>(event ->
    {
        IBlockState state = mc.world.getBlockState(event.Location);

        if (state == null || state.getBlock() == Blocks.AIR)
            return;

        _clickSelectBlock = state.getBlock();
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE || event.isCancelled())
            return;

        if (ClickSelect.getValue())
        {
            if (_clickSelectBlock == null)
                return;
        }

        BlockPos selectedBlock = null;

        if (BlockManager.GetCurrBlock() != null)
        {
            // cancel this event
            event.cancel();

            // calculate rotations to the block
            final double rotations[] =  EntityUtil.calculateLookAt(
                    BlockManager.GetCurrBlock().getX() + 0.5,
                    BlockManager.GetCurrBlock().getY() - 0.5,
                    BlockManager.GetCurrBlock().getZ() + 0.5,
                    mc.player);

            // send packets to face this serverside
            PlayerUtil.PacketFacePitchAndYaw((float)rotations[1], (float)rotations[0]);

            // update our breaking animations / sync
            if (BlockManager.Update(Range.getValue(), Raytrace.getValue()));
            return;
        }

        final float range = Range.getValue();

        final BlockPos flooredPos = PlayerUtil.GetLocalPlayerPosFloored();
        switch (Block.getValue()) {
            case threeXthree: {
                for (BlockPos pos : BlockInteractionHelper.getCube()) {
                    if (Flatten.getValue() && pos.getY() < flooredPos.getY())
                        continue;

                    IBlockState state = mc.world.getBlockState(pos);

                    if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                        continue;

                    if (ClickSelect.getValue()) {
                        if (_clickSelectBlock != null) {
                            if (state.getBlock() != _clickSelectBlock)
                                continue;
                        }
                    }

                    if (Mode.getValue() == Modes.Creative) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {

                        }
                        continue;
                    }

                    if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                        continue;

                    if (selectedBlock == null) {
                        selectedBlock = pos;
                        continue;
                    } else {
                        double dist = pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);

                        if (selectedBlock.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) < dist)
                            continue;

                        if (dist <= Range.getValue())
                            selectedBlock = pos;
                    }
                break;
                }
            }
            case oneXthree: {
                for (BlockPos pos : BlockInteractionHelper.get1x3()) {
                    if (Flatten.getValue() && pos.getY() < flooredPos.getY())
                        continue;

                    IBlockState state = mc.world.getBlockState(pos);

                    if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                        continue;

                    if (ClickSelect.getValue()) {
                        if (_clickSelectBlock != null) {
                            if (state.getBlock() != _clickSelectBlock)
                                continue;
                        }
                    }

                    if (Mode.getValue() == Modes.Creative) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ignored) {

                        }
                        continue;
                    }

                    if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                        continue;

                    if (selectedBlock == null) {
                        selectedBlock = pos;
                        continue;
                    } else {
                        double dist = pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);

                        if (selectedBlock.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) < dist)
                            continue;

                        if (dist <= Range.getValue())
                            selectedBlock = pos;
                    }
                break;
                }
            }
            case twoXthree: {
                BlockPos currentPos = PlayerUtil.GetLocalPlayerPosFloored();
                if (_lastPlayerPos == null || !_lastPlayerPos.equals(currentPos)) {

                    for (BlockPos pos : BlockInteractionHelper.get2x3()) {
                        if (Flatten.getValue() && pos.getY() < flooredPos.getY())
                            continue;

                        IBlockState state = mc.world.getBlockState(pos);

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;

                        if (ClickSelect.getValue()) {
                            if (_clickSelectBlock != null) {
                                if (state.getBlock() != _clickSelectBlock)
                                    continue;
                            }
                        }

                        if (Mode.getValue() == Modes.Creative) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                            _lastPlayerPos = PlayerUtil.GetLocalPlayerPosFloored();

                            continue;
                        }

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;


                        if (selectedBlock == null) {
                            selectedBlock = pos;
                            continue;
                        } else {
                            double dist = pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);

                            if (selectedBlock.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) < dist)
                                continue;

                            if (dist <= Range.getValue())
                                selectedBlock = pos;
                        }
                    }
                break;
                }
            }
            case Highway3: {
                BlockPos currentPos = PlayerUtil.GetLocalPlayerPosFloored();
                if (_lastPlayerPos == null || !_lastPlayerPos.equals(currentPos)) {

                    for (BlockPos pos : BlockInteractionHelper.getHighway3()) {

                        IBlockState state = mc.world.getBlockState(pos);

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;

                        if (ClickSelect.getValue()) {
                            if (_clickSelectBlock != null) {
                                if (state.getBlock() != _clickSelectBlock)
                                    continue;
                            }
                        }

                        if (Mode.getValue() == Modes.Creative) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                            _lastPlayerPos = PlayerUtil.GetLocalPlayerPosFloored();

                            continue;
                        }

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;


                        if (selectedBlock == null) {
                            selectedBlock = pos;
                            continue;
                        } else {
                            double dist = pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);

                            if (selectedBlock.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) < dist)
                                continue;

                            if (dist <= Range.getValue())
                                selectedBlock = pos;
                        }
                    }
                break;
                }
            }
            case Highway4: {
                BlockPos currentPos = PlayerUtil.GetLocalPlayerPosFloored();
                if (_lastPlayerPos == null || !_lastPlayerPos.equals(currentPos)) {

                    for (BlockPos pos : BlockInteractionHelper.getHighway4()) {

                        IBlockState state = mc.world.getBlockState(pos);

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;

                        if (Mode.getValue() == Modes.Creative) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                            _lastPlayerPos = PlayerUtil.GetLocalPlayerPosFloored();

                            continue;

                        }

                        if (ClickSelect.getValue()) {
                            if (_clickSelectBlock != null) {
                                if (state.getBlock() != _clickSelectBlock)
                                    continue;
                            }
                        }

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;


                        if (selectedBlock == null) {
                            selectedBlock = pos;
                            continue;
                        } else {
                            double dist = pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);

                            if (selectedBlock.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) < dist)
                                continue;

                            if (dist <= Range.getValue())
                                selectedBlock = pos;

                        }
                    }
                break;
                }
            }
            case Highway2: {
                BlockPos currentPos = PlayerUtil.GetLocalPlayerPosFloored();
                if (_lastPlayerPos == null || !_lastPlayerPos.equals(currentPos)) {

                    for (BlockPos pos : BlockInteractionHelper.getHighway2()) {

                        IBlockState state = mc.world.getBlockState(pos);

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;

                        if (Mode.getValue() == Modes.Creative) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                            _lastPlayerPos = PlayerUtil.GetLocalPlayerPosFloored();

                            continue;
                        }

                        if (ClickSelect.getValue()) {
                            if (_clickSelectBlock != null) {
                                if (state.getBlock() != _clickSelectBlock)
                                    continue;
                            }
                        }

                        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER)
                            continue;


                        if (selectedBlock == null) {
                            selectedBlock = pos;
                            continue;
                        } else {
                            double dist = pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);

                            if (selectedBlock.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) < dist)
                                continue;

                            if (dist <= Range.getValue())
                                selectedBlock = pos;
                        }
                    }
                break;
                }
            }
        }

        if (selectedBlock == null)
            return;

        if (Mode.getValue() != Modes.Creative)
            BlockManager.SetCurrentBlock(selectedBlock);
    });
}
