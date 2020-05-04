package me.ionar.salhack.mixin.client;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.blocks.EventCanCollideCheck;
import me.ionar.salhack.events.liquid.EventLiquidCollisionBB;
import me.ionar.salhack.events.render.EventRenderSign;

@Mixin(TileEntitySignRenderer.class)
public class MixinTileEntitySignRenderer
{
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo p_Info)
    {
        EventRenderSign l_Event = new EventRenderSign();
        SalHackMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
        {
           // destroyStage = 0;
            p_Info.cancel();
        }
    }
}
