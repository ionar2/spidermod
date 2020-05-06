package me.ionar.salhack.gui.hud;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.module.ui.HudEditorModule;
import me.ionar.salhack.util.render.RenderUtil;

public class GuiHudEditor extends SalGuiScreen
{
    public GuiHudEditor(HudEditorModule p_HudEditor)
    {
        super();
        
        HudEditor = p_HudEditor;
    }
    
    private HudEditorModule HudEditor;
    private boolean Clicked = false;
    private boolean Dragging = false;
    private int ClickMouseX = 0;
    private int ClickMouseY = 0;
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        GL11.glPushMatrix();
        
        HudManager.Get().Items.forEach(p_Item ->
        {
            if (!p_Item.IsHidden())
            {
                p_Item.render(mouseX, mouseY, partialTicks);

                if (p_Item.IsSelected())
                {
                    RenderUtil.drawRect(p_Item.GetX(), p_Item.GetY(),
                            p_Item.GetX() + p_Item.GetWidth(), p_Item.GetY() + p_Item.GetHeight(),
                            0x35DDDDDD);
                }
            }
        });
        
        if (Clicked)
        {
            final float l_MouseX1 = Math.min(ClickMouseX, mouseX);
            final float l_MouseX2 = Math.max(ClickMouseX, mouseX);
            final float l_MouseY1 = Math.min(ClickMouseY, mouseY);
            final float l_MouseY2 = Math.max(ClickMouseY, mouseY);
            
            RenderUtil.drawOutlineRect(l_MouseX2, l_MouseY2, l_MouseX1, l_MouseY1, 1, 0x75056EC6);
            RenderUtil.drawRect(l_MouseX1, l_MouseY1, l_MouseX2, l_MouseY2, 0x56EC6, 205);

            HudManager.Get().Items.forEach(p_Item ->
            {
                if (!p_Item.IsHidden())
                {
                    if (p_Item.IsInArea(l_MouseX1, l_MouseX2, l_MouseY1, l_MouseY2))
                        p_Item.SetSelected(true);
                    else if (p_Item.IsSelected())
                        p_Item.SetSelected(false);
                }
            });
        }
        
        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (HudComponentItem l_Item : HudManager.Get().Items)
        {
            if (!l_Item.IsHidden())
            {
                if (l_Item.OnMouseClick(mouseX, mouseY, mouseButton))
                    return;
            }
        }

        Clicked = true;
        ClickMouseX = mouseX;
        ClickMouseY = mouseY;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        
        HudManager.Get().Items.forEach(p_Item ->
        {
            if (!p_Item.IsHidden())
            {
                p_Item.OnMouseRelease(mouseX, mouseY, state);

                if (p_Item.IsSelected())
                    p_Item.SetMultiSelectedDragging(true);
                else
                    p_Item.SetMultiSelectedDragging(false);
            }
        });

        Clicked = false;
    }
    
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (HudEditor.isEnabled())
            HudEditor.toggle();

        Clicked = false;
        Dragging = false;
        ClickMouseX = 0;
        ClickMouseY = 0;
    }
}
