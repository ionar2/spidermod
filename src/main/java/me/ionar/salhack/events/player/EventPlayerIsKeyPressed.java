package me.ionar.salhack.events.player;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.settings.KeyBinding;

public class EventPlayerIsKeyPressed extends MinecraftEvent
{
    public KeyBinding Keybind;
    public boolean IsKeyPressed = false;
    
    public EventPlayerIsKeyPressed(KeyBinding p_Key)
    {
        Keybind = p_Key;
        IsKeyPressed = false;
    }

}
