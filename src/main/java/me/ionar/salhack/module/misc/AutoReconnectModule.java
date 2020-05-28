package me.ionar.salhack.module.misc;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public class AutoReconnectModule extends Module
{
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"Delay"}, "Delay to use between attempts", 5.0f, 0.0f, 20.0f, 1.0f);
    
    public AutoReconnectModule()
    {
        super("AutoReconnect", new String[] {"Reconnect"}, "Automatically reconnects you to your last server", "NONE", -1, ModuleType.MISC);
    }
}
