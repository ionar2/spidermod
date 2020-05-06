package me.ionar.salhack.waypoints;

import net.minecraft.util.math.Vec3d;

public class Waypoint
{
    private Vec3d Position;
    private Type m_Type;
    
    public Waypoint(Vec3d p_Position, Type p_Type)
    {
        Position = p_Position;
        m_Type = p_Type;
    }
    
    @Override
    public String toString()
    {
        return Position.toString() + " Type: " + String.valueOf(m_Type);
    }
    
    private enum Type
    {
        Normal,
        Logout,
        Death,
        CoordTPExploit
    }
}
