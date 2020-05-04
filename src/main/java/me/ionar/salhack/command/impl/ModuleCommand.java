package me.ionar.salhack.command.impl;

import java.util.HashMap;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public class ModuleCommand extends Command
{
    private Module Mod;
    
    public ModuleCommand(Module p_Mod)
    {
        super(p_Mod.getDisplayName(), p_Mod.getDesc());
        Mod = p_Mod;
        
        CommandChunks.add("hide");
        CommandChunks.add("toggle");
        CommandChunks.add("rename <newname>");
        
        /// TODO: Add enum names, etc
        for (Value l_Val : Mod.getValueList())
            CommandChunks.add(String.format("%s <%s>", l_Val.getName(), "value"));
    }

    @Override
    public void ProcessCommand(String p_Args)
    {
        String[] l_Split = p_Args.split(" ");
        
        if (l_Split == null || l_Split.length <= 1)
        {
            /// Print values

            for (Value l_Val : Mod.getValueList())
            {
                SendToChat(String.format("%s : %s",l_Val.getName(), l_Val.getValue()));
            }
            return;
        }
        
        for (Value l_Val : Mod.getValueList())
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
        return Mod.getDesc();
    }
}
