package me.ionar.salhack.gui.click.component.item;

import org.lwjgl.input.Keyboard;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;

public class ComponentItemKeybind extends ComponentItem
{
    public boolean Listening = false;
    final Module Mod;
    
    public ComponentItemKeybind(Module p_Mod, String p_DisplayText, String p_Description, int p_Flags, int p_State, ComponentItemListener p_Listener, float p_Width, float p_Height)
    {
        super(p_DisplayText, p_Description, p_Flags, p_State, p_Listener, p_Width, p_Height);
        Mod = p_Mod;

        Flags |= ComponentItem.RectDisplayAlways;
    }

    @Override
    public String GetDisplayText()
    {
        if (Listening)
            return "Press a Key...";

        return "Keybind " + Mod.getKey();
    }

    @Override
    public String GetDescription()
    {
        return "Sets the key of the Module: " + Mod.getDisplayName();
    }
    
    @Override
    public void OnMouseClick(int p_MouseX, int p_MouseY, int p_MouseButton)
    {
        super.OnMouseClick(p_MouseX, p_MouseY, p_MouseButton);
        
        if (p_MouseButton == 0)
        	Listening = !Listening;
        else if (p_MouseButton == 1)
        	Listening = false;
        else if (p_MouseButton == 2)
        {
        	Mod.setKey("NONE");
        	SalHack.SendMessage("Unbinded the module: " + Mod.getDisplayName());
        	Listening = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (Listening)
        {
            String l_Key = String.valueOf(Keyboard.getKeyName(keyCode)).toUpperCase();

            if (l_Key.length() < 1)
            {
                Listening = false;
                return;
            }
            
            if (l_Key.equals("END"))
            {
            	l_Key = "NONE";
            }

            Mod.setKey(l_Key);
            SalHack.SendMessage("Set the key of " + Mod.getDisplayName() + " to " + l_Key);
            Listening = false;
        }
    }
}
