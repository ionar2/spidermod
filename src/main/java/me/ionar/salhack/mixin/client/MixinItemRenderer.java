package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.render.EventRenderUpdateEquippedItem;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer
{
    @Inject(method = "updateEquippedItem", at = @At("HEAD"), cancellable = true)
    public void updateEquippedItem(CallbackInfo p_Info)
    {
        EventRenderUpdateEquippedItem l_Event = new EventRenderUpdateEquippedItem();
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }
}
