package me.ionar.salhack.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.command.impl.*;
import me.ionar.salhack.command.util.ModuleCommandListener;
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
        Commands.add(new ResetGUICommand());
        Commands.add(new FontCommand());
        Commands.add(new PresetsCommand());
        Commands.add(new WaypointCommand());
        
        ModuleManager.Get().GetModuleList().forEach(p_Mod ->
        {
            ModuleCommandListener l_Listener = new ModuleCommandListener()
            {
                @Override
                public void OnHide()
                {
                    p_Mod.setHidden(!p_Mod.isHidden());
                }

                @Override
                public void OnToggle()
                {
                    p_Mod.toggle();
                }

                @Override
                public void OnRename(String p_NewName)
                {
                    p_Mod.setDisplayName(p_NewName);
                }
            };
            
            Commands.add(new ModuleCommand(p_Mod.getDisplayName(), p_Mod.getDesc(), l_Listener, p_Mod.getValueList()));
        });

        HudManager.Get().Items.forEach(p_Item ->
        {
            ModuleCommandListener l_Listener = new ModuleCommandListener()
            {
                @Override
                public void OnHide()
                {
                    p_Item.SetHidden(!p_Item.IsHidden());
                }

                @Override
                public void OnToggle()
                {
                    p_Item.SetHidden(!p_Item.IsHidden());
                }

                @Override
                public void OnRename(String p_NewName)
                {
                    p_Item.SetDisplayName(p_NewName, true);
                }
            };
            
            Commands.add(new ModuleCommand(p_Item.GetDisplayName(), "NYI", l_Listener, p_Item.ValueList));
        });
        
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

    public void Reload()
    {
        Commands.clear();
        InitalizeCommands();
    }
}
