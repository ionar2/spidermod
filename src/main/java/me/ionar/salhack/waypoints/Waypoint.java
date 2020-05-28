package me.ionar.salhack.waypoints;

import me.ionar.salhack.util.SalVec3d;

public class Waypoint
{
    private String _displayName;
    private SalVec3d _pos;
    private Type _type;
    private String _address;
    private int _dimension;
    
    public Waypoint(String displayName, SalVec3d pos, Type type, String address, int dimension)
    {
        _displayName = displayName;
        _pos = pos;
        _type = type;
        _address = address;
        _dimension = dimension;
    }
    
    @Override
    public String toString()
    {
        return _pos.toString() + " Type: " + String.valueOf(_type);
    }
    
    public Type getType()
    {
        return _type;
    }
    
    public String getDisplayName()
    {
        return _displayName;
    }
    
    public double getX()
    {
        return _pos.x;
    }
    
    public double getY()
    {
        return _pos.y;
    }
    
    public double getZ()
    {
        return _pos.z;
    }

    public void setPos(SalVec3d pos)
    {
        _pos = pos;
    }

    public String getAddress()
    {
        return _address;
    }

    public int getDimension()
    {
        return _dimension;
    }

    public enum Type
    {
        Normal,
        Logout,
        Death,
        CoordTPExploit
    }

}
