package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public class GlideModule extends Module {

    public GlideModule()
    {
        super("GlideModule", new String[] {"Glide"}, "Allows you to glide.", "NONE", 0xFFFB11, ModuleType.MOVEMENT);
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event -> {
        if(GlideModule.this.isEnabled())
        if (shouldGlide()) {
            mc.player.motionY = -0.125;
            mc.player.jumpMovementFactor = mc.player.jumpMovementFactor * 1.21337F;
        }
    });

    private boolean shouldGlide() {
        return !mc.gameSettings.isKeyDown(mc.gameSettings.keyBindJump) && mc.player.motionY !=0 && !mc.player.onGround && mc.player.fallDistance !=0 && !mc.player.isInWater() && !mc.player.isOnLadder() && !mc.player.isInLava() && !mc.player.collidedVertically;
    }

}
