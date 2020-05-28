package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.render.SkeletonModule;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer
{
    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo callbackInfo)
    {
        if (Wrapper.GetMC().world != null && Wrapper.GetMC().player != null && entityIn instanceof EntityPlayer)
        {
            SkeletonModule.addEntity((EntityPlayer)entityIn, (ModelPlayer) (Object) this);
        }
    }
}
