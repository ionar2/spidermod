// 
// Decompiled by Procyon v0.5.36
// 

package me.ionar.salhack.module.world;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.render.RenderUtil;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.network.play.client.CPacketPlayer;
import me.ionar.salhack.util.entity.PlayerUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.init.Items;
import net.minecraft.block.Block;
import me.ionar.salhack.util.BlockInteractionHelper;
import net.minecraft.init.Blocks;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.events.MinecraftEvent;
import java.util.function.Predicate;
import net.minecraft.client.renderer.culling.Frustum;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.zero.alpine.listener.EventHandler;
import me.ionar.salhack.events.render.EventRenderLayers;
import me.zero.alpine.listener.Listener;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import me.ionar.salhack.util.Timer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.Vec3d;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.Module;

public class AutoBuilderModule extends Module
{
    public final Value<Modes> Mode;
    public final Value<Boolean> rotate;
    public final Value<Integer> BlocksPerTick;
    public final Value<Float> Delay;
    private Vec3d Center;
    private ICamera camera;
    private Timer timer;
    private float PitchHead;
    private boolean SentPacket;
    ArrayList<BlockPos> l_Array;
    @EventHandler
    private Listener<EventRenderLayers> OnRender;
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate;
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent;
    
    public AutoBuilderModule() {
        super("AutoBuilder", new String[] { "AutoSwastika" }, "Builds cool things at your facing block", "H", 9886500, ModuleType.WORLD);
        this.Mode = new Value<Modes>("Mode", new String[] { "" }, "Mode", Modes.Highway);
        this.rotate = new Value<Boolean>("Rotate", new String[] { "rotate" }, "Rotate", true);
        this.BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[] { "BPT" }, "Blocks per tick", 4, 1, 10, 1);
        this.Delay = new Value<Float>("Delay", new String[] { "Delay" }, "Delay of the place", 0.0f, 0.0f, 1.0f, 0.1f);
        this.Center = Vec3d.ZERO;
        this.camera = (ICamera)new Frustum();
        this.timer = new Timer();
        this.PitchHead = 0.0f;
        this.SentPacket = false;
        this.l_Array = new ArrayList<BlockPos>();
        this.OnRender = new Listener<EventRenderLayers>(p_Event -> {
            if (p_Event.getEntityLivingBase() == this.mc.player) {
                p_Event.SetHeadPitch((this.PitchHead == -420.0f) ? this.mc.player.rotationPitch : this.PitchHead);
            }
            return;
        }, (Predicate<EventRenderLayers>[])new Predicate[0]);
        Vec3d pos;
        BlockPos orignPos;
        BlockPos interpPos;
        Pair<Integer, Block> l_Pair;
        int slot;
        double l_Offset;
        int l_X;
        int l_Y;
        int l_X2;
        int l_Y2;
        int l_Tries;
        BlockPos l_Pos;
        boolean l_NeedPlace;
        float[] rotations;
        int lastSlot;
        int l_BlocksPerTick;
        final Iterator<BlockPos> iterator;
        BlockPos l_Pos2;
        BlockInteractionHelper.PlaceResult l_Place;
        int l_I;
        ItemStack l_Stack;
        boolean l_IsSprinting;
        boolean l_IsSneaking;
        float l_Pitch;
        float l_Yaw;
        AxisAlignedBB axisalignedbb;
        double l_PosXDifference;
        double l_PosYDifference;
        double l_PosZDifference;
        double l_YawDifference;
        double l_RotationDifference;
        EntityPlayerSP player;
        boolean l_MovedXYZ;
        boolean l_MovedRotation;
        this.OnPlayerUpdate = new Listener<EventPlayerMotionUpdate>(p_Event -> {
            if (p_Event.getEra() != MinecraftEvent.Era.PRE) {
                return;
            }
            else if (!this.timer.passed(this.Delay.getValue() * 1000.0f)) {
                return;
            }
            else {
                this.timer.reset();
                pos = MathUtil.interpolateEntity((Entity)this.mc.player, this.mc.getRenderPartialTicks());
                orignPos = new BlockPos(pos.x, pos.y + 0.5, pos.z);
                interpPos = new BlockPos(pos.x, pos.y, pos.z).north().north();
                this.l_Array.clear();
                l_Pair = this.findStackHotbar();
                slot = -1;
                l_Offset = pos.y - orignPos.getY();
                if (l_Pair != null) {
                    slot = l_Pair.getFirst();
                    if (l_Pair.getSecond() instanceof BlockSlab && l_Offset == 0.5) {
                        orignPos = new BlockPos(pos.x, pos.y + 0.5, pos.z);
                        interpPos = new BlockPos(pos.x, pos.y + 1.0, pos.z).north().north();
                    }
                }
                switch (this.Mode.getValue()) {
                    case WallNorth: {
                        this.l_Array.add(orignPos.north().north().east().east());
                        this.l_Array.add(orignPos.north().north().east());
                        this.l_Array.add(orignPos.north().north());
                        this.l_Array.add(orignPos.north().north().west());
                        this.l_Array.add(orignPos.north().north().west().west());
                        this.l_Array.add(orignPos.north().north().west().west().west());
                        this.l_Array.add(orignPos.up().north().north().east().east());
                        this.l_Array.add(orignPos.up().north().north().east());
                        this.l_Array.add(orignPos.up().north().north());
                        this.l_Array.add(orignPos.up().north().north().west());
                        this.l_Array.add(orignPos.up().north().north().west().west());
                        this.l_Array.add(orignPos.up().north().north().west().west().west());
                        this.l_Array.add(orignPos.up().up().north().north().east().east());
                        this.l_Array.add(orignPos.up().up().north().north().east());
                        this.l_Array.add(orignPos.up().up().north().north());
                        this.l_Array.add(orignPos.up().up().north().north().west());
                        this.l_Array.add(orignPos.up().up().north().north().west().west());
                        this.l_Array.add(orignPos.up().up().north().north().west().west().west());
                        break;
                    }
                    case WallSouth: {
                        this.l_Array.add(orignPos.south().south().west().west());
                        this.l_Array.add(orignPos.south().south().west());
                        this.l_Array.add(orignPos.south().south());
                        this.l_Array.add(orignPos.south().south().east());
                        this.l_Array.add(orignPos.south().south().east().east());
                        this.l_Array.add(orignPos.south().south().east().east().east());
                        this.l_Array.add(orignPos.up().south().south().west().west());
                        this.l_Array.add(orignPos.up().south().south().west());
                        this.l_Array.add(orignPos.up().south().south());
                        this.l_Array.add(orignPos.up().south().south().east());
                        this.l_Array.add(orignPos.up().south().south().east().east());
                        this.l_Array.add(orignPos.up().south().south().east().east().east());
                        this.l_Array.add(orignPos.up().up().south().south().west().west());
                        this.l_Array.add(orignPos.up().up().south().south().west());
                        this.l_Array.add(orignPos.up().up().south().south());
                        this.l_Array.add(orignPos.up().up().south().south().east());
                        this.l_Array.add(orignPos.up().up().south().south().east().east());
                        this.l_Array.add(orignPos.up().up().south().south().east().east().east());
                        break;
                    }
                    case WallEast: {
                        this.l_Array.add(orignPos.east().east().south().south());
                        this.l_Array.add(orignPos.east().east().south());
                        this.l_Array.add(orignPos.east().east());
                        this.l_Array.add(orignPos.east().east().north());
                        this.l_Array.add(orignPos.east().east().north().north());
                        this.l_Array.add(orignPos.east().east().north().north().north());
                        this.l_Array.add(orignPos.up().east().east().south().south());
                        this.l_Array.add(orignPos.up().east().east().south());
                        this.l_Array.add(orignPos.up().east().east());
                        this.l_Array.add(orignPos.up().east().east().north());
                        this.l_Array.add(orignPos.up().east().east().north().north());
                        this.l_Array.add(orignPos.up().east().east().north().north().north());
                        this.l_Array.add(orignPos.up().up().east().east().south().south());
                        this.l_Array.add(orignPos.up().up().east().east().south());
                        this.l_Array.add(orignPos.up().up().east().east());
                        this.l_Array.add(orignPos.up().up().east().east().north());
                        this.l_Array.add(orignPos.up().up().east().east().north().north());
                        this.l_Array.add(orignPos.up().up().east().east().north().north().north());
                        break;
                    }
                    case WallWest: {
                        this.l_Array.add(orignPos.west().west().north().north());
                        this.l_Array.add(orignPos.west().west().north());
                        this.l_Array.add(orignPos.west().west());
                        this.l_Array.add(orignPos.west().west().south());
                        this.l_Array.add(orignPos.west().west().south().south());
                        this.l_Array.add(orignPos.west().west().south().south().south());
                        this.l_Array.add(orignPos.up().west().west().north().north());
                        this.l_Array.add(orignPos.up().west().west().north());
                        this.l_Array.add(orignPos.up().west().west());
                        this.l_Array.add(orignPos.up().west().west().south());
                        this.l_Array.add(orignPos.up().west().west().south().south());
                        this.l_Array.add(orignPos.up().west().west().south().south().south());
                        this.l_Array.add(orignPos.up().up().west().west().north().north());
                        this.l_Array.add(orignPos.up().up().west().west().north());
                        this.l_Array.add(orignPos.up().up().west().west());
                        this.l_Array.add(orignPos.up().up().west().west().south());
                        this.l_Array.add(orignPos.up().up().west().west().south().south());
                        this.l_Array.add(orignPos.up().up().west().west().south().south().south());
                        break;
                    }
                    case Highway: {
                        this.l_Array.add(orignPos.down());
                        this.l_Array.add(orignPos.down().north());
                        this.l_Array.add(orignPos.down().north().east());
                        this.l_Array.add(orignPos.down().north().west());
                        this.l_Array.add(orignPos.down().north().east().east());
                        this.l_Array.add(orignPos.down().north().west().west());
                        this.l_Array.add(orignPos.down().north().east().east().east());
                        this.l_Array.add(orignPos.down().north().west().west().west());
                        this.l_Array.add(orignPos.down().north().east().east().east().up());
                        this.l_Array.add(orignPos.down().north().west().west().west().up());
                        break;
                    }
                    case HighwayTunnel: {
                        this.l_Array.add(orignPos.down());
                        this.l_Array.add(orignPos.down().north());
                        this.l_Array.add(orignPos.down().north().east());
                        this.l_Array.add(orignPos.down().north().west());
                        this.l_Array.add(orignPos.down().north().east().east());
                        this.l_Array.add(orignPos.down().north().west().west());
                        this.l_Array.add(orignPos.down().north().east().east().east());
                        this.l_Array.add(orignPos.down().north().west().west().west());
                        this.l_Array.add(orignPos.down().north().east().east().east().up());
                        this.l_Array.add(orignPos.down().north().west().west().west().up());
                        this.l_Array.add(orignPos.down().north().east().east().east().up().up());
                        this.l_Array.add(orignPos.down().north().west().west().west().up().up());
                        this.l_Array.add(orignPos.down().north().east().east().east().up().up().up());
                        this.l_Array.add(orignPos.down().north().west().west().west().up().up().up());
                        this.l_Array.add(orignPos.down().north().east().east().east().up().up().up().up());
                        this.l_Array.add(orignPos.down().north().west().west().west().up().up().up().up());
                        this.l_Array.add(orignPos.down().north().east().east().east().up().up().up().up().west());
                        this.l_Array.add(orignPos.down().north().west().west().west().up().up().up().up().east());
                        this.l_Array.add(orignPos.down().north().east().east().east().up().up().up().up().west().west());
                        this.l_Array.add(orignPos.down().north().west().west().west().up().up().up().up().east().east());
                        this.l_Array.add(orignPos.down().north().east().east().east().up().up().up().up().west().west().west());
                        this.l_Array.add(orignPos.down().north().west().west().west().up().up().up().up().east().east().east());
                        break;
                    }
                    case Swastika: {
                        this.l_Array.add(interpPos);
                        this.l_Array.add(interpPos.west());
                        this.l_Array.add(interpPos.west().west());
                        this.l_Array.add(interpPos.up());
                        this.l_Array.add(interpPos.up().up());
                        this.l_Array.add(interpPos.up().up().west());
                        this.l_Array.add(interpPos.up().up().west().west());
                        this.l_Array.add(interpPos.up().up().west().west().up());
                        this.l_Array.add(interpPos.up().up().west().west().up().up());
                        this.l_Array.add(interpPos.up().up().east());
                        this.l_Array.add(interpPos.up().up().east().east());
                        this.l_Array.add(interpPos.up().up().east().east().down());
                        this.l_Array.add(interpPos.up().up().east().east().down().down());
                        this.l_Array.add(interpPos.up().up().up());
                        this.l_Array.add(interpPos.up().up().up().up());
                        this.l_Array.add(interpPos.up().up().up().up().east());
                        this.l_Array.add(interpPos.up().up().up().up().east().east());
                        break;
                    }
                    case Portal: {
                        this.l_Array.add(interpPos.east());
                        this.l_Array.add(interpPos.east().east());
                        this.l_Array.add(interpPos);
                        this.l_Array.add(interpPos.east().east().up());
                        this.l_Array.add(interpPos.east().east().up().up());
                        this.l_Array.add(interpPos.east().east().up().up().up());
                        this.l_Array.add(interpPos.east().east().up().up().up().up());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west().west());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west().west().west());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west().west().west().down());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west().west().west().down().down());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west().west().west().down().down().down());
                        this.l_Array.add(interpPos.east().east().up().up().up().up().west().west().west().down().down().down().down());
                        break;
                    }
                    case Flat: {
                        for (l_X = -3; l_X < 3; ++l_X) {
                            for (l_Y = -3; l_Y < 3; ++l_Y) {
                                this.l_Array.add(orignPos.down().add(l_X, 0, l_Y));
                            }
                        }
                        break;
                    }
                    case Cover: {
                        if (l_Pair == null) {
                            return;
                        }
                        else {
                            for (l_X2 = -3; l_X2 < 3; ++l_X2) {
                                for (l_Y2 = -3; l_Y2 < 3; ++l_Y2) {
                                    l_Tries = 5;
                                    l_Pos = orignPos.down().add(l_X2, 0, l_Y2);
                                    if (this.mc.world.getBlockState(l_Pos).getBlock() != l_Pair.getSecond() && this.mc.world.getBlockState(l_Pos.down()).getBlock() != Blocks.AIR) {
                                        if (this.mc.world.getBlockState(l_Pos.down()).getBlock() != l_Pair.getSecond()) {
                                            while (this.mc.world.getBlockState(l_Pos).getBlock() != Blocks.AIR) {
                                                if (this.mc.world.getBlockState(l_Pos).getBlock() == l_Pair.getSecond()) {
                                                    break;
                                                }
                                                else {
                                                    l_Pos = l_Pos.up();
                                                    if (--l_Tries <= 0) {
                                                        break;
                                                    }
                                                    else {
                                                        continue;
                                                    }
                                                }
                                            }
                                            this.l_Array.add(l_Pos);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        break;
                    }
                    case Tower: {
                        this.l_Array.add(orignPos.up());
                        this.l_Array.add(orignPos);
                        this.l_Array.add(orignPos.down());
                        break;
                    }
                }
                l_NeedPlace = false;
                rotations = null;
                if (slot != -1 && this.mc.player.onGround) {
                    lastSlot = this.mc.player.inventory.currentItem;
                    this.mc.player.inventory.currentItem = slot;
                    this.mc.playerController.updateController();
                    l_BlocksPerTick = this.BlocksPerTick.getValue();
                    this.l_Array.iterator();
                    while (iterator.hasNext()) {
                        l_Pos2 = iterator.next();
                        l_Place = BlockInteractionHelper.place(l_Pos2, 5.0f, false, l_Offset == -0.5);
                        if (l_Place != BlockInteractionHelper.PlaceResult.Placed) {
                            continue;
                        }
                        else {
                            l_NeedPlace = true;
                            rotations = BlockInteractionHelper.getLegitRotations(new Vec3d((double)l_Pos2.getX(), (double)l_Pos2.getY(), (double)l_Pos2.getZ()));
                            if (--l_BlocksPerTick <= 0) {
                                break;
                            }
                            else {
                                continue;
                            }
                        }
                    }
                    if (!this.slotEqualsBlock(lastSlot, l_Pair.getSecond())) {
                        this.mc.player.inventory.currentItem = lastSlot;
                    }
                    this.mc.playerController.updateController();
                }
                if (!l_NeedPlace && this.Mode.getValue() == Modes.Portal) {
                    if (this.mc.world.getBlockState(this.l_Array.get(0).up()).getBlock() == Blocks.PORTAL) {
                        return;
                    }
                    else {
                        for (l_I = 0; l_I < 9; ++l_I) {
                            l_Stack = this.mc.player.inventory.getStackInSlot(l_I);
                            if (!l_Stack.isEmpty()) {
                                if (l_Stack.getItem() == Items.FLINT_AND_STEEL) {
                                    this.mc.player.inventory.currentItem = l_I;
                                    this.mc.playerController.updateController();
                                    break;
                                }
                            }
                        }
                        if (this.SentPacket) {
                            this.mc.getConnection().sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock((BlockPos)this.l_Array.get(0), EnumFacing.UP, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        }
                        rotations = BlockInteractionHelper.getLegitRotations(new Vec3d((double)this.l_Array.get(0).getX(), (double)this.l_Array.get(0).getY(), (double)this.l_Array.get(0).getZ()));
                        l_NeedPlace = true;
                    }
                }
                if (!this.rotate.getValue() || !l_NeedPlace || rotations == null) {
                    this.PitchHead = -420.0f;
                    this.SentPacket = false;
                    return;
                }
                else {
                    p_Event.cancel();
                    l_IsSprinting = this.mc.player.isSprinting();
                    if (l_IsSprinting != this.mc.player.serverSprintState) {
                        if (l_IsSprinting) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.mc.player, CPacketEntityAction.Action.START_SPRINTING));
                        }
                        else {
                            this.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        }
                        this.mc.player.serverSprintState = l_IsSprinting;
                    }
                    l_IsSneaking = this.mc.player.isSneaking();
                    if (l_IsSneaking != this.mc.player.serverSneakState) {
                        if (l_IsSneaking) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        }
                        else {
                            this.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)this.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                        }
                        this.mc.player.serverSneakState = l_IsSneaking;
                    }
                    if (PlayerUtil.isCurrentViewEntity()) {
                        l_Pitch = rotations[1];
                        l_Yaw = rotations[0];
                        this.mc.player.rotationYawHead = l_Yaw;
                        this.PitchHead = l_Pitch;
                        axisalignedbb = this.mc.player.getEntityBoundingBox();
                        l_PosXDifference = this.mc.player.posX - this.mc.player.lastReportedPosX;
                        l_PosYDifference = axisalignedbb.minY - this.mc.player.lastReportedPosY;
                        l_PosZDifference = this.mc.player.posZ - this.mc.player.lastReportedPosZ;
                        l_YawDifference = l_Yaw - this.mc.player.lastReportedYaw;
                        l_RotationDifference = l_Pitch - this.mc.player.lastReportedPitch;
                        player = this.mc.player;
                        ++player.positionUpdateTicks;
                        l_MovedXYZ = (l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4 || this.mc.player.positionUpdateTicks >= 20);
                        l_MovedRotation = (l_YawDifference != 0.0 || l_RotationDifference != 0.0);
                        if (this.mc.player.isRiding()) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(this.mc.player.motionX, -999.0, this.mc.player.motionZ, l_Yaw, l_Pitch, this.mc.player.onGround));
                            l_MovedXYZ = false;
                        }
                        else if (l_MovedXYZ && l_MovedRotation) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketPlayer.PositionRotation(this.mc.player.posX, axisalignedbb.minY, this.mc.player.posZ, l_Yaw, l_Pitch, this.mc.player.onGround));
                        }
                        else if (l_MovedXYZ) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Position(this.mc.player.posX, axisalignedbb.minY, this.mc.player.posZ, this.mc.player.onGround));
                        }
                        else if (l_MovedRotation) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(l_Yaw, l_Pitch, this.mc.player.onGround));
                        }
                        else if (this.mc.player.prevOnGround != this.mc.player.onGround) {
                            this.mc.player.connection.sendPacket((Packet)new CPacketPlayer(this.mc.player.onGround));
                        }
                        if (l_MovedXYZ) {
                            this.mc.player.lastReportedPosX = this.mc.player.posX;
                            this.mc.player.lastReportedPosY = axisalignedbb.minY;
                            this.mc.player.lastReportedPosZ = this.mc.player.posZ;
                            this.mc.player.positionUpdateTicks = 0;
                        }
                        if (l_MovedRotation) {
                            this.mc.player.lastReportedYaw = l_Yaw;
                            this.mc.player.lastReportedPitch = l_Pitch;
                        }
                        this.SentPacket = true;
                        this.mc.player.prevOnGround = this.mc.player.onGround;
                        this.mc.player.autoJumpEnabled = this.mc.player.mc.gameSettings.autoJump;
                    }
                    return;
                }
            }
        }, (Predicate<EventPlayerMotionUpdate>[])new Predicate[0]);
        final Iterator l_Itr;
        BlockPos l_Pos3;
        AxisAlignedBB bb;
        double dist;
        float alpha;
        int l_Color;
        this.OnRenderEvent = new Listener<RenderEvent>(p_Event -> {
            l_Itr = this.l_Array.iterator();
            while (l_Itr.hasNext()) {
                l_Pos3 = l_Itr.next();
                bb = new AxisAlignedBB(l_Pos3.getX() - this.mc.getRenderManager().viewerPosX, l_Pos3.getY() - this.mc.getRenderManager().viewerPosY, l_Pos3.getZ() - this.mc.getRenderManager().viewerPosZ, l_Pos3.getX() + 1 - this.mc.getRenderManager().viewerPosX, l_Pos3.getY() + 1 - this.mc.getRenderManager().viewerPosY, l_Pos3.getZ() + 1 - this.mc.getRenderManager().viewerPosZ);
                this.camera.setPosition(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ);
                if (this.camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + this.mc.getRenderManager().viewerPosX, bb.minY + this.mc.getRenderManager().viewerPosY, bb.minZ + this.mc.getRenderManager().viewerPosZ, bb.maxX + this.mc.getRenderManager().viewerPosX, bb.maxY + this.mc.getRenderManager().viewerPosY, bb.maxZ + this.mc.getRenderManager().viewerPosZ))) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.5f);
                    dist = this.mc.player.getDistance((double)(l_Pos3.getX() + 0.5f), (double)(l_Pos3.getY() + 0.5f), (double)(l_Pos3.getZ() + 0.5f)) * 0.75;
                    alpha = MathUtil.clamp((float)(dist * 255.0 / 5.0 / 255.0), 0.0f, 0.3f);
                    l_Color = 268500991;
                    RenderUtil.drawBoundingBox(bb, 1.0f, l_Color);
                    RenderUtil.drawFilledBox(bb, l_Color);
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        }, (Predicate<RenderEvent>[])new Predicate[0]);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        if (this.mc.player == null) {
            this.toggle();
            return;
        }
        this.timer.reset();
    }
    
    @Override
    public String getMetaData() {
        return this.Mode.getValue().toString();
    }
    
    private boolean slotEqualsBlock(final int slot, final Block type) {
        if (this.mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock)this.mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }
        return false;
    }
    
    private Pair<Integer, Block> findStackHotbar() {
        if (this.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            return new Pair<Integer, Block>(this.mc.player.inventory.currentItem, ((ItemBlock)this.mc.player.getHeldItemMainhand().getItem()).getBlock());
        }
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                final ItemBlock block = (ItemBlock)stack.getItem();
                return new Pair<Integer, Block>(i, block.getBlock());
            }
        }
        return null;
    }
    
    public Vec3d GetCenter(final double posX, final double posY, final double posZ) {
        final double x = Math.floor(posX) + 0.5;
        final double y = Math.floor(posY);
        final double z = Math.floor(posZ) + 0.5;
        return new Vec3d(x, y, z);
    }
    
    public enum Modes
    {
        Highway, 
        Swastika, 
        HighwayTunnel, 
        Portal, 
        Flat, 
        Tower, 
        Cover, 
        WallNorth, 
        WallSouth, 
        WallEast, 
        WallWest;
    }
}
