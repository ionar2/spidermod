package me.ionar.salhack.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.EnumHand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.*;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer
{
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP)
    {
        // if (ModuleManager.isModuleEnabled("PortalChat"))
        // return;
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen)
    {
        // if (ModuleManager.isModuleEnabled("PortalChat"))
        // return;
    }

    //  public void move(MoverType type, double x, double y, double z)
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo p_Info)
    {
        EventPlayerMove event = new EventPlayerMove(type, x, y, z);
        SalHackMod.EVENT_BUS.post(event);
        if (event.isCancelled())
        {
            super.move(type, event.X, event.Y, event.Z);
            p_Info.cancel();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void OnPreUpdateWalkingPlayer(CallbackInfo p_Info)
    {
        EventPlayerMotionUpdate l_Event = new EventPlayerMotionUpdate(Era.PRE);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"), cancellable = true)
    public void OnPostUpdateWalkingPlayer(CallbackInfo p_Info)
    {
        EventPlayerMotionUpdate l_Event = new EventPlayerMotionUpdate(Era.POST);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo p_Info)
    {
        EventPlayerUpdate l_Event = new EventPlayerUpdate();
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    public void swingArm(EnumHand p_Hand, CallbackInfo p_Info)
    {
        EventPlayerSwingArm l_Event = new EventPlayerSwingArm(p_Hand);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Inject(method = "pushOutOfBlocks(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfo)
    {
        EventPlayerPushOutOfBlocks l_Event = new EventPlayerPushOutOfBlocks(x, y, z);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            callbackInfo.setReturnValue(false);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void swingArm(String p_Message, CallbackInfo p_Info)
    {
        EventPlayerSendChatMessage l_Event = new EventPlayerSendChatMessage(p_Message);
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
            p_Info.cancel();
    }

    @Override
    public void jump()
    {
        try
        {
            double l_MotionX = motionX;
            double l_MotionZ = motionZ;
            super.jump();
            SalHackMod.EVENT_BUS.post(new EventPlayerJump(l_MotionX, l_MotionZ));
        }
        catch (Exception v3)
        {
            v3.printStackTrace();
        }
    }
}
