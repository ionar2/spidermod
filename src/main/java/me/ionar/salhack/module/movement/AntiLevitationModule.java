package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerIsPotionActive;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;

public final class AntiLevitationModule extends Module
{
    public AntiLevitationModule()
    {
        super("AntiLevitation", new String[]
        { "NoLevitate" }, "Prevents you from levitating", "NONE", 0xC224DB, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerIsPotionActive> IsPotionActive = new Listener<>(p_Event ->
    {
        if (p_Event.potion == MobEffects.LEVITATION)
            p_Event.cancel();
    });
}
