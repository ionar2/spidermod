package me.ionar.salhack.command.impl;

import java.util.HashMap;

import com.google.gson.internal.LinkedTreeMap;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.PresetsManager;

public class PresetsCommand extends Command
{
    public PresetsCommand()
    {
        super("Presets", "Allows you to create custom presets");
        
        CommandChunks.add("create <name>");
        CommandChunks.add("delete <name>");
        CommandChunks.add("list");
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
        
        if (l_Split[1].toLowerCase().startsWith("c"))
        {
            if (l_Split.length > 1)
            {
                String presetName = l_Split[2].toLowerCase();
                
                if (!presetName.equalsIgnoreCase("Deault"))
                {
                    PresetsManager.Get().CreatePreset(presetName);
                    SendToChat("Created a preset named " + presetName);
                }
                else
                    SendToChat("Default preset is reserved!");
                
                return;
            }
            else
            {
                SendToChat("Usage: preset create <name>");
                return;
            }
        }
        else if (l_Split[1].toLowerCase().startsWith("d"))
        {
            if (l_Split.length > 1)
            {
                String presetName = l_Split[2].toLowerCase();
                
                if (!presetName.equalsIgnoreCase("Deault"))
                {
                    PresetsManager.Get().RemovePreset(presetName);
                    SendToChat("Removed a preset named " + presetName);
                }
                else
                    SendToChat("Default preset is reserved!");
                
                return;
            }
            else
            {
                SendToChat("Usage: preset remove <name>");
                return;
            }
        }
        else if (l_Split[1].toLowerCase().startsWith("l"))
        {
            PresetsManager.Get().GetItems().forEach(p ->
            {
                SendToChat(p.getName());
            });
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you to create, remove and list the presets";
    }
}
