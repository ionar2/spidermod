package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentHUDList;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.util.render.RenderUtil;

public class SelecterMenuComponent extends HudComponentItem
{
    MenuComponentHUDList l_Component = new MenuComponentHUDList("Selecter", 300, 300);
    
    public SelecterMenuComponent()
    {
        super("Selecter", 300, 300);
        SetHidden(false);
        AddFlag(HudComponentItem.OnlyVisibleInHudEditor);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);
        
        l_Component.Render(p_MouseX, p_MouseY, true, true);
    }

    @Override
    public boolean OnMouseClick(int p_MouseX, int p_MouseY, int p_MouseButton)
    {
        return l_Component.MouseClicked(p_MouseX, p_MouseY, p_MouseButton);
    }

    @Override
    public void OnMouseRelease(int p_MouseX, int p_MouseY, int p_State)
    {
        super.OnMouseRelease(p_MouseX, p_MouseY, p_State);
        l_Component.MouseReleased(p_MouseX, p_MouseY, p_State);
    }
}
