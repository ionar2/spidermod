package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public class ToggleCommand extends Command
{
    public ToggleCommand()
    {
        super("Toggle", "Allows you to toggle a mod");
    }
    
    @Override
    public void ProcessCommand(String p_Args)
    {
        String[] l_Split = p_Args.split(" ");
        
        if (l_Split == null || l_Split.length <= 1)
        {
            SendToChat("Invalid Input");
            return;
        }
        
        Module l_Mod = ModuleManager.Get().GetModLike(l_Split[1]);
        
        if (l_Mod != null)
        {
            l_Mod.toggle();

            SendToChat(String.format("%sToggled %s", l_Mod.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED, l_Mod.GetArrayListDisplayName()));
        }
        else
        {
            SendToChat(String.format("Could not find the module named %s", l_Split[1]));
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you to toggle a mod";
    }
}
