package me.ionar.salhack.module.movement;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.player.EventPlayerTravel;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class ElytraFlyModule extends Module
{
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
    { "Mode", "M" }, "Mode to use for 2b2t flight.", Mode.Normal);
    public final Value<Float> speed = new Value<Float>("Speed", new String[]
    { "Spd" }, "Speed multiplier for flight, higher values equals more speed. - 2b speed recommended is 1.8~", 1.82f, 0.0f, 10.0f, 0.1f);
    public final Value<Float> DownSpeed = new Value<Float>("DownSpeed", new String[]
    { "DS" }, "DownSpeed multiplier for flight, higher values equals more speed.", 1.82f, 0.0f, 10.0f, 0.1f);
    public final Value<Float> GlideSpeed = new Value<Float>("GlideSpeed", new String[]
    { "GlideSpeed" }, "Glide value for acceleration, this is divided by 10000.", 1f, 0f, 10f, 1f);
    public final Value<Float> UpSpeed = new Value<Float>("UpSpeed", new String[]
    { "UpSpeed" }, "Up speed for elytra.", 2.0f, 0f, 10f, 1f);
    public final Value<Boolean> Accelerate = new Value<Boolean>("Accelerate", new String[]
    { "Accelerate", "Accelerate" }, "Auto accelerates when going up", true);
    public final Value<Integer> vAccelerationTimer = new Value<Integer>("Timer", new String[]
    { "AT" }, "Acceleration timer, default 1000", 1000, 0, 10000, 1000);
    public final Value<Float> RotationPitch = new Value<Float>("RotationPitch", new String[]
    { "RP" }, "RotationPitch default 0.0, this is for going up, -90 is lowest you can face, 90 is highest", 0.0f, -90f, 90f, 10.0f);
    public final Value<Boolean> CancelInWater = new Value<Boolean>("CancelInWater", new String[]
    { "CiW" }, "Cancel in water, anticheat will flag you if you try to go up in water, accelerating will still work.", true);
    public final Value<Integer> CancelAtHeight = new Value<Integer>("CancelAtHeight", new String[]
    { "CAH" }, "Doesn't allow flight Y is below, or if too close to bedrock. since 2b anticheat is wierd", 5, 0, 10, 1);
    public final Value<Boolean> InstantFly = new Value<Boolean>("InstantFly", new String[]
    { "IF" }, "Sends the fall flying packet when your off ground", true);
    public final Value<Boolean> EquipElytra = new Value<Boolean>("EquipElytra", new String[] {"EE"}, "Equips your elytra when enabled if you're not already wearing one", false);

    private Timer PacketTimer = new Timer();
    private Timer AccelerationTimer = new Timer();
    private Timer AccelerationResetTimer = new Timer();
    private Timer InstantFlyTimer = new Timer();
    private boolean SendMessage = false;

    private enum Mode
    {
        Normal, Tarzan, Superior, Packet
    }

    public ElytraFlyModule()
    {
        super("ElytraFly", new String[]
        { "ElytraFly2b2t" }, "Allows you to fly with elytra on 2b2t", "P", 0x24DB26, ModuleType.MOVEMENT);
    }
    
    private int ElytraSlot = -1;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        ElytraSlot = -1;
        
        if (EquipElytra.getValue())
        {
            if (mc.player != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            {
                for (int l_I = 0; l_I < 44; ++l_I)
                {
                    ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                    
                    if (l_Stack.isEmpty() || l_Stack.getItem() != Items.ELYTRA)
                        continue;
                    
                    ItemElytra l_Elytra = (ItemElytra)l_Stack.getItem();
                    
                    ElytraSlot = l_I;
                    break;
                }
                
                if (ElytraSlot != -1)
                {
                    boolean l_HasArmorAtChest = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.AIR;
                    
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                    
                    if (l_HasArmorAtChest)
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                }
            }
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        
        if (mc.player == null)
            return;
        
        if (ElytraSlot != -1)
        {
            boolean l_HasItem = !mc.player.inventory.getStackInSlot(ElytraSlot).isEmpty() || mc.player.inventory.getStackInSlot(ElytraSlot).getItem() != Items.AIR;
            
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
            
            if (l_HasItem)
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
        }
    }
    
    @Override
    public String getMetaData()
    {
        return this.mode.getValue().name();
    }

    @EventHandler
    private Listener<EventPlayerTravel> OnTravel = new Listener<>(p_Event ->
    {
        if (mc.player == null)
            return;

        /// Player must be wearing an elytra.
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        if (!mc.player.isElytraFlying())
        {
            if (!mc.player.onGround && InstantFly.getValue())
            {
                if (!InstantFlyTimer.passed(1000))
                    return;

                InstantFlyTimer.reset();

                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
            }

            return;
        }

        switch (mode.getValue())
        {
            case Normal:
            case Tarzan:
            case Packet:
                HandleNormalModeElytra(p_Event);
                break;
            case Superior:
                HandleImmediateModeElytra(p_Event);
                break;
            default:
                break;
        }
    });

    public void HandleNormalModeElytra(EventPlayerTravel p_Travel)
    {
        double l_YHeight = mc.player.posY;

        if (l_YHeight <= CancelAtHeight.getValue())
        {
            if (!SendMessage)
            {
                SalHack.SendMessage(ChatFormatting.RED + "WARNING, you must scaffold up or use fireworks, as YHeight <= CancelAtHeight!");
                SendMessage = true;
            }

            return;
        }

        boolean l_IsMoveKeyDown = mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()
                || mc.gameSettings.keyBindBack.isKeyDown();

        boolean l_CancelInWater = !mc.player.isInWater() && !mc.player.isInLava() && CancelInWater.getValue();

        if (!l_IsMoveKeyDown)
        {
            AccelerationTimer.resetTimeSkipTo(-vAccelerationTimer.getValue());
        }
        else if ((mc.player.rotationPitch <= RotationPitch.getValue() || mode.getValue() == Mode.Tarzan) && l_CancelInWater)
        {
            if (Accelerate.getValue())
            {
                if (AccelerationTimer.passed(vAccelerationTimer.getValue()))
                {
                    Accelerate();
                    return;
                }
            }
            return;
        }

        p_Travel.cancel();
        Accelerate();
    }

    public void HandleImmediateModeElytra(EventPlayerTravel p_Travel)
    {
        p_Travel.cancel();

        boolean moveForward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean moveBackward = mc.gameSettings.keyBindBack.isKeyDown();
        boolean moveLeft = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean moveRight = mc.gameSettings.keyBindRight.isKeyDown();
        boolean moveUp = mc.gameSettings.keyBindJump.isKeyDown();
        boolean moveDown = mc.gameSettings.keyBindSneak.isKeyDown();
        float moveForwardFactor = moveForward ? 1.0f : (float) (moveBackward ? -1 : 0);
        float yawDeg = mc.player.rotationYaw;

        if (moveLeft && (moveForward || moveBackward))
        {
            yawDeg -= 40.0f * moveForwardFactor;
        }
        else if (moveRight && (moveForward || moveBackward))
        {
            yawDeg += 40.0f * moveForwardFactor;
        }
        else if (moveLeft)
        {
            yawDeg -= 90.0f;
        }
        else if (moveRight)
        {
            yawDeg += 90.0f;
        }
        if (moveBackward)
            yawDeg -= 180.0f;

        float yaw = (float) Math.toRadians(yawDeg);
        double motionAmount = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
        if (moveUp || moveForward || moveBackward || moveLeft || moveRight)
        {
            if ((moveUp) && motionAmount > 1.0)
            {
                if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0)
                {
                    mc.player.motionY = UpSpeed.getValue();
                }
                else
                {
                    double calcMotionDiff = motionAmount * 0.008;
                    mc.player.motionY += calcMotionDiff * 3.2;
                    mc.player.motionX -= (double) (-MathHelper.sin(yaw)) * calcMotionDiff / 1.0;
                    mc.player.motionZ -= (double) MathHelper.cos(yaw) * calcMotionDiff / 1.0;
                    mc.player.motionX *= 0.99f;
                    mc.player.motionY *= 0.98f;
                    mc.player.motionZ *= 0.99f;
                }
            }
            else
            { /* runs when pressing wasd */
                mc.player.motionX = (double) (-MathHelper.sin(yaw)) * 1.8f;
                mc.player.motionY = -(GlideSpeed.getValue() / 10000f);
                mc.player.motionZ = (double) MathHelper.cos(yaw) * 1.8f;
            }
        }
        else
        { /* Stop moving if no inputs are pressed */
            mc.player.motionX = 0.0;
            mc.player.motionY = 0.0;
            mc.player.motionZ = 0.0;
        }
        if (moveDown)
        {
            mc.player.motionY = -DownSpeed.getValue();
        }
        if (moveUp || moveDown)
        {
            // hoverTarget = mc.player.posY;
        }
    }

    public void Accelerate()
    {
        if (AccelerationResetTimer.passed(vAccelerationTimer.getValue()))
        {
            AccelerationResetTimer.reset();
            AccelerationTimer.reset();
            SendMessage = false;
        }

        float l_Speed = this.speed.getValue();

        final double[] dir = MathUtil.directionSpeed(l_Speed);

        mc.player.motionY = -(GlideSpeed.getValue() / 10000f);

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0)
        {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];
        }
        else
        {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown())
            mc.player.motionY = -DownSpeed.getValue();

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    public void SetupStashFinder()
    {
        SalHack.SendMessage(ChatFormatting.AQUA + "[ElytraFly]: " + ChatFormatting.LIGHT_PURPLE + " Preparing ElytraFly for stash finder");
        mode.setValue(Mode.Normal);
        speed.setValue(1.8f);
    }
}
