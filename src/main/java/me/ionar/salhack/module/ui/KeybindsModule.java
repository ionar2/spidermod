package me.ionar.salhack.module.ui;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public class KeybindsModule extends Module
{
    public final Value<Boolean> Shift = new Value<Boolean>("StrictShift", new String[] {"Shift"}, "Activates strict keybinds when shift key is down", true);
    public final Value<Boolean> Ctrl = new Value<Boolean>("StrictCtrl", new String[] {"Ctrl"}, "Activates strict keybinds when ctrl key is down", true);
    public final Value<Boolean> Alt = new Value<Boolean>("StrictAlt", new String[] {"Alt"}, "Activates strict keybinds when alt key is down", true);

    public KeybindsModule()
    {
        super("Keybinds", new String[] {"Keys"}, "Allows you to modify the behavior of keybinds", "NONE", -1, ModuleType.UI);
    }

}
