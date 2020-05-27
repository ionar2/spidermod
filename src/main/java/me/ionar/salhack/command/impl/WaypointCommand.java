package me.ionar.salhack.command.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import me.ionar.salhack.command.Command;
import me.ionar.salhack.util.SalVec3d;
import me.ionar.salhack.waypoints.Waypoint;
import me.ionar.salhack.waypoints.WaypointManager;

public class WaypointCommand extends Command
{
    public WaypointCommand()
    {
        super("Waypoint", "Allows you to create waypoints, remove them, or edit them, if no args are put in, the last created waypoint is used");
        
        CommandChunks.add("add <optional: name> x y z");
        CommandChunks.add("remove <optional: name>");
        CommandChunks.add("edit <optional: name> x y z");
    }
    
    @Override
    public void ProcessCommand(String args)
    {
        String[] split = args.split(" ");
        
        if (split == null || split.length <= 1)
        {
            SendToChat(GetHelp());
            return;
        }

        String name = null;
        SalVec3d pos = null;
        
        if (split.length >= 3)
            name = split[2];
        
        if (split.length > 3)
        {
            pos = new SalVec3d(0, -420, 0);
            
            pos.x = Double.parseDouble(split[3]);
            
            if (split.length == 4)
                pos.z = Double.parseDouble(split[4]);
            else if (split.length > 4)
                pos.y = Double.parseDouble(split[4]);
            
            if (split.length > 5)
                pos.z = Double.parseDouble(split[5]);
            
            if (pos.y == -420)
                pos.y = 100;
        }
        
        if (pos == null)
            pos = new SalVec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
        
        if (split[1].startsWith("a"))
        {
            if (name == null)
            {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
                LocalDateTime now = LocalDateTime.now();  
                name = dtf.format(now);
            }
            
            WaypointManager.Get().AddWaypoint(Waypoint.Type.Normal, name, pos, mc.player.dimension);
        }
        else if (split[1].startsWith("r"))
        {
            if (WaypointManager.Get().RemoveWaypoint(name))
                SendToChat("Successfully removed the waypoint named " + (name == null ? "last" : name));
            else
                SendToChat("Fail!");
        }
        else if (split[1].startsWith("e"))
        {
            if (WaypointManager.Get().EditWaypoint(name, pos))
                SendToChat("Successfully edited the waypoint");
            else
                SendToChat("Fail!");
        }
    }
}
