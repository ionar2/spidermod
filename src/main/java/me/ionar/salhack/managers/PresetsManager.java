package me.ionar.salhack.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentPresetsList;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.preset.Preset;
import net.minecraft.entity.item.EntityEnderCrystal;

public class PresetsManager
{
    private List<Preset> _presets = new CopyOnWriteArrayList<>();
    private MenuComponentPresetsList _presetList;
    
    public void LoadPresets()
    {
        try
        {
            File[] directories = new File(DirectoryManager.Get().GetCurrentDirectory() + "/SalHack/Presets/").listFiles(File::isDirectory);
            
            for (File file : directories)
            {
                System.out.println("" + file.getName().toString());
                Preset preset = new Preset(file.getName().toString());
                preset.load(file);
                _presets.add(preset);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        Preset defaultPreset = null;
        boolean alreadyEnabled = false;
        
        for (Preset p : _presets)
        {
            if (p.getName().equalsIgnoreCase("default"))
                defaultPreset = p;
            else if (p.isActive())
            {
                alreadyEnabled = true;
                break;
            }
        }
        
        if (!alreadyEnabled && defaultPreset != null)
        {
            defaultPreset.setActive(true);
        }
    }
    
    public void CreatePreset(String presetName)
    {
        try
        {
            new File(DirectoryManager.Get().GetCurrentDirectory() + "/SalHack/Presets/" + presetName).mkdirs();
            new File(DirectoryManager.Get().GetCurrentDirectory() + "/SalHack/Presets/" + presetName + "/Modules").mkdirs();
            Preset preset = new Preset(presetName);
            _presets.add(preset);
            preset.initNewPreset();
            preset.save();
            SetPresetActive(preset);
            
            if (_presetList != null)
            {
                _presetList.AddPreset(preset);
            }
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void RemovePreset(String presetName)
    {
        Preset toRemove = null;
        
        for (Preset p : _presets)
        {
            if (p.getName().equalsIgnoreCase(presetName))
            {
                toRemove = p;
                break;
            }
        }
        
        if (toRemove != null)
        {
            try
            {
                FileUtils.deleteDirectory(new File(DirectoryManager.Get().GetCurrentDirectory() + "/SalHack/Presets/" + toRemove.getName()));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            _presets.remove(toRemove);
            if (_presetList != null)
            {
                _presetList.RemovePreset(toRemove);
            }
        }
    }
    
    public Preset getActivePreset()
    {
        for (Preset p : _presets)
        {
            if (p.isActive())
                return p;
        }
        
        // default MUST always be available
        return _presets.get(0);
    }

    public void SetPresetActive(Preset preset)
    {
        for (Preset p : _presets)
        {
            p.setActive(false);
        }
        
        preset.setActive(true);

        ModuleManager.Get().Mods.forEach(mod ->
        {
            preset.initValuesForMod(mod);
        });
    }
    
    public final List<Preset> GetItems()
    {
        return _presets;
    }
    
    public static PresetsManager Get()
    {
        return SalHack.GetPresetsManager();
    }

    public void InitalizeGUIComponent(MenuComponentPresetsList presetList)
    {
        _presetList = presetList;
    }
}
