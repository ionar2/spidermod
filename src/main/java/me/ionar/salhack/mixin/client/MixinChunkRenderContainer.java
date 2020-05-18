package me.ionar.salhack.mixin.client;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderChunkContainer;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkRenderContainer.class)
public class MixinChunkRenderContainer {
    @Inject(method = "preRenderChunk", at = @At(value = "RETURN", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;getPosition()Lnet/minecraft/util/math/BlockPos/MutableBlockPos;"))
    private void preRenderChunk(RenderChunk renderChunk, CallbackInfo callbackInfo) {
        SalHackMod.EVENT_BUS.post(new EventRenderChunkContainer(renderChunk));
    }
}
