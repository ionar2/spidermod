package me.ionar.salhack.mixin.client;

import me.ionar.salhack.events.world.EventWorldSetBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderRainStrength;
import net.minecraft.world.World;

@Mixin(World.class)
public class MixinWorld
{
    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    public void getRainStrength(float delta, CallbackInfoReturnable<Float> p_Callback)
    {
        EventRenderRainStrength l_Event = new EventRenderRainStrength();
        
        SalHackMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
        {
            p_Callback.cancel();
            p_Callback.setReturnValue(0.0f);
        }
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    public void setBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> p_CallBack)
    {
        EventWorldSetBlockState l_Event = new EventWorldSetBlockState(pos, newState, flags);

        SalHackMod.EVENT_BUS.post(l_Event);

        if (l_Event.isCancelled())
        {
            p_CallBack.cancel();
            p_CallBack.setReturnValue(false);
        }
    }
}
