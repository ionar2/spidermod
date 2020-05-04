package me.ionar.salhack.module.ui;

import me.ionar.salhack.gui.click.ClickGuiScreen;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public final class ClickGuiModule extends Module
{
    public final Value<Boolean> AllowOverflow = new Value<Boolean>("AllowOverflow", new String[]
    { "AllowOverflow" }, "Allows the GUI to overflow", true);

    public ClickGuiScreen m_ClickGui;

    public ClickGuiModule()
    {
        super("ClickGui", new String[]
        { "ClickGui", "ClickGui" }, "Displays the click gui", "LEFT", 0xDB9324, ModuleType.UI);
        setHidden(true);
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (m_ClickGui == null)
            m_ClickGui = new ClickGuiScreen(this);

        if (mc.world != null)
        {
            mc.displayGuiScreen(m_ClickGui);
        }
    }
}
