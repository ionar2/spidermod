package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public class BindCommand extends Command
{
    public BindCommand()
    {
        super("Bind", "Allows you to bind a mod to a key");
        
        CommandChunks.add("<module>");
        CommandChunks.add("<module> <key>");
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
            if (l_Split.length <= 2)
            {
                SendToChat(String.format("The key of %s is %s", l_Mod.getDisplayName(), l_Mod.getKey()));
                return;
            }
            
            l_Mod.setKey(l_Split[2].toUpperCase());
            SendToChat(String.format("Set the key of %s to %s", l_Mod.getDisplayName(), l_Mod.getKey()));
        }
        else
        {
            SendToChat(String.format("Could not find the module named %s", l_Split[1]));
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you to Bind a mod";
    }
}
