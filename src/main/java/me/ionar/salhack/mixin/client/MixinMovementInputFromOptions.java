package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerIsKeyPressed;
import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import me.ionar.salhack.main.Wrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInputFromOptions;

@Mixin(value = MovementInputFromOptions.class)
public class MixinMovementInputFromOptions
{
    @Inject(method = "updatePlayerMoveState", at = @At("RETURN"), cancellable = true)
    public void updatePlayerMoveStateReturn(CallbackInfo callback)
    {
        EventPlayerUpdateMoveState l_Event = new EventPlayerUpdateMoveState();
        SalHackMod.EVENT_BUS.post(l_Event);
        if (l_Event.isCancelled())
        {
            callback.cancel();
        }
    }

    @Redirect(method = "updatePlayerMoveState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    public boolean isKeyPressed(KeyBinding keyBinding)
    {
        EventPlayerIsKeyPressed l_Event = new EventPlayerIsKeyPressed(keyBinding);
        
        SalHackMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
        {
            return l_Event.IsKeyPressed;
        }
        
        return keyBinding.isKeyDown();
    }
}
