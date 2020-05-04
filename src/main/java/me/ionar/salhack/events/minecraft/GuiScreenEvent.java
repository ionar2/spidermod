package me.ionar.salhack.events.minecraft;

import net.minecraft.client.gui.GuiScreen;

public class GuiScreenEvent
{
    private GuiScreen screen;

    public GuiScreenEvent(GuiScreen screen)
    {
        super();
        this.screen = screen;
    }

    public GuiScreen getScreen()
    {
        return screen;
    }

    public void setScreen(GuiScreen screen)
    {
        this.screen = screen;
    }

    public static class Displayed extends GuiScreenEvent
    {
        public Displayed(GuiScreen screen)
        {
            super(screen);
        }
    }

    public static class Closed extends GuiScreenEvent
    {
        public Closed(GuiScreen screen)
        {
            super(screen);
        }
    }
}
