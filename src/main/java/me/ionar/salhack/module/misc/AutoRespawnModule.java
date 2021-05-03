package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.minecraft.GuiScreenEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

public class AutoRespawnModule extends Module {

    public AutoRespawnModule()
    {
        super("AutoRespawn", new String[] {"AutoRespawn"}, "Automatically respawn.", "NONE", 0xFFFB11, ModuleType.MISC);
    }

    @EventHandler
    private final Listener<EventPlayerUpdate> listener = new Listener<>(p_Event -> {
        if (mc.player.getHealth() == 0.0f && mc.player != null) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    });
}
