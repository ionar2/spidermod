package me.ionar.salhack.gui.click.component.item;

import java.math.BigDecimal;

import org.lwjgl.input.Keyboard;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.render.RenderUtil;

public class ComponentItemValue extends ComponentItem
{
    final Value Val;
    private boolean IsDraggingSlider = false;
    private Timer timer = new Timer();
    private String DisplayString = "";
    private boolean _isEditingString = false;

    public ComponentItemValue(final Value p_Val, String p_DisplayText, String p_Description, int p_Flags, int p_State, ComponentItemListener p_Listener, float p_Width,
            float p_Height)
    {
        super(p_DisplayText, p_Description, p_Flags, p_State, p_Listener, p_Width, p_Height);
        Val = p_Val;

        if (p_Val.getValue() instanceof Number && !(p_Val.getValue() instanceof Enum))
        {
            Flags |= ComponentItem.Slider;
            Flags |= ComponentItem.DontDisplayClickableHighlight;
            Flags |= ComponentItem.RectDisplayAlways;

            this.SetCurrentWidth(CalculateXPositionFromValue(p_Val));
        }
        else if (p_Val.getValue() instanceof Boolean)
        {
            Flags |= ComponentItem.Boolean;
            Flags |= ComponentItem.RectDisplayOnClicked;
            Flags |= ComponentItem.DontDisplayClickableHighlight;

            if ((Boolean) p_Val.getValue())
                State |= ComponentItem.Clicked;
        }
        else if (p_Val.getValue() instanceof Enum)
        {
            Flags |= ComponentItem.Enum;
            Flags |= ComponentItem.DontDisplayClickableHighlight;
            Flags |= ComponentItem.RectDisplayAlways;
        }
        else if (p_Val.getValue() instanceof String)
            Flags |= ComponentItem.Enum;
    }

    private void SetCurrentWidth(float p_Width)
    {
        CurrentWidth = p_Width;
    }

    @Override
    public void Update()
    {
    }

    @Override
    public boolean HasState(int p_State)
    {
        if ((p_State & ComponentItem.Clicked) != 0)
            return Val.getValue() instanceof Boolean ? (Boolean) Val.getValue() : true;

        return super.HasState(p_State);
    }

    public float CalculateXPositionFromValue(final Value p_Val)
    {
        float l_MinX = GetX();
        float l_MaxX = GetX() + GetWidth();

        if (p_Val.getMax() == null)
            return l_MinX;

        Number l_Val = (Number) p_Val.getValue();
        Number l_Max = (Number) p_Val.getMax();

        return (l_MaxX - l_MinX) * (l_Val.floatValue() / l_Max.floatValue());
    }

    @Override
    public String GetDisplayText()
    {
        if (Val.getValue() instanceof Boolean)
        {
            String l_DisplayText = Val.getName();
            
            if (HasState(ComponentItem.Hovered) && RenderUtil.getStringWidth(l_DisplayText) > GetWidth() - 3)
            {
                if (DisplayString == null)
                    DisplayString = Val.getName();

                l_DisplayText = DisplayString;
                float l_Width = RenderUtil.getStringWidth(l_DisplayText);

                while (l_Width > GetWidth() - 3)
                {
                    l_Width = RenderUtil.getStringWidth(l_DisplayText);
                    l_DisplayText = l_DisplayText.substring(0, l_DisplayText.length() - 1);
                }

                if (timer.passed(75) && DisplayString.length() > 0)
                {
                    String l_FirstChar = String.valueOf(DisplayString.charAt(0));

                    DisplayString = DisplayString.substring(1) + l_FirstChar;

                    timer.reset();
                }

                return l_DisplayText;
            }
            else
                DisplayString = null;

            float l_Width = RenderUtil.getStringWidth(l_DisplayText);

            while (l_Width > GetWidth() - 3)
            {
                l_Width = RenderUtil.getStringWidth(l_DisplayText);
                l_DisplayText = l_DisplayText.substring(0, l_DisplayText.length() - 1);
            }

            return l_DisplayText;
        }

        String l_DisplayText = Val.getName() + " " + Val.getValue().toString() + " ";
        
        if (HasState(ComponentItem.Hovered) && RenderUtil.getStringWidth(l_DisplayText) > GetWidth() - 3)
        {
            if (DisplayString == null)
                DisplayString = Val.getName() + " " + Val.getValue().toString() + " ";

            l_DisplayText = DisplayString;
            float l_Width = RenderUtil.getStringWidth(l_DisplayText);

            while (l_Width > GetWidth() - 3)
            {
                l_Width = RenderUtil.getStringWidth(l_DisplayText);
                l_DisplayText = l_DisplayText.substring(0, l_DisplayText.length() - 1);
            }

            if (timer.passed(75) && DisplayString.length() > 0)
            {
                String l_FirstChar = String.valueOf(DisplayString.charAt(0));

                DisplayString = DisplayString.substring(1) + l_FirstChar;

                timer.reset();
            }

            return l_DisplayText;
        }
        else
            DisplayString = null;

        float l_Width = RenderUtil.getStringWidth(l_DisplayText);

        while (l_Width > GetWidth() - 3)
        {
            l_Width = RenderUtil.getStringWidth(l_DisplayText);
            l_DisplayText = l_DisplayText.substring(0, l_DisplayText.length() - 1);
        }

        return l_DisplayText;
    }

