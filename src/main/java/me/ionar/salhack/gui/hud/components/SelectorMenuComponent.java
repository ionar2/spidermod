package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentHUDList;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.util.render.RenderUtil;

public class SelectorMenuComponent extends HudComponentItem
{
    MenuComponentHUDList l_Component = new MenuComponentHUDList("Selector", 300, 300);
    
    public SelectorMenuComponent()
    {
        super("Selector", 300, 300);
        SetHidden(false);
        AddFlag(HudComponentItem.OnlyVisibleInHudEditor);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);
        
        l_Component.Render(p_MouseX, p_MouseY, true, true, 0);
        
        SetWidth(l_Component.GetWidth());
        SetHeight(l_Component.GetHeight());
        SetX(l_Component.GetX());
        SetY(l_Component.GetY());
    }

    @Override
    public boolean OnMouseClick(int p_MouseX, int p_MouseY, int p_MouseButton)
    {
        return l_Component.MouseClicked(p_MouseX, p_MouseY, p_MouseButton, 0);
    }

    @Override
    public void OnMouseRelease(int p_MouseX, int p_MouseY, int p_State)
    {
        super.OnMouseRelease(p_MouseX, p_MouseY, p_State);
        l_Component.MouseReleased(p_MouseX, p_MouseY, p_State);
    }
}
