package me.ionar.salhack.command.impl;

import java.util.HashMap;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.managers.CommandManager;
import me.ionar.salhack.managers.FriendManager;

public class HelpCommand extends Command
{
    public HelpCommand()
    {
        super("Help", "Gives you help for commands");
    }
    
    @Override
    public void ProcessCommand(String p_Args)
    {
        String[] l_Split = p_Args.split(" ");
        
        if (l_Split == null || l_Split.length <= 1)
        {
            SendToChat(GetHelp());
            return;
        }
        
        Command l_Command = CommandManager.Get().GetCommandLike(l_Split[1]);
        
        if (l_Command == null)
            SendToChat(String.format("Couldn't find any command named like %s", l_Split[1]));
        else
            SendToChat(l_Command.GetHelp());
    }
    
    @Override
    public String GetHelp()
    {
        final List<Command> l_Commands = CommandManager.Get().GetCommands();
        
        String l_CommandString = "Available commands: (" + l_Commands.size() + ")" + ChatFormatting.WHITE + " [";
        
        for (int l_I = 0; l_I < l_Commands.size(); ++l_I)
        {
            Command l_Command = l_Commands.get(l_I);
            
            if (l_I == l_Commands.size() - 1)
                l_CommandString += l_Command.GetName() + "]";
            else
                l_CommandString += l_Command.GetName() + ", ";
        }
        
        return l_CommandString;
    }
}
