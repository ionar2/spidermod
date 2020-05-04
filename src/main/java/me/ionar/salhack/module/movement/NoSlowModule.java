package me.ionar.salhack.module.movement;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.player.EventPlayerIsKeyPressed;
import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShield;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputUpdateEvent;

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
    private Listener<EventPlayerIsKeyPressed> OnIsKeyPressed = new Listener<>(p_Event ->
    {
        if (InventoryMove.getValue())
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

            KeyBinding[] moveKeys = new KeyBinding[]
            { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint,
                    mc.gameSettings.keyBindSneak };
            if (mc.currentScreen != null)// && !(mc.currentScreen instanceof GuiAnvil))
            {
                for (KeyBinding bind : moveKeys)
                {
                    KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));

                    if (p_Event.Keybind == bind)
                    {
                        if (Keyboard.isKeyDown(bind.getKeyCode()))
                        {
                            p_Event.IsKeyPressed = Keyboard.isKeyDown(bind.getKeyCode());
                            p_Event.cancel();
                            break;
                        }
                    }
                }
            }
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

        if (this.ice.getValue())
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
}
