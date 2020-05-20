package me.ionar.salhack.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.events.salhack.EventSalHackModuleDisable;
import me.ionar.salhack.events.salhack.EventSalHackModuleEnable;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.gui.click.component.item.ComponentItem;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.CommandManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.Listenable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

public class Module implements Listenable
{ 
    private String displayName;
    private String[] alias;
    private String desc;
    private String key;
    private int color;
    private boolean hidden = false;
    private boolean enabled = false;
    protected boolean EnabledByDefault = false;
    private ModuleType type;
    private boolean m_NeedsClickGuiValueUpdate;
    protected final Minecraft mc = Minecraft.getMinecraft();
    
    private List<Value> valueList = new ArrayList<Value>();
    public float RemainingXAnimation = 0f;

    private Module(String displayName, String[] alias, String key, int color, ModuleType type)
    {
        this.displayName = displayName;
        this.alias = alias;
        this.key = key;
        this.color = color;
        this.type = type;
    }

    public Module(String displayName, String[] alias, String desc, String key, int color, ModuleType type)
    {
        this(displayName, alias, key, color, type);
        this.desc = desc;
    }

    public void onEnable()
    {
        /// allow events to be called
        SalHackMod.EVENT_BUS.subscribe(this);
        
        RemainingXAnimation = RenderUtil.getStringWidth(GetFullArrayListDisplayName())+10f;
        
        ModuleManager.Get().OnModEnable(this);
        
        SalHackMod.EVENT_BUS.post(new EventSalHackModuleEnable(this));
    }

    public void onDisable()
    {
        /// disallow events to be called
        SalHackMod.EVENT_BUS.unsubscribe(this);
        SalHackMod.EVENT_BUS.post(new EventSalHackModuleDisable(this));
    }

    public void onToggle()
    {

    }

    public void toggle()
    {
        this.setEnabled(!this.isEnabled());
        if (this.isEnabled())
        {
            this.onEnable();
        }
        else
        {
            this.onDisable();
        }
        this.onToggle();
        
        SaveSettings();
    }
    
    public void toggleNoSave()
    {
        this.setEnabled(!this.isEnabled());
        if (this.isEnabled())
        {
            this.onEnable();
        }
        else
        {
            this.onDisable();
        }
        this.onToggle();
    }

    public void ToggleOnlySuper()
    {
        this.setEnabled(!this.isEnabled());
        this.onToggle();
    }

    public String getMetaData()
    {
        return null;
    }

