package me.ionar.salhack.module.ui;

import me.ionar.salhack.gui.chat.SalGuiConsole;
import me.ionar.salhack.module.Module;

public final class ConsoleModule extends Module
{
    private SalGuiConsole m_Console;

    public ConsoleModule()
    {
        super("Console", new String[]{"Console"}, "Displays the click gui", "UP", 0xDBB024, ModuleType.UI);
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }

    @Override
    public void onToggle()
    {
        super.onToggle();
        
        if (mc.world != null)
        {
            if (m_Console == null)
                m_Console = new SalGuiConsole(this);
            
            mc.displayGuiScreen(m_Console);
        }
    }
}
