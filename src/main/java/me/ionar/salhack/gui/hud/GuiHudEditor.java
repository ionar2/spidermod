package me.ionar.salhack.gui.hud;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.managers.HudManager;

public class GuiHudEditor extends SalGuiScreen
{
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
            }
        });
        
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
                    break;
            }
        }
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
            }
        });
    }
    
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
}
