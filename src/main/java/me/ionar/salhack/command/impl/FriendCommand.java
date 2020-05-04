package me.ionar.salhack.command.impl;

import java.util.HashMap;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.managers.FriendManager;

public class FriendCommand extends Command
{
    public FriendCommand()
    {
        super("Friend", "Allows you to communicate with the friend manager, allowing for adding/removing/updating friends");
        
        CommandChunks.add("add <username>");
        CommandChunks.add("remove <username>");
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
        
        if (l_Split[1].toLowerCase().startsWith("a"))
        {
            if (l_Split.length > 1)
            {
                if (FriendManager.Get().AddFriend(l_Split[2].toLowerCase()))
                    SendToChat(String.format("Added %s as a friend.", l_Split[2]));
                else
                    SendToChat(String.format("%s is already a friend.", l_Split[2]));
                return;
            }
            else
            {
                SendToChat("Usage: friend add <name>");
                return;
            }
        }
        else if (l_Split[1].toLowerCase().startsWith("r"))
        {
            if (l_Split.length > 1)
            {
                if (FriendManager.Get().RemoveFriend(l_Split[2].toLowerCase()))
                    SendToChat(String.format("Removed %s as a friend.", l_Split[2]));
                else
                    SendToChat(String.format("%s is not a friend.", l_Split[2]));
                return;
            }
            else
            {
                SendToChat("Usage: friend remove <name>");
                return;
            }
        }
        else if (l_Split[1].toLowerCase().startsWith("l"))
        {
            final HashMap<String, Friend> l_Map = FriendManager.Get().GetFriends();
            
            l_Map.forEach((k,v)->
            {
                SendToChat(String.format("F: %s A: %s", v.GetName(), v.GetAlias()));
            });
            
            if (l_Map.isEmpty())
            {
                SendToChat("You don't have any friends...");
            }
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you to add friends, or remove friends or list friends..\nfriend add <name>\nfriend remove<name>\nfriend list";
    }
}
