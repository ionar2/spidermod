package me.ionar.salhack.gui.hud.components;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FontManager;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.AbstractGui;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class KeyStrokesComponent extends HudComponentItem
{
    public final Value<Float> Red = new Value<Float>("Red", new String[] {"bRed"}, "Red for rendering", 1.0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Green = new Value<Float>("Green", new String[] {"bGreen"}, "Green for rendering", 1.0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Blue = new Value<Float>("Blue", new String[] {"bBlue"}, "Blue for rendering", 1.0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Alpha = new Value<Float>("Alpha", new String[] {"bAlpha"}, "Alpha for rendering", 1.0f, 0f, 1.0f, 0.1f);
    
    private ArrayList<Button> Buttons = new ArrayList<Button>();

    public KeyStrokesComponent()
    {
        super("KeyStrokes", 2, 300);

        Buttons.add(new Button("W", 35, 35));
        Buttons.add(new Button("S", 35, 35));
        Buttons.add(new Button("A", 35, 35));
        Buttons.add(new Button("D", 35, 35));
        Buttons.add(new Button("SPACE", 115, 35));
    }

    public class Button
    {
        private String Name;
        public float Width;
        public float Height;
        
        public final Timer timer = new Timer();
        public final Timer pressedTimer = new Timer();
        public float RemainingAnimationW = 0;
        public float RemainingAnimationH = 0;
        boolean Pressed = false;

        public Button(String p_Name, float p_Width, float p_Height)
        {
            Name = p_Name;
            Width = p_Width;
            Height = p_Height;
        }

        public void Display(float p_X, float p_Y)
        {
            if (pressedTimer.passed(50))
            {
                pressedTimer.reset();
                Pressed = false;
            }
            
            if (timer.passed(1))
            {
                timer.reset();
                if(++RemainingAnimationW >= Width);
                {
                    RemainingAnimationW = Width;
                }
                if(++RemainingAnimationH >= Height);
                {
                    RemainingAnimationH = Height;
                }
            }
            
            RenderUtil.drawRect(p_X, p_Y, p_X + Width, p_Y + Height, 0x75101010);

            if (Pressed)
            {
                SalDynamicTexture l_Texture = ImageManager.Get().GetDynamicTexture("OutlinedEllipse");
    
                mc.renderEngine.bindTexture(l_Texture.GetResourceLocation());
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                
                GL11.glColor4f(Red.getValue(), Green.getValue(), Blue.getValue(), Alpha.getValue());
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    
                // (int x, int y, int width, int height, float minU, float minV, int maxU, int maxV, int uScale, int vScale)
                AbstractGui.blit((int)(p_X), (int)(p_Y), (int)Width, (int)Height, 0f, 0f, (int)Width, (int)Height, 1, 1);
                
                GlStateManager.disableBlend();

                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
            }
            
            FontManager.Get().GetFontBySize(24).drawCenteredString(Name, p_X+Width/2, p_Y+Height/2-5, 0xFFFFFF);
        }

        public void OnPress()
        {
            pressedTimer.resetTimeSkipTo(30);
            timer.resetTimeSkipTo(30);
            Pressed = true;
            RemainingAnimationW = 0;
            RemainingAnimationH = 0;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        super.render(mouseX, mouseY, partialTicks);
        // RenderUtil.drawRect(this.GetX(), this.GetY(), this.GetX() + this.getW(),
        // this.GetY() + this.getH(), 0x90222222);

        boolean l_Forward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean l_Left = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean l_Right = mc.gameSettings.keyBindRight.isKeyDown();
        boolean l_Back = mc.gameSettings.keyBindBack.isKeyDown();
        boolean l_Jump = mc.gameSettings.keyBindJump.isKeyDown();

        if (l_Forward)
            Buttons.get(0).OnPress();
        if (l_Back)
            Buttons.get(1).OnPress();
        if (l_Left)
            Buttons.get(2).OnPress();
        if (l_Right)
            Buttons.get(3).OnPress();
        if (l_Jump)
            Buttons.get(4).OnPress();

        Buttons.get(0).Display(GetX() + 40, GetY());
        Buttons.get(2).Display(GetX(), GetY() + 40);
        Buttons.get(1).Display(GetX() + 40, GetY() + 40);
        Buttons.get(3).Display(GetX() + 80, GetY() + 40);
        Buttons.get(4).Display(GetX(), GetY() + 80);

        this.SetWidth(120);
        this.SetHeight(120);
    }
}
