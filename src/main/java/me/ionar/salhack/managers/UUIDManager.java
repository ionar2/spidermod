package me.ionar.salhack.managers;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonParser;

import me.ionar.salhack.main.SalHack;

import org.apache.commons.io.IOUtils;

import java.net.URL;

// based from seppuku
public class UUIDManager
{
    public UUIDManager()
    {
        
    }
    
    private final Map<String, String> uuidNameCache = Maps.newConcurrentMap();

    public String resolveName(String uuid)
    {
        uuid = uuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid))
        {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try
        {
            final String nameJson = IOUtils.toString(new URL(url), "UTF-8");
            if (nameJson != null && nameJson.length() > 0)
            {
                JsonParser parser = new JsonParser();

                return parser.parse(nameJson).getAsJsonArray().get(parser.parse(nameJson).getAsJsonArray().size() - 1)
                        .getAsJsonObject().get("name").toString();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static UUIDManager Get()
    {
        return SalHack.GetUUIDManager();
    }
}
