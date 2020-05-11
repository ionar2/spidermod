package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo
{
    /*@Inject(method = "updateRenderInfo", at = @At("RETURN"))
    private static void updateRenderInfo(EntityPlayer entityplayerIn, boolean p_74583_1_, CallbackInfo info)
    {
        RenderUtil.updateModelViewProjectionMatrix();
    }*/
}
