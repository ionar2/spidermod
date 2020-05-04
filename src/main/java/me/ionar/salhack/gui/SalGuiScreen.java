package me.ionar.salhack.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class SalGuiScreen extends GuiScreen
{
   // private NoSlowModule NoSlow = null;
    
    private boolean InventoryMoveEnabled()
    {
        /*if (NoSlow == null)
            NoSlow = (NoSlowModule)SalHack.INSTANCE.getModuleManager().find(NoSlowModule.class);
        
        return NoSlow != null && NoSlow.InventoryMove.getValue();*/
        return true;
    }
    
    public static void UpdateRotationPitch(float p_Amount)
    {
        final Minecraft mc = Minecraft.getMinecraft();

        float l_NewRotation = mc.player.rotationPitch + p_Amount;

        l_NewRotation = Math.max(l_NewRotation, -90.0f);
        l_NewRotation = Math.min(l_NewRotation, 90.0f);

        mc.player.rotationPitch = l_NewRotation;
    }

    public static void UpdateRotationYaw(float p_Amount)
    {
        final Minecraft mc = Minecraft.getMinecraft();

        float l_NewRotation = mc.player.rotationYaw + p_Amount;

        // l_NewRotation = Math.min(l_NewRotation, -360.0f);
        // l_NewRotation = Math.max(l_NewRotation, 360.0f);

        mc.player.rotationYaw = l_NewRotation;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        if (!InventoryMoveEnabled())
            return;

        if (Keyboard.isKeyDown(200))
        {
            UpdateRotationPitch(-2.5f);
        }
        if (Keyboard.isKeyDown(208))
        {
            UpdateRotationPitch(2.5f);
        }
        if (Keyboard.isKeyDown(205))
        {
            UpdateRotationYaw(2.5f);
        }

        if (Keyboard.isKeyDown(203))
        {
            UpdateRotationYaw(-2.5f);
        }
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
    }
    
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
}
