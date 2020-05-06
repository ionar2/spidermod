package me.ionar.salhack.waypoints;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.main.SalHack;
import me.zero.alpine.fork.listener.Listenable;

public class WaypointManager implements Listenable
{
    public WaypointManager()
    {
        SalHackMod.EVENT_BUS.subscribe(this);
    }
    
    public static WaypointManager Get()
    {
        return SalHack.GetWaypointManager();
    }
}
