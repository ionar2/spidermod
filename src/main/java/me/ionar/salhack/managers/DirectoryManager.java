package me.ionar.salhack.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import me.ionar.salhack.main.SalHack;

public class DirectoryManager
{
    public DirectoryManager()
    {
    }

    public void Init()
    {
        /// Create directories as needed
        try
        {
            CreateDirectory("SalHack");
            CreateDirectory("SalHack/Modules");
            CreateDirectory("SalHack/GUI");
            CreateDirectory("SalHack/HUD");
            CreateDirectory("SalHack/Locater");
            CreateDirectory("SalHack/StashFinder");
            CreateDirectory("SalHack/Config");
            CreateDirectory("SalHack/Capes");
            CreateDirectory("SalHack/Music");
            CreateDirectory("SalHack/CoordExploit");
            CreateDirectory("SalHack/LogoutSpots");
            CreateDirectory("SalHack/DeathSpots");
            CreateDirectory("SalHack/Waypoints");
            CreateDirectory("SalHack/Fonts");
            CreateDirectory("SalHack/CustomMods");
            CreateDirectory("SalHack/Presets");
            CreateDirectory("SalHack/Presets/Default");
            CreateDirectory("SalHack/Presets/Default/Modules");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void CreateDirectory(String p_Path) throws IOException
    {
        new File(p_Path).mkdirs();
        
        //System.out.println("Created path at " + l_Path.get().toString());
    }
    
    public static DirectoryManager Get()
    {
        return SalHack.GetDirectoryManager();
    }

    public String GetCurrentDirectory() throws IOException
    {
        return new java.io.File(".").getCanonicalPath();
    }
}
