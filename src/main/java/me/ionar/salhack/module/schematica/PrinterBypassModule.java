package me.ionar.salhack.module.schematica;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.schematica.EventSchematicaPlaceBlock;
import me.ionar.salhack.events.schematica.EventSchematicaPlaceBlockFull;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.world.AutoBuilderModule.Modes;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PrinterBypassModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "Mode" }, "Which mode to use for printer bypass", Modes.Full);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "Delay" }, "Delay of the place for Full mode", 0f, 0.0f, 1.0f, 0.1f);

    public enum Modes
    {
        Packet,
        Full,
    }

    private BlockPos BlockToPlace = null;
    private Item NeededItem = null;
    private Timer timer = new Timer();

    public PrinterBypassModule()
    {
        super("PrinterBypass", new String[]
        { "PrinterNCP" }, "Faces block rotations on schematica place block events", "NONE", 0xDB24AB, ModuleType.SCHEMATICA);
    }

    @Override
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }

    @EventHandler
    private Listener<EventSchematicaPlaceBlock> Position = new Listener<>(p_Event ->
    {
        if (Mode.getValue() == Modes.Packet)
        {
            BlockInteractionHelper.faceVectorPacketInstant(new Vec3d(p_Event.Pos.getX(), p_Event.Pos.getY(), p_Event.Pos.getZ()));
        }
    });

    @Override
    public void onEnable()
    {
        super.onEnable();
        BlockToPlace = null;
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        BlockToPlace = null;
    }

    @EventHandler
    private Listener<EventSchematicaPlaceBlockFull> OnSchematicaPlaceBlockFull = new Listener<>(p_Event ->
    {
        if (Mode.getValue() != Modes.Full)
            return;

        p_Event.cancel();

        boolean l_Result = BlockToPlace == null;

        if (l_Result)
            BlockToPlace = p_Event.Pos;

        p_Event.Result = l_Result;

        if (p_Event.ItemStack != null)
            NeededItem = p_Event.ItemStack;
        else
            NeededItem = null;
    });

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;

        if (BlockToPlace == null)
            return;

        if (!timer.passed(Delay.getValue() * 1000f))
            return;

        /*if (NeededItem != null)
        {
            if (mc.player.getHeldItemMainhand().getItem() != NeededItem)
            {
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    if (mc.player.inventory.getStackInSlot(l_I).getItem() == NeededItem)
                    {
                        mc.player.inventory.currentItem = l_I;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

        }*/

        timer.reset();

        float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(BlockToPlace.getX(), BlockToPlace.getY(), BlockToPlace.getZ()));

        /*
         * ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
         * 
         * if (l_Result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(l_Pos).getMaterial().isReplaceable()) continue;
         * 
         * if (l_Result == ValidResult.NoNeighbors) continue;
         */

        p_Event.cancel();

        boolean l_IsSprinting = mc.player.isSprinting();

        if (l_IsSprinting != mc.player.serverSprintState)
        {
            if (l_IsSprinting)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = l_IsSprinting;
        }

        boolean l_IsSneaking = mc.player.isSneaking();

        if (l_IsSneaking != mc.player.serverSneakState)
        {
            if (l_IsSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = l_IsSneaking;
        }

        if (PlayerUtil.isCurrentViewEntity())
        {
            float l_Pitch = rotations[1];
            float l_Yaw = rotations[0];

            mc.player.rotationYawHead = l_Yaw;

            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double l_PosXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double l_PosYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double l_PosZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double l_YawDifference = (double) (l_Yaw - mc.player.lastReportedYaw);
            double l_RotationDifference = (double) (l_Pitch - mc.player.lastReportedPitch);
            ++mc.player.positionUpdateTicks;
            boolean l_MovedXYZ = l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean l_MovedRotation = l_YawDifference != 0.0D || l_RotationDifference != 0.0D;

            if (mc.player.isRiding())
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, l_Yaw, l_Pitch, mc.player.onGround));
                l_MovedXYZ = false;
            }
            else if (l_MovedXYZ && l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (l_MovedXYZ)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            }
            else if (l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (mc.player.prevOnGround != mc.player.onGround)
            {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (l_MovedXYZ)
            {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (l_MovedRotation)
            {
                mc.player.lastReportedYaw = l_Yaw;
                mc.player.lastReportedPitch = l_Pitch;
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }

        PlaceResult l_Place = BlockInteractionHelper.place(BlockToPlace, 5.0f, false, false);

        //if (l_Place == PlaceResult.Placed)
        //    SendMessage("Placed! at " + BlockToPlace.toString());

        BlockToPlace = null;
    });

}
