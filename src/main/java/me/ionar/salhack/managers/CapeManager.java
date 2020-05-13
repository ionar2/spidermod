package me.ionar.salhack.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.imageio.ImageIO;
import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerGetLocationCape;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class CapeManager implements Listenable
{
    public CapeManager()
    {
        LoadCapes();

        SalHackMod.EVENT_BUS.subscribe(this);
    }

    private HashMap<String, ResourceLocation> CapeUsers = new HashMap<String, ResourceLocation>();
    private HashMap<String, ResourceLocation> Capes = new HashMap<String, ResourceLocation>(); /// < Only used at startup
    
    public void LoadCapes()
    {
        try
        {

            URL l_URL = null;
            URLConnection l_Connection = null;
            BufferedReader l_Reader = null;
            String l_Line;

            System.out.println("Downloading cape imgs");
            l_URL = new URL("http://salhack.com/cape.txt");
            l_Connection = l_URL.openConnection();
            l_Connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");

            l_Reader = new BufferedReader(new InputStreamReader(l_Connection.getInputStream()));

            while ((l_Line = l_Reader.readLine()) != null)
            {
                String[] l_Split = l_Line.split(" ");

                if (l_Split.length == 2)
                    DownloadCapeFromLocationWithName(l_Split[0], l_Split[1]);
            }
            
            l_Reader.close();

            System.out.println("Loading capes");
            l_URL = new URL("http://salhack.com/capes.txt");
            l_Connection = l_URL.openConnection();
            l_Connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");

            l_Reader = new BufferedReader(new InputStreamReader(l_Connection.getInputStream()));

            while ((l_Line = l_Reader.readLine()) != null)
                ProcessCapeString(l_Line);
            
            l_Reader.close();
        }

        catch (Exception e)
        {
            System.out.println(e.toString());
        }

        System.out.println("Done loading capes");
    }

    private void ProcessCapeString(String p_String)
    {
        String[] l_Split = p_String.split(" ");

        if (l_Split.length == 2)
        {
            /// User, CapeName
            final ResourceLocation l_Cape = GetCapeFromName(l_Split[1]);

            if (l_Cape != null)
                CapeUsers.put(l_Split[0], l_Cape);
            else
                System.out.println("Invalid cape name " + l_Split[1] + " for user " + l_Split[0]);
        }
    }

    private final ResourceLocation GetCapeFromName(String p_Name)
    {
        if (!Capes.containsKey(p_Name))
            return null;

        return Capes.get(p_Name);
    }

    public void DownloadCapeFromLocationWithName(String p_Link, String p_Name) throws MalformedURLException, IOException
    {
        final DynamicTexture l_Texture = new DynamicTexture(ImageIO.read(new URL(p_Link)));
        Capes.put(p_Name, Wrapper.GetMC().getTextureManager().getDynamicTextureLocation("salhack/capes", l_Texture));
    }

    @EventHandler
    private Listener<EventPlayerGetLocationCape> OnGetLocationSkin = new Listener<>(p_Event ->
    {
        if (Wrapper.GetMC().renderEngine == null)
            return;

        if (CapeUsers.containsKey(p_Event.Player.getName()))
        {
            p_Event.cancel();
            p_Event.SetResourceLocation(CapeUsers.get(p_Event.Player.getName()));
        }
    });

    public static CapeManager Get()
    {
        return SalHack.GetCapeManager();
    }
}