    public TextComponentString toUsageTextComponent()
    {
        if (this.valueList.size() <= 0)
        {
            return null;
        }

        final String valuePrefix = " " + ChatFormatting.RESET;
        final TextComponentString msg = new TextComponentString("");
        final DecimalFormat df = new DecimalFormat("#.##");

        for (Value v : this.getValueList())
        {
            if (v.getValue() instanceof Boolean)
            {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ": "
                        + ((Boolean) v.getValue() ? ChatFormatting.GREEN : ChatFormatting.RED) + v.getValue())
                                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD
                                                + ((v.getDesc() == null || v.getDesc().equals(""))
                                                        ? "There is no description for this boolean value."
                                                        : v.getDesc())
                                                + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY
                                                + "<true / false>")))));
            }

            if (v.getValue() instanceof Number && !(v.getValue() instanceof Enum))
            {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <amount>"
                        + ChatFormatting.RESET + ": " + ChatFormatting.AQUA + (df.format(v.getValue())))
                                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD
                                                + ((v.getDesc() == null || v.getDesc().equals(""))
                                                        ? "There is no description for this number value."
                                                        : v.getDesc())
                                                + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<" + v.getMin()
                                                + " - " + v.getMax() + ">")))));
            }

            if (v.getValue() instanceof String)
            {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <text>"
                        + ChatFormatting.RESET + ": " + v.getValue())
                                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD
                                                + ((v.getDesc() == null || v.getDesc().equals(""))
                                                        ? "There is no description for this string value."
                                                        : v.getDesc())
                                                + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<text>")))));
            }

            if (v.getValue() instanceof Enum)
            {
                final Enum val = (Enum) v.getValue();
                final StringBuilder options = new StringBuilder();
                final int size = val.getClass().getEnumConstants().length;

                for (int i = 0; i < size; i++)
                {
                    final Enum option = val.getClass().getEnumConstants()[i];
                    options.append(option.name().toLowerCase() + ((i == size - 1) ? "" : ", "));
                }

                msg.appendSibling(new TextComponentString(
                        valuePrefix + v.getName() + ChatFormatting.GRAY + " <" + options.toString() + ">"
                                + ChatFormatting.RESET + ": " + ChatFormatting.YELLOW + val.name().toLowerCase())
                                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD
                                                        + ((v.getDesc() == null || v.getDesc().equals(""))
                                                                ? "There is no description for this enum value."
                                                                : v.getDesc())
                                                        + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<"
                                                        + options.toString() + ">")))));
            }
        }

        return msg;
    }

    public Value find(String alias)
    {
        for (Value v : this.getValueList())
        {
            for (String s : v.getAlias())
            {
                if (alias.equalsIgnoreCase(s))
                {
                    return v;
                }
            }

            if (v.getName().equalsIgnoreCase(alias))
            {
                return v;
            }
        }
        return null;
    }

    public void unload()
    {
        this.valueList.clear();
    }

    public enum ModuleType
    {
        COMBAT, EXPLOIT, MOVEMENT, RENDER, WORLD, MISC, HIDDEN, UI, BOT, SCHEMATICA
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
        CommandManager.Get().Reload();
        SaveSettings();
    }

    public String[] getAlias()
    {
        return alias;
    }

    public void setAlias(String[] alias)
    {
        this.alias = alias;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getKey()
    {
        return key;
    }
    
    public boolean IsKeyPressed(String p_KeyCode)
    {
        if (GuiScreen.isAltKeyDown() || GuiScreen.isCtrlKeyDown() || GuiScreen.isShiftKeyDown())
        {
            if (key.contains(" + "))
            {
                if (GuiScreen.isAltKeyDown() && key.contains("MENU"))
                {
                    String l_Result = key.replace(Keyboard.isKeyDown(56) ? "LMENU + " : "RMENU + ", "");
                    return l_Result.equals(p_KeyCode);
                }
                else if (GuiScreen.isCtrlKeyDown() && key.contains("CONTROL"))
                {
                    String l_CtrlKey = "";
                    
                    if (Minecraft.IS_RUNNING_ON_MAC)
                        l_CtrlKey = Keyboard.isKeyDown(219) ? "LCONTROL" : "RCONTROL";
                    else
                        l_CtrlKey = Keyboard.isKeyDown(29) ? "LCONTROL" : "RCONTROL";
                    
                    String l_Result = key.replace(l_CtrlKey + " + ", "");
                    return l_Result.equals(p_KeyCode);
                }
                else if (GuiScreen.isShiftKeyDown() && key.contains("SHIFT"))
                {
                    String l_Result = key.replace((Keyboard.isKeyDown(42) ? "LSHIFT" : "RSHIFT") + " + ", "");
                    return l_Result.equals(p_KeyCode);
                }
            }
            
            if (!ModuleManager.Get().IgnoreStrictKeybinds())
            {
                if (p_KeyCode.contains("SHIFT") || p_KeyCode.contains("CONTROL") || p_KeyCode.contains("MENU"))
                    return key.equals(p_KeyCode);
                
                return false;
            }
        }
        
        return key.equals(p_KeyCode);
    }

    public void setKey(String key)
    {
        this.key = key;
        SaveSettings();
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
        SaveSettings();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public ModuleType getType()
    {
        return type;
    }

    public void setType(ModuleType type)
    {
        this.type = type;
    }

    public List<Value> getValueList()
    {
        return valueList;
    }

    public void setValueList(List<Value> valueList)
    {
        this.valueList = valueList;
    }

    public float GetRemainingXArraylistOffset()
    {
        return RemainingXAnimation;
    }

    public void SignalEnumChange()
    {
    }

    public void SignalValueChange(Value p_Val)
    {
        SaveSettings();
    }

    public List<Value> GetVisibleValueList()
    {
        return valueList;
    }

    /// functions for updating value in an async way :)
    public void SetClickGuiValueUpdate(boolean p_Val)
    {
        m_NeedsClickGuiValueUpdate = p_Val;
    }

    public boolean NeedsClickGuiValueUpdate()
    {
        return m_NeedsClickGuiValueUpdate;
    }

    public String GetNextStringValue(final Value<String> p_Val, boolean p_Recursive)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String GetArrayListDisplayName()
    {
        return getDisplayName();
    }
    
    public String GetFullArrayListDisplayName()
    {
        return getDisplayName() + (getMetaData() != null ? " " + ChatFormatting.GRAY + getMetaData() : "");
    }
    
    protected void SendMessage(String p_Message)
    {
        if (mc.player != null)
            SalHack.SendMessage(ChatFormatting.AQUA + "[" + GetArrayListDisplayName() + "]: " + ChatFormatting.RESET + p_Message);
    }
    
    public void LoadSettings()
    {
        File l_Exists = new File("SalHack/Modules/" + getDisplayName() + ".json");
        if (!l_Exists.exists())
        {
            if (EnabledByDefault && !isEnabled())
                toggle();
            return;
        }
        
        try 
        {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("SalHack/Modules/" + getDisplayName() + ".json"));

            // convert JSON file to map
            Map<?, ?> map = gson.fromJson(reader, Map.class);
            
            // print map entries
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                String l_Key = (String)entry.getKey();
                String l_Value = (String)entry.getValue();
             
                if (l_Key.equalsIgnoreCase("enabled"))
                {
                    if (l_Value.equalsIgnoreCase("true"))
                        toggleNoSave();
                    continue;
                }

                if (l_Key.equalsIgnoreCase("display"))
                {
                    displayName = l_Value;
                    continue;
                }

                if (l_Key.equalsIgnoreCase("keybind"))
                {
                    key = l_Value;
                    continue;
                }
                
                if (l_Key.equalsIgnoreCase("hidden"))
                {
                    hidden = l_Value.equalsIgnoreCase("true");
                    continue;
                }
                
                for (Value l_Val : valueList)
                {
                    if (l_Val.getName().equalsIgnoreCase((String) entry.getKey()))
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

            // close reader
            reader.close();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void SaveSettings()
    {
        try
        {
            GsonBuilder builder = new GsonBuilder();
            
            Gson gson = builder.setPrettyPrinting().create();

            Writer writer = Files.newBufferedWriter(Paths.get("SalHack/Modules/" + getDisplayName() + ".json"));
            Map<String, String> map = new HashMap<>();
            
            map.put("enabled", isEnabled() ? "true" : "false");
            map.put("display", getDisplayName());
            map.put("keybind", getKey());
            map.put("hidden", isHidden() ? "true" : "false");
            
            for (Value l_Val : valueList)
            {
                map.put(l_Val.getName().toString(), l_Val.getValue().toString());
            }
            gson.toJson(map, writer);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
