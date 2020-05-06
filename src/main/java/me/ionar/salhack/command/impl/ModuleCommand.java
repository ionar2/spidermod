package me.ionar.salhack.command.impl;

import java.util.HashMap;
import java.util.List;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.command.util.ModuleCommandListener;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public class ModuleCommand extends Command
{
    private ModuleCommandListener Listener;
    private final List<Value> Values;
    
    public ModuleCommand(String p_Name, String p_Description, ModuleCommandListener p_Listener, final List<Value> p_Values)
    {
        super(p_Name, p_Description);
        Listener = p_Listener;
        Values = p_Values;
        
        CommandChunks.add("hide");
        CommandChunks.add("toggle");
        CommandChunks.add("rename <newname>");
        
        /// TODO: Add enum names, etc
        for (Value l_Val : Values)
            CommandChunks.add(String.format("%s <%s>", l_Val.getName(), "value"));
    }

    @Override
    public void ProcessCommand(String p_Args)
    {
        String[] l_Split = p_Args.split(" ");
        
        if (l_Split == null || l_Split.length <= 1)
        {
            /// Print values
            for (Value l_Val : Values)
            {
                SendToChat(String.format("%s : %s",l_Val.getName(), l_Val.getValue()));
            }
            return;
        }
        
        if (l_Split[1].equalsIgnoreCase("hide"))
        {
            Listener.OnHide();
            return;
        }

        if (l_Split[1].equalsIgnoreCase("toggle"))
        {
            Listener.OnHide();
            return;
        }

        if (l_Split[1].equalsIgnoreCase("rename"))
        {
            if (l_Split.length <= 3)
                Listener.OnRename(l_Split[2]);
            
            return;
        }
        
        for (Value l_Val : Values)
        {
            if (l_Val.getName().toLowerCase().startsWith(l_Split[1].toLowerCase()))
            {
                if (l_Split.length <= 2)
                    break;
                
                String l_Value = l_Split[2].toLowerCase();
                
                if (l_Val.getValue() instanceof Number && !(l_Val.getValue() instanceof Enum))
                {
                    if (l_Val.getValue() instanceof Integer)
                        l_Val.SetForcedValue(Integer.parseInt(l_Value));
                    else if (l_Val.getValue() instanceof Float)
                        l_Val.SetForcedValue(Float.parseFloat(l_Value));
                    else if (l_Val.getValue() instanceof Double)
                        l_Val.SetForcedValue(Double.parseDouble(l_Value));
                }
                else if (l_Val.getValue() instanceof Boolean)
                {
                    l_Val.SetForcedValue(l_Value.equalsIgnoreCase("true"));
                }
                else if (l_Val.getValue() instanceof Enum)
                {
                    l_Val.SetForcedValue(l_Val.GetEnumReal(l_Value));
                }
                else if (l_Val.getValue() instanceof String)
                    l_Val.SetForcedValue(l_Value);
                
                SendToChat(String.format("Set the value of %s to %s", l_Val.getName(), l_Val.getValue()));
                
                break;
            }
        }
    }
    
    @Override
    public String GetHelp()
    {
        return GetDescription();
    }
}
