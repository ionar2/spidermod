package me.ionar.salhack.mixin.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.potion.Potion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.blocks.EventCanCollideCheck;
import me.ionar.salhack.events.player.*;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity
{
    public MixinEntityLivingBase()
    {
        super();
    }

    @Shadow
    public void jump()
    {
    }

    @Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
    public void isPotionActive(Potion potionIn, final CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        EventPlayerIsPotionActive l_Event = new EventPlayerIsPotionActive(potionIn);
        SalHackMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
            callbackInfoReturnable.setReturnValue(false);
    }
}
