package me.ionar.salhack.gui.click.component.menus.mods;

import me.ionar.salhack.gui.click.component.MenuComponent;
import me.ionar.salhack.gui.click.component.item.ComponentItem;
import me.ionar.salhack.gui.click.component.item.ComponentItemHUD;
import me.ionar.salhack.gui.click.component.item.ComponentItemKeybind;
import me.ionar.salhack.gui.click.component.item.ComponentItemMod;
import me.ionar.salhack.gui.click.component.item.ComponentItemValue;
import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.ui.ColorsModule;
import me.ionar.salhack.module.Value;

public class MenuComponentHUDList extends MenuComponent
{

    public MenuComponentHUDList(String p_DisplayName, float p_X, float p_Y)
    {
        super(p_DisplayName, p_X, p_Y, 100f, 105f, "", (ColorsModule)ModuleManager.Get().GetMod(ColorsModule.class), null);
        
        final float Width = 105f;
        final float Height = 11f;

        for (HudComponentItem l_Item : HudManager.Get().Items)
        {
            ComponentItemListener l_Listener = new ComponentItemListener()
            {
                @Override
                public void OnEnabled()
                {
                }
                
                @Override
                public void OnToggled()
                {
                    l_Item.SetHidden(!l_Item.IsHidden());
                }

                @Override
                public void OnDisabled()
                {
                }

                @Override
                public void OnHover()
                {

                }

                @Override
                public void OnMouseEnter()
                {

                }

                @Override
                public void OnMouseLeave()
                {

                }
            };
            
            int l_Flags = ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip;
            
            if (!l_Item.ValueList.isEmpty())
                l_Flags |= ComponentItem.HasValues;
            
            int l_State = 0;
            
            if (!l_Item.IsHidden())
                l_State |= ComponentItem.Clicked;
            
            ComponentItem l_CItem = new ComponentItemHUD(l_Item, l_Item.GetDisplayName(), "", l_Flags, l_State, l_Listener, Width, Height);
            
            for (Value l_Val : l_Item.ValueList)
            {
                l_Listener = new ComponentItemListener()
                {
                    @Override
                    public void OnEnabled()
                    {
                    }

                    @Override
                    public void OnToggled()
                    {
                    }

                    @Override
                    public void OnDisabled()
                    {
                    }

                    @Override
                    public void OnHover()
                    {

                    }

                    @Override
                    public void OnMouseEnter()
                    {

                    }

                    @Override
                    public void OnMouseLeave()
                    {

                    }
                };
                ComponentItemValue l_ValItem = new ComponentItemValue(l_Val, l_Val.getName(), l_Val.getDesc(), ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip, 0, l_Listener, Width, Height);
                
                l_CItem.DropdownItems.add(l_ValItem);
            }

            l_Listener = new ComponentItemListener()
            {
                @Override
                public void OnEnabled()
                {
                }

                @Override
                public void OnToggled()
                {
                    l_Item.ResetToDefaultPos();
                }

                @Override
                public void OnDisabled()
                {
                }

                @Override
                public void OnHover()
                {

                }

                @Override
                public void OnMouseEnter()
                {

                }

                @Override
                public void OnMouseLeave()
                {

                }
            };
            
            ComponentItem l_ResetButton = new ComponentItem("Reset", "Resets the position of " + l_Item.GetDisplayName() + " to default.",  ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip | ComponentItem.Enum | ComponentItem.DontDisplayClickableHighlight | ComponentItem.RectDisplayAlways, 0, l_Listener, Width, Height);

            l_CItem.DropdownItems.add(l_ResetButton);
            
            AddItem(l_CItem);
        }
        
    }

}
