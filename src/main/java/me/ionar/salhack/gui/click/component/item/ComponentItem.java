package me.ionar.salhack.gui.click.component.item;

import java.util.ArrayList;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;

public class ComponentItem
{
    /// Flags
    public static int Clickable = 0x1;
    public static int Hoverable = 0x2;
    public static int Tooltip   = 0x4;
    public static int HasValues = 0x8;
    public static int RectDisplayAlways = 0x10;
    public static int Slider = 0x20;
    public static int Boolean = 0x40;
    public static int Enum = 0x80;
    public static int DontDisplayClickableHighlight = 0x100;
    public static int RectDisplayOnClicked = 0x200;
    
    /// State
    public static int Clicked = 0x1;
    public static int Hovered = 0x2;
    public static int Extended = 0x4;
    
    private String DisplayText;
    private String Description;
    protected int Flags;
    protected int State;
    protected ComponentItemListener Listener;
    private float X;
    private float Y;
    private float Width;
    private float Height;
    protected float CurrentWidth;
    
    public ArrayList<ComponentItem> DropdownItems;
    
    public ComponentItem(String p_DisplayText, String p_Description, int p_Flags, int p_State, ComponentItemListener p_Listener, float p_Width, float p_Height)
    {
        DisplayText = p_DisplayText;
        Description = p_Description;
        Flags = p_Flags;
        State = p_State;
        Listener = p_Listener;
        
        DropdownItems = new ArrayList<ComponentItem>();
        
        X = 0;
        Y = 0;
        Width = p_Width;
        Height = p_Height;
        CurrentWidth = p_Width;
    }
    
    public String GetDisplayText()
    {
        return DisplayText;
    }
    
    public String GetDescription()
    {
        return Description;
    }
    
    public boolean HasFlag(int p_Flag)
    {
        return (Flags & p_Flag) != 0;
    }
    
    public boolean HasState(int p_State)
    {
        return (State & p_State) != 0;
    }
    
    public void AddState(int p_State)
    {
        State |= p_State;
    }
    
    public void RemoveState(int p_State)
    {
        State &= ~p_State;
    }

    public float GetX()
    {
        return X;
    }

    public void SetX(float x)
    {
        X = x;
    }

    public float GetY()
    {
        return Y;
    }

    public void SetY(float y)
    {
        Y = y;
    }

    public float GetWidth()
    {
        return Width;
    }

    public void SetWidth(float width)
    {
        Width = width;
    }

    public float GetHeight()
    {
        return Height;
    }

    public void SetHeight(float height)
    {
        Height = height;
    }
    
    public float GetCurrentWidth()
    {
        return CurrentWidth;
    }

    public void OnMouseClick(int p_MouseX, int p_MouseY, int p_MouseButton)
    {
        if (p_MouseButton == 0)
        {
            if (Listener != null)
                Listener.OnToggled();
            
            if (HasState(Clicked))
                RemoveState(Clicked);
            else
                AddState(Clicked);
        }
        else if (p_MouseButton == 1)
        {
            if (HasState(Extended))
                RemoveState(Extended);
            else
                AddState(Extended);
        }
    }

    public void keyTyped(char typedChar, int keyCode)
    {
    }

    public void OnMouseMove(float p_MouseX, float p_MouseY, float p_X, float p_Y)
    {
    }

    public void Update()
    {
    }

    public void OnMouseRelease(int p_MouseX, int p_MouseY)
    {
        // TODO Auto-generated method stub
        
    }

    public void OnMouseClickMove(int p_MouseX, int p_MouseY, int p_ClickedMouseButton)
    {
        // TODO Auto-generated method stub
        
    }
}
