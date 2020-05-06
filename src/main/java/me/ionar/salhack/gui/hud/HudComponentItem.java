package me.ionar.salhack.gui.hud;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;

import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.CommandManager;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class HudComponentItem
{
    public ArrayList<Value> ValueList = new ArrayList<Value>();
    private String DisplayName;
    private float X;
    private float Y;
    private float DefaultX;
    private float DefaultY;
    private float Width;
    private float Height;

    protected float DeltaX;
    protected float DeltaY;
    protected float ClampX;
    protected float ClampY;
    private int Flags;

    private boolean Hidden = true;
    private boolean Dragging = false;
    protected boolean Clamped = false;
    protected int Side = 0;
    private boolean Selected = false;
    private boolean MultiSelectedDragging = false;

    protected Minecraft mc = Wrapper.GetMC();
    
    public HudComponentItem(String p_DisplayName, float p_X, float p_Y)
    {
        DisplayName = p_DisplayName;
        X = p_X;
        Y = p_Y;
        DefaultX = p_X;
        DefaultY = p_Y;
    }

    public String GetDisplayName()
    {
        return DisplayName;
    }

    public void SetWidth(float p_Width)
    {
        Width = p_Width;
    }

    public void SetHeight(float p_Height)
    {
        Height = p_Height;
    }

    public float GetWidth()
    {
        return Width;
    }

    public float GetHeight()
    {
        return Height;
    }

    public boolean IsHidden()
    {
        return Hidden;
    }

    public void SetHidden(boolean p_Hide)
    {
        Hidden = p_Hide;

        HudManager.Get().ScheduleSave(this);
    }

    public float GetX()
    {
        return X;
    }

    public float GetY()
    {
        return Y;
    }

    public void SetX(float p_X)
    {
        if (X == p_X)
            return;
        
        X = p_X;
        
        if (!Clamped)
            HudManager.Get().ScheduleSave(this);
    }

    public void SetY(float p_Y)
    {
        if (Y == p_Y)
            return;
        
        Y = p_Y;

        if (!Clamped)
            HudManager.Get().ScheduleSave(this);
    }

    public boolean IsDragging()
    {
        return Dragging;
    }

    public void SetDragging(boolean p_Dragging)
    {
        Dragging = p_Dragging;
    }

    protected void SetClampPosition(float p_X, float p_Y)
    {
        ClampX = p_X;
        ClampY = p_Y;
    }

    protected void SetClamped(boolean p_Clamped)
    {
        Clamped = p_Clamped;
    }

    /// don't override unless you return this
    public boolean Render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        boolean l_Inside = p_MouseX >= GetX() && p_MouseX < GetX() + GetWidth() && p_MouseY >= GetY() && p_MouseY < GetY() + GetHeight();

        if (l_Inside)
        {
            RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x50384244);
        }
        
        if (IsDragging())
        {
            ScaledResolution l_Res = new ScaledResolution(mc);
            
            float l_X = p_MouseX - DeltaX;
            float l_Y = p_MouseY - DeltaY;
            
            SetX(Math.min(Math.max(0, l_X), l_Res.getScaledWidth()-GetWidth()));
            SetY(Math.min(Math.max(0, l_Y), l_Res.getScaledHeight()-GetHeight()));
        }
        /*else if (Clamped)
        {
            SetX(ClampX);
            SetY(ClampY);
        }*/

        render(p_MouseX, p_MouseY, p_PartialTicks);
        
        if (IsSelected())
        {
            RenderUtil.drawRect(GetX(), GetY(),
                    GetX() + GetWidth(), GetY() + GetHeight(),
                    0x35DDDDDD);
        }

        return l_Inside;
    }
    
    /// override for childs
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        
    }

    public boolean OnMouseClick(int p_MouseX, int p_MouseY, int p_MouseButton)
    {
        if (p_MouseX >= GetX() && p_MouseX < GetX() + GetWidth() && p_MouseY >= GetY() && p_MouseY < GetY() + GetHeight())
        {
            if (p_MouseButton == 0)
            {
                SetDragging(true);
                DeltaX = p_MouseX - GetX();
                DeltaY = p_MouseY - GetY();

                HudManager.Get().Items.forEach(p_Item ->
                {
                    if (p_Item.IsMultiSelectedDragging())
                    {
                        p_Item.SetDragging(true);
                        p_Item.SetDeltaX(p_MouseX - p_Item.GetX());
                        p_Item.SetDeltaY(p_MouseY - p_Item.GetY());
                    }
                });
            }
            else if (p_MouseButton == 1)
            {
                ++Side;
                
                if (Side > 3)
                    Side = 0;
                
                HudManager.Get().ScheduleSave(this);
            }
            else if (p_MouseButton == 2)
            {
                SetClamped(!IsClamped());
                SetClampPosition(GetX(), GetY());
                HudManager.Get().ScheduleSave(this);
            }
            
            return true;
        }
        
        return false;
    }

    public void SetDeltaX(float p_X)
    {
        DeltaX = p_X;
    }

    public void SetDeltaY(float p_Y)
    {
        DeltaY = p_Y;
    }

    public void OnMouseRelease(int p_MouseX, int p_MouseY, int p_State)
    {
        SetDragging(false);
    }

    public void LoadSettings()
    {
        File l_Exists = new File("SalHack/HUD/" + GetDisplayName() + ".json");
        if (!l_Exists.exists())
            return;
        
        try 
        {
            // create Gson instance
            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get("SalHack/HUD/" + GetDisplayName() + ".json"));

            // convert JSON file to map
            Map<?, ?> map = gson.fromJson(reader, Map.class);
            
            // print map entries
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                String l_Key = (String)entry.getKey();
                String l_Value = (String)entry.getValue();

                if (l_Key.equalsIgnoreCase("displayname"))
                {
                    SetDisplayName(l_Value, false);
                    continue;
                }
                
                if (l_Key.equalsIgnoreCase("visible"))
                {
                    SetHidden(l_Value.equalsIgnoreCase("false"));
                    continue;
                }

                if (l_Key.equalsIgnoreCase("PositionX"))
                {
                    SetX(Float.parseFloat(l_Value));
                    continue;
                }

                if (l_Key.equalsIgnoreCase("PositionY"))
                {
                    SetY(Float.parseFloat(l_Value));
                    continue;
                }
                
                if (l_Key.equalsIgnoreCase("Clamped"))
                {
                    SetClamped(l_Value.equalsIgnoreCase("true"));
                    continue;
                }

                if (l_Key.equalsIgnoreCase("ClampPositionX"))
                {
                    ClampX = (Float.parseFloat(l_Value));
                    continue;
                }

                if (l_Key.equalsIgnoreCase("ClampPositionY"))
                {
                    ClampY = (Float.parseFloat(l_Value));
                    continue;
                }
                
                if (l_Key.equalsIgnoreCase("Side"))
                {
                    Side = Integer.parseInt(l_Value);
                    continue;
                }
                
                for (Value l_Val : ValueList)
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

    public int GetSide()
    {
        return Side;
    }

    public boolean IsClamped()
    {
        return Clamped;
    }
    
    public boolean HasFlag(int p_Flag)
    {
        return (Flags & p_Flag) != 0;
    }
    
    public void AddFlag(int p_Flags)
    {
        Flags |= p_Flags;
    }
    
    public static int OnlyVisibleInHudEditor = 0x1;

    public void ResetToDefaultPos()
    {
        SetX(DefaultX);
        SetY(DefaultY);
    }

    public void SetSelected(boolean p_Selected)
    {
        Selected = p_Selected;
    }

    public boolean IsInArea(float p_MouseX1, float p_MouseX2, float p_MouseY1, float p_MouseY2)
    {
        return GetX() >= p_MouseX1 && GetX()+GetWidth() <= p_MouseX2 && GetY() >= p_MouseY1 && GetY()+GetHeight() <= p_MouseY2;
    }

    public boolean IsSelected()
    {
        return Selected;
    }

    public void SetMultiSelectedDragging(boolean b)
    {
        MultiSelectedDragging = b;
    }
    
    public boolean IsMultiSelectedDragging()
    {
        return MultiSelectedDragging;
    }

    public void SetDisplayName(String p_NewName, boolean p_Save)
    {
        DisplayName = p_NewName;
        
        if (p_Save)
        {
            HudManager.Get().ScheduleSave(this);
            CommandManager.Get().Reload();
        }
    }
}
