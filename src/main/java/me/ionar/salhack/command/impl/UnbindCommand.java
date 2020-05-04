package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;

public class UnbindCommand extends Command
{
    public UnbindCommand()
    {
        super("Unbind", "Allows you to unbind a mod to a key");
        
        CommandChunks.add("<module>");
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
            l_Mod.setKey("NONE");
            SendToChat(String.format("Unbound %s", l_Mod.getDisplayName()));
        }
        else
        {
            SendToChat(String.format("Could not find the module named %s", l_Split[1]));
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you to unbind a mod";
    }
}
