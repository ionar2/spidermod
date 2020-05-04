package me.ionar.salhack.module.ui;

import me.ionar.salhack.gui.hud.GuiHudEditor;
import me.ionar.salhack.module.Module;

public final class HudEditorModule extends Module
{
    private GuiHudEditor m_HudEditor;

    public HudEditorModule()
    {
        super("HudEditor", new String[]{"HudEditor"}, "Displays the HudEditor", "GRAVE", 0xDBC824, ModuleType.UI);
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
            if (m_HudEditor == null)
                m_HudEditor = new GuiHudEditor();
            
            mc.displayGuiScreen(m_HudEditor);
        }
    }
}
