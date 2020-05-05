package me.ionar.salhack.gui.click.component.menus.mods;

import me.ionar.salhack.gui.click.component.MenuComponent;
import me.ionar.salhack.gui.click.component.item.ComponentItem;
import me.ionar.salhack.gui.click.component.item.ComponentItemHiddenMod;
import me.ionar.salhack.gui.click.component.item.ComponentItemKeybind;
import me.ionar.salhack.gui.click.component.item.ComponentItemMod;
import me.ionar.salhack.gui.click.component.item.ComponentItemValue;
import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.ColorsModule;

public class MenuComponentModList extends MenuComponent
{

    public MenuComponentModList(String p_DisplayName, ModuleType p_Type, float p_X, float p_Y, String p_Image, ColorsModule p_Colors)
    {
        super(p_DisplayName, p_X, p_Y, 100f, 105f, p_Image, p_Colors);
        
        final float Width = 105f;
        final float Height = 11f;

        for (Module l_Mod : ModuleManager.Get().GetModuleList(p_Type))
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
                    l_Mod.toggle();
                    
                  //  SalHack.INSTANCE.getNotificationManager().addNotification("ClickGUI", "Toggled " + l_Mod.getDisplayName());
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
            
            if (!l_Mod.getValueList().isEmpty())
                l_Flags |= ComponentItem.HasValues;
            
            int l_State = 0;
            
            if (l_Mod.isEnabled())
                l_State |= ComponentItem.Clicked;
            
            ComponentItem l_Item = new ComponentItemMod(l_Mod, l_Mod.getDisplayName(), l_Mod.getDesc(), l_Flags, l_State, l_Listener, Width, Height);
            
            for (Value l_Val : l_Mod.getValueList())
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
                
                l_Item.DropdownItems.add(l_ValItem);
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
                    l_Mod.setHidden(!l_Mod.isHidden());
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
            
            ComponentItem l_HideButton = new ComponentItemHiddenMod(l_Mod, "Hidden", "Hides " + l_Mod.getDisplayName() + " from the arraylist",  ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip | ComponentItem.RectDisplayOnClicked | ComponentItem.DontDisplayClickableHighlight, 0, l_Listener, Width, Height);

            l_Item.DropdownItems.add(l_HideButton);
            
            l_Item.DropdownItems.add(new ComponentItemKeybind(l_Mod, "Keybind:"+l_Mod.getDisplayName(), l_Mod.getDesc(),  ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip, 0, null, Width, Height));
            
            AddItem(l_Item);
        }
        
    }

}
