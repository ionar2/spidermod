package me.ionar.salhack.command.impl;

import java.io.File;
import java.io.IOException;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.DirectoryManager;
import me.ionar.salhack.managers.FontManager;

public class FontCommand extends Command
{
    public FontCommand()
    {
        super("Font", "Allows you to set the font of the client, this must be an existing TTF in your salhack folder");
        
        CommandChunks.add("<ttfFontName>");
    }
    
    @Override
    public void ProcessCommand(String args)
    {
        String[] split = args.split(" ");
        
        if (split == null || split.length <= 1)
        {
            SendToChat("Invalid Input");
            return;
        }

        if (split.length <= 2)
        {
            SendToChat(String.format("Trying to load \"%s\"", split[1]));
            
            try
            {
                if (!new File(DirectoryManager.Get().GetCurrentDirectory() + "/SalHack/Fonts/" + split[1] + ".ttf").exists())
                {
                    SendToChat(ChatFormatting.RED + "That file doesn't exist in SalHack/Fonts/ directory!");
                    return;
                }
            }
            catch (IOException e)
            {
            }
            
            FontManager.Get().LoadCustomFont(split[1]);
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you to Bind a mod";
    }
}