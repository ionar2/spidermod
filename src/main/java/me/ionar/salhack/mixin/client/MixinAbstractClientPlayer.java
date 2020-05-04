package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerGetLocationSkin;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.render.EventRenderTooltip;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends MixinEntityPlayer
{
    public MixinAbstractClientPlayer()
    {
        super();
    }

    @Inject(method = "getLocationSkin", at = @At("HEAD"), cancellable = true)
    public void getLocationSkin(CallbackInfoReturnable<ResourceLocation> p_Callback)
    {
        EventPlayerGetLocationSkin l_Event = new EventPlayerGetLocationSkin();
        SalHackMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
        {
            p_Callback.cancel();
            p_Callback.setReturnValue(l_Event.GetResourceLocation());
        }
    }
}
