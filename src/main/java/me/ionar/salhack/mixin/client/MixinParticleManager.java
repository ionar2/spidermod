package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.particles.EventParticleEmitParticleAtEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;

@Mixin(ParticleManager.class)
public class MixinParticleManager
{
    /*@Inject(method = "emitParticleAtEntity", at = @At("HEAD"), cancellable = true)
    public void emitParticleAtEntity(Entity p_Entity, EnumParticleTypes p_Type, int p_Amount, CallbackInfo p_Info)
    {
        EventParticleEmitParticleAtEntity l_Event = new EventParticleEmitParticleAtEntity(p_Entity, p_Type, p_Amount);
        
        SalHackMod.EVENT_BUS.post(l_Event);
        
        if (l_Event.isCancelled())
            l_Event.cancel();
    }*/
}
