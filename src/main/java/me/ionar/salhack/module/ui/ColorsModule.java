package me.ionar.salhack.module.ui;

import me.ionar.salhack.gui.click.ClickGuiScreen;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public final class ColorsModule extends Module
{
    // 0x9933b6d7
    public final Value<Integer> Red = new Value<Integer>("Red", new String[] {"bRed"}, "Red for rendering", 0x33, 0, 255, 11);
    public final Value<Integer> Green = new Value<Integer>("Green", new String[] {"bGreen"}, "Green for rendering", 0xb6, 0, 255, 11);
    public final Value<Integer> Blue = new Value<Integer>("Blue", new String[] {"bBlue"}, "Blue for rendering", 0xd7, 0, 255, 11);
    public final Value<Integer> Alpha = new Value<Integer>("Alpha", new String[] {"bAlpha"}, "Alpha for rendering", 0x99, 0, 255, 11);
    
    public final Value<Float> ImageRed = new Value<Float>("ImageRed", new String[] {"iRed"}, "Red for rendering the icons", 1f, 0f, 1f, 0.1f);
    public final Value<Float> ImageGreen = new Value<Float>("ImageGreen", new String[] {"iGreen"}, "Green for rendering the icons", 1f, 0f, 1f, 0.1f);
    public final Value<Float> ImageBlue = new Value<Float>("ImageBlue", new String[] {"iBlue"}, "Blue for rendering the icons", 1f, 0f, 1f, 0.1f);
    public final Value<Float> ImageAlpha = new Value<Float>("ImageAlpha", new String[] {"iAlpha"}, "Alpha for rendering the icons", 1f, 0f, 1f, 0.1f);
    
    public ColorsModule()
    {
        super("Colors", new String[]
        { "Colors", "Colors" }, "Allows you to modify the GUI Colors", "NONE", -1, ModuleType.UI);
    }
}
