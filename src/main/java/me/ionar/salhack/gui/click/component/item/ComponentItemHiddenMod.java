package me.ionar.salhack.gui.click.component.item;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.util.render.RenderUtil;

public class ComponentItemHiddenMod extends ComponentItem
{
    final Module Mod;
    
    public ComponentItemHiddenMod(Module p_Mod, String p_DisplayText, String p_Description, int p_Flags, int p_State, ComponentItemListener p_Listener, float p_Width, float p_Height)
    {
        super(p_DisplayText, p_Description, p_Flags, p_State, p_Listener, p_Width, p_Height);
        Mod = p_Mod;
    }
    
    @Override
    public boolean HasState(int p_State)
    {
        if ((p_State & ComponentItem.Clicked) != 0)
            return Mod.isHidden();
        
        return super.HasState(p_State);
    }
}
