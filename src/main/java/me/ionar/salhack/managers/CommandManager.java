package me.ionar.salhack.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.command.impl.*;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;

public class CommandManager
{
    public CommandManager()
    {
    }
    
    public void InitalizeCommands()
    {
        Commands.add(new FriendCommand());
        Commands.add(new HelpCommand());
        Commands.add(new SoundReloadCommand());
        Commands.add(new HClipCommand());
        Commands.add(new VClipCommand());
        Commands.add(new ToggleCommand());
        Commands.add(new BindCommand());
        Commands.add(new UnbindCommand());
        
        for (final Module l_Mod : ModuleManager.Get().GetModuleList())
            Commands.add(new ModuleCommand(l_Mod));
        
        /// Sort by alphabet
        Commands.sort(Comparator.comparing(Command::GetName));
    }
    
    private ArrayList<Command> Commands = new ArrayList<Command>();
    
    public final ArrayList<Command> GetCommands()
    {
        return Commands;
    }
    
    public final List<Command> GetCommandsLike(String p_Like)
    {
        return Commands.stream()
                .filter(p_Command -> p_Command.GetName().toLowerCase().startsWith(p_Like.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public static CommandManager Get()
    {
        return SalHack.GetCommandManager();
    }

    public Command GetCommandLike(String p_Like)
    {
        for (Command l_Command : Commands)
        {
            if (l_Command.GetName().toLowerCase().startsWith(p_Like.toLowerCase()))
                return l_Command;
        }
        
        return null;
    }
}
