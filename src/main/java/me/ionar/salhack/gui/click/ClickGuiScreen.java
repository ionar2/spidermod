package me.ionar.salhack.gui.click;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.gui.click.component.*;
import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentModList;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.ui.ClickGuiModule;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.AbstractGui;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ClickGuiScreen extends SalGuiScreen
{
    private ArrayList<MenuComponent> MenuComponents = new ArrayList<MenuComponent>();
    private SalDynamicTexture Watermark = ImageManager.Get().GetDynamicTexture("SalHackWatermark");
    private SalDynamicTexture BlueBlur = ImageManager.Get().GetDynamicTexture("BlueBlur");

    public ClickGuiScreen(ClickGuiModule p_Mod)
    {
        // COMBAT, EXPLOIT, MOVEMENT, RENDER, WORLD, MISC, HIDDEN, UI
        MenuComponents.add(new MenuComponentModList("Combat", ModuleType.COMBAT, 10, 3, "Shield"));
        MenuComponents.add(new MenuComponentModList("Exploit", ModuleType.EXPLOIT, 120, 3, "skull"));
        // MenuComponents.add(new MenuComponentModList("Hidden", ModuleType.HIDDEN, 320, 3));
        MenuComponents.add(new MenuComponentModList("Misc", ModuleType.MISC, 230, 3, "questionmark"));
        MenuComponents.add(new MenuComponentModList("Movement", ModuleType.MOVEMENT, 340, 3, "Arrow"));
        MenuComponents.add(new MenuComponentModList("Render", ModuleType.RENDER, 450, 3, "Eye"));
        MenuComponents.add(new MenuComponentModList("UI", ModuleType.UI, 560, 3, "mouse"));
        MenuComponents.add(new MenuComponentModList("World", ModuleType.WORLD, 670, 3, "blockimg"));
        MenuComponents.add(new MenuComponentModList("Bot", ModuleType.BOT, 780, 3, "robotimg"));
        MenuComponents.add(new MenuComponentModList("Schematica", ModuleType.SCHEMATICA, 10, 203, "robotimg"));

        ClickGuiMod = p_Mod;
    }

    private ClickGuiModule ClickGuiMod;

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        for (MenuComponent l_Menu : MenuComponents)
        {
            if (l_Menu.MouseClicked(mouseX, mouseY, mouseButton))
                break;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state)
    {
        for (MenuComponent l_Menu : MenuComponents)
        {
            l_Menu.MouseReleased(mouseX, mouseY, state);
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        for (MenuComponent l_Menu : MenuComponents)
        {
            l_Menu.MouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (Watermark != null)
        {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();

            ScaledResolution l_Res = new ScaledResolution(mc);

            mc.renderEngine.bindTexture(Watermark.GetResourceLocation());
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderUtil.drawTexture(0, l_Res.getScaledHeight()-Watermark.GetHeight()-5, Watermark.GetWidth()/2, Watermark.GetHeight()/2, 0, 0, 1, 1);
            
            GlStateManager.popMatrix();
        }
        
        GlStateManager.pushMatrix();
        
        RenderHelper.disableStandardItemLighting();

        MenuComponent l_LastHovered = null;

        for (MenuComponent l_Menu : MenuComponents)
            if (l_Menu.Render(mouseX, mouseY, true, AllowsOverflow()))
                l_LastHovered = l_Menu;

        if (l_LastHovered != null)
        {
            /// Add to the back of the list for rendering
            MenuComponents.remove(l_LastHovered);
            MenuComponents.add(l_LastHovered);
        }

        RenderHelper.enableGUIStandardItemLighting();

        GlStateManager.popMatrix();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        for (MenuComponent l_Menu : MenuComponents)
        {
            l_Menu.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (ClickGuiMod.isEnabled())
            ClickGuiMod.toggle();
    }

    public boolean AllowsOverflow()
    {
        return ClickGuiMod.AllowOverflow.getValue();
    }
}
