package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.render.EventRenderTooltip;

@Mixin(GuiScreen.class)
public class MixinGuiScreen
{
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void renderToolTip(ItemStack stack, int x, int y, CallbackInfo p_Info)
    {
        EventRenderTooltip l_Event = new EventRenderTooltip(stack, x, y);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }
}