    @Override
    public void OnMouseClick(int p_MouseX, int p_MouseY, int p_MouseButton)
    {
        super.OnMouseClick(p_MouseX, p_MouseY, p_MouseButton);

        if (Val.getValue() instanceof Enum)
            Val.setEnumValue(Val.GetNextEnumValue(p_MouseButton == 1));
        else if (Val.getValue() instanceof String)
        {
            _isEditingString = !_isEditingString;
            Val.setValue("");
        }
        else if (Val.getValue() instanceof Boolean)
        {
            Val.setValue(!(Boolean) Val.getValue());
        }
        else
        {
            IsDraggingSlider = !IsDraggingSlider;
        }

        // SalHack.INSTANCE.getNotificationManager().addNotification(Mod.getDisplayName(), "Changed the value of " + Val.getName() + " to " + Val.getValue().toString());
    }

    @Override
    public void OnMouseRelease(int p_MouseX, int p_MouseY)
    {
        if (IsDraggingSlider)
        {
            IsDraggingSlider = false;
            // SalHack.INSTANCE.getNotificationManager().addNotification(Mod.getDisplayName(), "Changed the value of " + Val.getName() + " to " + Val.getValue().toString());
        }
    }

    @Override
    public void OnMouseMove(float p_MouseX, float p_MouseY, float p_X, float p_Y)
    {
        if (!HasFlag(ComponentItem.Slider))
            return;

        if (!IsDraggingSlider)
            return;

        float l_X = p_X + GetX();

        if (p_MouseX >= l_X && p_MouseX <= p_X + GetX() + GetWidth())
            l_X = p_MouseX;

        if (p_MouseX > p_X + GetX() + GetWidth())
            l_X = p_X + GetX() + GetWidth();

        l_X -= p_X;

        SetCurrentWidth(l_X - GetX());
        // Slider.SetX(l_X - GetX());

        float l_Pct = (l_X - GetX()) / GetWidth();

        // stupid hacks below because java sux it shd rly static assert or make compile error instead of crash when it reach this point lol
        // could also fix all values but meh..

        if (Val.getValue().getClass() == Float.class)
        {
            BigDecimal l_Decimal = new BigDecimal(
                    (this.Val.getMax().getClass() == Float.class ? (Float) this.Val.getMax() : this.Val.getMax().getClass() == Double.class ? (Double) this.Val.getMax() : (Integer) Val.getMax())
                            * l_Pct);

            this.Val.setValue(l_Decimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue());
        }
        else if (Val.getValue().getClass() == Double.class)
        {
            BigDecimal l_Decimal = new BigDecimal(
                    (this.Val.getMax().getClass() == Double.class ? (Double) this.Val.getMax() : this.Val.getMax().getClass() == Float.class ? (Float) this.Val.getMax() : (Integer) Val.getMax())
                            * l_Pct);

            this.Val.setValue(l_Decimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
        }
        else if (Val.getValue().getClass() == Integer.class)
            this.Val.setValue((int) ((int) this.Val.getMax() * l_Pct));
        // salhack.INSTANCE.logChat("Calculated Pct is " + (l_X-GetX())/GetWidth());
    }
    
    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        if (_isEditingString)
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_RETURN))
            {
                _isEditingString = false;
                return;
            }
            
            String string = (String)Val.getValue();
            
            if (string == null)
                return;
            
            if (Keyboard.isKeyDown(Keyboard.KEY_BACK))
            {
                if (string.length() > 0)
                    string = string.substring(0, string.length() - 1);
            }
            else if (Character.isDigit(typedChar) || Character.isLetter(typedChar))
                string += typedChar;
            
            Val.setValue(string);;
        }
    }
}
