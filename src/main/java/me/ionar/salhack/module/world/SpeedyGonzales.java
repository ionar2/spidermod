package me.ionar.salhack.module.world;

import java.awt.Color;

import me.ionar.salhack.events.player.EventPlayerClickBlock;
import me.ionar.salhack.events.player.EventPlayerDamageBlock;
import me.ionar.salhack.events.player.EventPlayerResetBlockRemoving;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public final class SpeedyGonzales extends Module
{
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
    { "Mode", "M" }, "The speed-mine mode to use.", Mode.Instant);
    public final Value<Float> Speed = new Value<Float>("Speed", new String[]
    { "S" }, "Speed for Bypass Mode", 1.0f, 0.0f, 1.0f, 0.1f);

    private enum Mode
    {
        Packet, Damage, Instant, Bypass
    }

    public final Value<Boolean> reset = new Value<Boolean>("Reset", new String[]
    { "Res" }, "Stops current block destroy damage from resetting if enabled.", true);
    public final Value<Boolean> doubleBreak = new Value<Boolean>("DoubleBreak", new String[]
    { "DoubleBreak", "Double", "DB" }, "Mining a block will also mine the block above it, if enabled.", false);

    public SpeedyGonzales()
    {
        super("SpeedyGonzales", new String[]
        { "Speedy Gonzales" }, "Allows you to break blocks faster", "NONE", 0x24DB60, ModuleType.WORLD);
    }

    @Override
    public String getMetaData()
    {
        return this.mode.getValue().name();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        mc.playerController.blockHitDelay = 0;

        if (this.reset.getValue() && Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown())
        {
            mc.playerController.isHittingBlock = false;
        }
        
    });

    @EventHandler
    private Listener<EventPlayerResetBlockRemoving> ResetBlock = new Listener<>(p_Event ->
    {
        if (this.reset.getValue())
        {
            p_Event.cancel();
        }
    });

    @EventHandler
    private Listener<EventPlayerClickBlock> ClickBlock = new Listener<>(p_Event ->
    {
        if (this.reset.getValue())
        {
            if (mc.playerController.curBlockDamageMP > 0.1f)
            {
                mc.playerController.isHittingBlock = true;
            }
        }
    });

    @EventHandler
    private Listener<EventPlayerDamageBlock> OnDamageBlock = new Listener<>(p_Event ->
    {
        if (canBreak(p_Event.getPos()))
        {
            if (this.reset.getValue())
            {
                mc.playerController.isHittingBlock = false;
            }

            switch (this.mode.getValue())
            {
            case Packet:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, p_Event.getPos(), p_Event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        p_Event.getPos(), p_Event.getDirection()));
                p_Event.cancel();
                break;
            case Damage:
                if (mc.playerController.curBlockDamageMP >= 0.7f)
                {
                    mc.playerController.curBlockDamageMP = 1.0f;
                }
                break;
            case Instant:
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, p_Event.getPos(), p_Event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        p_Event.getPos(), p_Event.getDirection()));
                mc.playerController.onPlayerDestroyBlock(p_Event.getPos());
                mc.world.setBlockToAir(p_Event.getPos());
                break;
            case Bypass:

                mc.player.swingArm(EnumHand.MAIN_HAND);

                final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(p_Event.getPos());
                
                float l_Speed = blockState.getPlayerRelativeBlockHardness(mc.player, mc.world, p_Event.getPos()) * Speed.getValue();
                
                
              //  mc.playerController.onPlayerDestroyBlock(;)
                
                break;
            }
        }

        if (this.doubleBreak.getValue())
        {
            final BlockPos above = p_Event.getPos().add(0, 1, 0);

            if (canBreak(above) && mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5f)
            {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, p_Event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        above, p_Event.getDirection()));
                mc.playerController.onPlayerDestroyBlock(above);
                mc.world.setBlockToAir(above);
            }
        }
    });

    private boolean canBreak(BlockPos pos)
    {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

}
