package me.ionar.salhack.module.misc;

import me.ionar.salhack.module.Module;

public class FriendsModule extends Module
{
    public FriendsModule()
    {
        super("Friends", new String[] {"Homies"}, "Allows the friend system to function, disabling this ignores friend requirements, useful for dueling friends.", "NONE", -1, ModuleType.MISC);
        EnabledByDefault = true;
    }
}
