package me.ionar.salhack.mixin.client;

import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.*;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.events.render.EventRenderGetEntitiesINAABBexcluding;
import me.ionar.salhack.events.render.EventRenderGetFOVModifier;
import me.ionar.salhack.events.render.EventRenderHand;
import me.ionar.salhack.events.render.EventRenderHurtCameraEffect;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.render.EventRenderUpdateLightmap;
import me.ionar.salhack.util.render.RenderUtil;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer
{
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo p_Info)
    {
        EventRenderSetupFog l_Event = new EventRenderSetupFog(startCoords, partialTicks);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate)
    {
        EventRenderGetEntitiesINAABBexcluding l_Event = new EventRenderGetEntitiesINAABBexcluding(worldClient, entityIn, boundingBox, predicate);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            return new ArrayList<>();
        else
            return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(float partialTicks, int pass, CallbackInfo p_Info)
    {
        EventRenderHand l_Event = new EventRenderHand(partialTicks, pass);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info)
    {
        EventRenderHurtCameraEffect l_Event = new EventRenderHurtCameraEffect(ticks);
        
        SalHackMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
            info.cancel();
    }

    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    private void updateLightmap(float partialTicks, CallbackInfo p_Info)
    {
        EventRenderUpdateLightmap l_Event = new EventRenderUpdateLightmap(partialTicks);
        
        SalHackMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.AFTER))
    private void renderWorldPassPost(int pass, float partialTicks, long finishTimeNano, CallbackInfo callbackInfo)
    {
        RenderUtil.updateModelViewProjectionMatrix();
    }
}
