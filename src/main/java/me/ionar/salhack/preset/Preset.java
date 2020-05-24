package me.ionar.salhack.preset;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;

public class Preset
{
    private String _displayName;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> _valueListMods = new ConcurrentHashMap<>();
    private boolean _active;
    
    public Preset(String displayName)
    {
        _displayName = displayName;
    }
    
    public void initNewPreset()
    {
        ModuleManager.Get().GetModuleList().forEach(mod ->
        {
            addModuleSettings(mod);
        });
    }
    
    public void addModuleSettings(final Module mod)
    {
        ConcurrentHashMap<String, String> valsMap = new ConcurrentHashMap<>();

        valsMap.put("enabled", mod.isEnabled() ? "true" : "false");
        valsMap.put("display", mod.getDisplayName());
        valsMap.put("keybind", mod.getKey());
        valsMap.put("hidden", mod.isHidden() ? "true" : "false");
        
        mod.getValueList().forEach(val ->
        {
            valsMap.put(val.getName(), val.getValue().toString());
        });

        _valueListMods.put(mod.getDisplayName(), valsMap);
        
        save();
    }
    
    // this will load the settings for presets, and modules settings
    public void load(File directory)
    {
        System.out.println("SalHack/Presets/" + directory.getName() + "/" + directory.getName() + ".json");
        
        File exists = new File("SalHack/Presets/" + directory.getName() + "/" + directory.getName() + ".json");
        if (!exists.exists())
            return;
        
        try 
        {
            Gson gson = new Gson();

            Reader reader = Files.newBufferedReader(Paths.get("SalHack/Presets/" + directory.getName() + "/" + directory.getName() + ".json"));

            Map<?, ?> map = gson.fromJson(reader, Map.class);
            
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                
                if (key == "displayName")
                {
                    _displayName = val;
                    continue;
                }
            }

            reader.close();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        try (Stream<Path> paths = Files.walk(Paths.get("SalHack/Presets/" + directory.getName() + "/Modules/")))
        {
            paths
                .filter(Files::isRegularFile)
                .forEach(path -> 
                {
                    try 
                    {
                        Gson gson = new Gson();
                        
                        System.out.println(path.getFileName().toString());

                        Reader reader = Files.newBufferedReader(Paths.get("SalHack/Presets/" + directory.getName() + "/Modules/" + path.getFileName().toString()));

                        Map<?, ?> map = gson.fromJson(reader, Map.class);

                        ConcurrentHashMap<String, String> valsMap = new ConcurrentHashMap<>();

                        for (Map.Entry<?, ?> entry : map.entrySet())
                        {
                            String key = (String) entry.getKey();
                            String val = (String) entry.getValue();

                            valsMap.put(key, val);
                        }

                        _valueListMods.put(path.getFileName().toString().substring(0, path.getFileName().toString().indexOf(".json")), valsMap);

                        reader.close();

                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void save()
    {
        try
        {
            GsonBuilder builder = new GsonBuilder();
            
            Gson gson = builder.setPrettyPrinting().create();

            Writer writer = Files.newBufferedWriter(Paths.get("SalHack/Presets/" + _displayName + "/" + _displayName + ".json"));
            Map<String, String> map = new HashMap<>();
            
            map.put("displayName", _displayName);
            gson.toJson(map, writer);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try
        {

            for (Entry<String, ConcurrentHashMap<String, String>> entry : _valueListMods.entrySet())
            {
                GsonBuilder builder = new GsonBuilder();
                
                Gson gson = builder.setPrettyPrinting().create();

                Writer writer = Files.newBufferedWriter(Paths.get("SalHack/Presets/" + _displayName + "/Modules/" + entry.getKey() + ".json"));
                Map<String, String> map = new HashMap<>();
                
                for (Entry<String, String> value : entry.getValue().entrySet())
                {
                    String key = (String)value.getKey();
                    String val = (String)value.getValue();
                    
                    map.put(key, val);
                }
                gson.toJson(map, writer);
                writer.close();
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getName()
    {
        return _displayName;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean b)
    {
        _active = b;
    }

    public void initValuesForMod(Module mod)
    {
        if (_valueListMods.containsKey(mod.getDisplayName().toString()))
        {
            for (Entry<String, String> value : _valueListMods.get(mod.getDisplayName().toString()).entrySet())
            {
                String l_Key = (String)value.getKey();
                String l_Value = (String)value.getValue();
             
                if (l_Key.equalsIgnoreCase("enabled"))
                {
                    if (l_Value.equalsIgnoreCase("true"))
                    {
                        if (!mod.isEnabled())
                            mod.toggleNoSave();
                    }
                    else if (mod.isEnabled())
                        mod.toggle();
                    continue;
                }

                if (l_Key.equalsIgnoreCase("display"))
                {
                    mod.displayName = l_Value;
                    continue;
                }

                if (l_Key.equalsIgnoreCase("keybind"))
                {
                    mod.key = l_Value;
                    continue;
                }
                
                if (l_Key.equalsIgnoreCase("hidden"))
                {
                    mod.hidden = l_Value.equalsIgnoreCase("true");
                    continue;
                }
                
                for (Value l_Val : mod.valueList)
                {
                    if (l_Val.getName().equalsIgnoreCase((String) value.getKey()))
                    {
                        if (l_Val.getValue() instanceof Number && !(l_Val.getValue() instanceof Enum))
                        {
                            if (l_Val.getValue() instanceof Integer)
                                l_Val.SetForcedValue(Integer.parseInt(l_Value));
                            else if (l_Val.getValue() instanceof Float)
                                l_Val.SetForcedValue(Float.parseFloat(l_Value));
                            else if (l_Val.getValue() instanceof Double)
                                l_Val.SetForcedValue(Double.parseDouble(l_Value));
                        }
                        else if (l_Val.getValue() instanceof Boolean)
                        {
                            l_Val.SetForcedValue(l_Value.equalsIgnoreCase("true"));
                        }
                        else if (l_Val.getValue() instanceof Enum)
                        {
                            l_Val.SetForcedValue(l_Val.GetEnumReal(l_Value));
                        }
                        else if (l_Val.getValue() instanceof String)
                            l_Val.SetForcedValue(l_Value);
                        
                        break;
                    }
                }
            }
        }
    }
}
