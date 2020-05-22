package me.ionar.salhack.module.movement;

import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.network.EventNetworkPostPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShield;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;

public final class NoSlowModule extends Module
{
    public final Value<Boolean> InventoryMove = new Value<Boolean>("InventoryMove", new String[]
    { "InvMove", "InventoryMove", "GUIMove" }, "Allows you to move while guis are open", true);
    public final Value<Boolean> OnlyOnCustom = new Value<Boolean>("OnlyOnCustom", new String[]
    { "Custom" }, "Only inventory move on custom GUIs", true);
    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]
    { "it" }, "Disables the slowness from using items (shields, eating, etc).", true);
    public final Value<Boolean> ice = new Value<Boolean>("Ice", new String[]
    { "ic" }, "Disables slowness from walking on ice.", true);
    public final Value<Boolean> NCPStrict = new Value<Boolean>("NCPStrict", new String[]{"NCP"}, "Allows NoSlow to work on nocheatplus", true);

    public NoSlowModule()
    {
        super("NoSlow", new String[]
        { "AntiSlow", "NoSlowdown", "AntiSlowdown" }, "Allows you to move faster with things that slow you down", "NONE", 0x2460DB, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        Blocks.ICE.setDefaultSlipperiness(0.98f);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f);
        Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f);
    }

    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnIsKeyPressed = new Listener<>(event ->
    {
        if (InventoryMove.getValue() && mc.currentScreen != null)
        {
            if (OnlyOnCustom.getValue())
            {
                if (!(mc.currentScreen instanceof SalGuiScreen))
                    return;
            }

            if (!(mc.currentScreen instanceof SalGuiScreen))
            {
                if (Keyboard.isKeyDown(200))
                {
                    SalGuiScreen.UpdateRotationPitch(-2.0f);
                }
                if (Keyboard.isKeyDown(208))
                {
                    SalGuiScreen.UpdateRotationPitch(2.0f);
                }
                if (Keyboard.isKeyDown(205))
                {
                    SalGuiScreen.UpdateRotationYaw(2.0f);
                }

                if (Keyboard.isKeyDown(203))
                {
                    SalGuiScreen.UpdateRotationYaw(-2.0f);
                }
            }

            mc.player.movementInput.moveStrafe = 0.0F;
            mc.player.movementInput.moveForward = 0.0F;
            
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()))
            {
                ++mc.player.movementInput.moveForward;
                mc.player.movementInput.forwardKeyDown = true;
            }
            else
            {
                mc.player.movementInput.forwardKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()))
            {
                --mc.player.movementInput.moveForward;
                mc.player.movementInput.backKeyDown = true;
            }
            else
            {
                mc.player.movementInput.backKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()))
            {
                ++mc.player.movementInput.moveStrafe;
                mc.player.movementInput.leftKeyDown = true;
            }
            else
            {
                mc.player.movementInput.leftKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()))
            {
                --mc.player.movementInput.moveStrafe;
                mc.player.movementInput.rightKeyDown = true;
            }
            else
            {
                mc.player.movementInput.rightKeyDown = false;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
            mc.player.movementInput.jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        }
    });

    @EventHandler
    private Listener<EventClientTick> OnTick = new Listener<>(p_Event ->
    {
        if (mc.player.isHandActive())
        {
            if (mc.player.getHeldItem(mc.player.getActiveHand()).getItem() instanceof ItemShield)
            {
                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0 && mc.player.getItemInUseMaxCount() >= 8)
                {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                }
            }
        }

        if (ice.getValue())
        {
            if (mc.player.getRidingEntity() != null)
            {
                Blocks.ICE.setDefaultSlipperiness(0.98f);
                Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f);
                Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f);
            }
            else
            {
                Blocks.ICE.setDefaultSlipperiness(0.45f);
                Blocks.FROSTED_ICE.setDefaultSlipperiness(0.45f);
                Blocks.PACKED_ICE.setDefaultSlipperiness(0.45f);
            }
        }

    });

    @EventHandler
    private Listener<EventPlayerUpdateMoveState> OnUpdateMoveState = new Listener<>(event ->
    {
        if (items.getValue() && mc.player.isHandActive() && !mc.player.isRiding())
        {
            mc.player.movementInput.moveForward /= 0.2F;
            mc.player.movementInput.moveStrafe /= 0.2F;
        }
    });

    @EventHandler
    private Listener<EventNetworkPostPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof CPacketPlayer)
        {
            if (NCPStrict.getValue())
            {
                if (items.getValue() && mc.player.isHandActive() && !mc.player.isRiding())
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, PlayerUtil.GetLocalPlayerPosFloored(), EnumFacing.DOWN));
            }
        }
    });
}
