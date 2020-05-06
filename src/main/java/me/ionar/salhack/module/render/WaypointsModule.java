package me.ionar.salhack.module.render;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public class WaypointsModule extends Module
{
    public final Value<Boolean> Normal = new Value<Boolean>("Normal", new String[] {"Normal"}, "Displays normal waypoints", true);
    public final Value<Boolean> LogoutSpots = new Value<Boolean>("LogoutSpots", new String[] {"LogoutSpots"}, "Displays players LogoutSpots", true);
    public final Value<Boolean> DeathPoints = new Value<Boolean>("DeathPoints", new String[] {"DeathPoints"}, "Displays players DeathPoints", true);
    
    public WaypointsModule()
    {
        super("Waypoints", new String[] {"Waypoint"}, "Displays waypoints in your render", "NONE", -1, ModuleType.WORLD);
    }
}
