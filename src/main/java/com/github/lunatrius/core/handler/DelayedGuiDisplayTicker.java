package com.github.lunatrius.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DelayedGuiDisplayTicker {
    private final GuiScreen guiScreen;
    private int ticks;

    private DelayedGuiDisplayTicker(final GuiScreen guiScreen, final int delay) {
        this.guiScreen = guiScreen;
        this.ticks = delay;
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        this.ticks--;

        if (this.ticks < 0) {
            Minecraft.getMinecraft().displayGuiScreen(this.guiScreen);
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public static void create(final GuiScreen guiScreen, final int delay) {
        MinecraftForge.EVENT_BUS.register(new DelayedGuiDisplayTicker(guiScreen, delay));
    }
}
