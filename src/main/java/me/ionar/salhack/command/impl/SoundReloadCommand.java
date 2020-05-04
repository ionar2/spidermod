package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;

public class SoundReloadCommand extends Command
{
    public SoundReloadCommand()
    {
        super("SoundReload", "Reloads the sound system");
    }
    
    @Override
    public void ProcessCommand(String p_Args)
    {
        mc.getSoundHandler().sndManager.reloadSoundSystem();
        SendToChat(ChatFormatting.GREEN + "Reloaded the SoundSystem!");
    }
    
    @Override
    public String GetHelp()
    {
        return "Reloads the sound manager sound system";
    }
}
