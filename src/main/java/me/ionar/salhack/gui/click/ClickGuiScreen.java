package me.ionar.salhack.gui.click;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.gui.click.component.*;
import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentModList;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.ui.ClickGuiModule;
import me.ionar.salhack.module.ui.ColorsModule;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public class ClickGuiScreen extends SalGuiScreen
{
    private ArrayList<MenuComponent> MenuComponents = new ArrayList<MenuComponent>();
    private SalDynamicTexture Watermark = ImageManager.Get().GetDynamicTexture("SalHackWatermark");
    private SalDynamicTexture BlueBlur = ImageManager.Get().GetDynamicTexture("BlueBlur");
    
    private float OffsetY = 0;

    public ClickGuiScreen(ClickGuiModule p_Mod, ColorsModule p_Colors)
    {
        // COMBAT, EXPLOIT, MOVEMENT, RENDER, WORLD, MISC, HIDDEN, UI
        MenuComponents.add(new MenuComponentModList("Combat", ModuleType.COMBAT, 10, 3, "Shield", p_Colors, p_Mod));
        MenuComponents.add(new MenuComponentModList("Exploit", ModuleType.EXPLOIT, 120, 3, "skull", p_Colors, p_Mod));
        // MenuComponents.add(new MenuComponentModList("Hidden", ModuleType.HIDDEN, 320,
        // 3));
        MenuComponents.add(new MenuComponentModList("Misc", ModuleType.MISC, 230, 3, "questionmark", p_Colors, p_Mod));
        MenuComponents.add(new MenuComponentModList("Movement", ModuleType.MOVEMENT, 340, 3, "Arrow", p_Colors, p_Mod));
        MenuComponents.add(new MenuComponentModList("Render", ModuleType.RENDER, 450, 3, "Eye", p_Colors, p_Mod));
        MenuComponents.add(new MenuComponentModList("UI", ModuleType.UI, 560, 3, "mouse", p_Colors, p_Mod));
        MenuComponents.add(new MenuComponentModList("World", ModuleType.WORLD, 670, 3, "blockimg", p_Colors, p_Mod));
     //   MenuComponents.add(new MenuComponentModList("Bot", ModuleType.BOT, 780, 3, "robotimg", p_Colors));
        MenuComponents
                .add(new MenuComponentModList("Schematica", ModuleType.SCHEMATICA, 10, 203, "robotimg", p_Colors, p_Mod));

        ClickGuiMod = p_Mod;

        /// Load settings
        for (MenuComponent l_Component : MenuComponents)
        {
            File l_Exists = new File("SalHack/GUI/" + l_Component.GetDisplayName() + ".json");
            if (!l_Exists.exists())
                continue;

            try
            {
                // create Gson instance
                Gson gson = new Gson();

                // create a reader
                Reader reader = Files
                        .newBufferedReader(Paths.get("SalHack/GUI/" + l_Component.GetDisplayName() + ".json"));

                // convert JSON file to map
                Map<?, ?> map = gson.fromJson(reader, Map.class);

                for (Map.Entry<?, ?> entry : map.entrySet())
                {
                    String l_Key = (String) entry.getKey();
                    String l_Value = (String) entry.getValue();

                    if (l_Key.equals("PosX"))
                        l_Component.SetX(Float.parseFloat(l_Value));
                    else if (l_Key.equals("PosY"))
                        l_Component.SetY(Float.parseFloat(l_Value));
                }

                reader.close();
            }
            catch (Exception e)
            {

            }
        }
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
            if (l_Menu.MouseClicked(mouseX, mouseY, mouseButton, OffsetY))
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

        if (Watermark != null && ClickGuiMod.Watermark.getValue())
        {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();

            ScaledResolution l_Res = new ScaledResolution(mc);

            mc.renderEngine.bindTexture(Watermark.GetResourceLocation());
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderUtil.drawTexture(0, l_Res.getScaledHeight() - Watermark.GetHeight() - 5, Watermark.GetWidth() / 2,
                    Watermark.GetHeight() / 2, 0, 0, 1, 1);

            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();

        MenuComponent l_LastHovered = null;
        
        for (MenuComponent l_Menu : MenuComponents)
            if (l_Menu.Render(mouseX, mouseY, true, AllowsOverflow(), OffsetY))
                l_LastHovered = l_Menu;
        
        if (l_LastHovered != null)
        {
            /// Add to the back of the list for rendering
            MenuComponents.remove(l_LastHovered);
            MenuComponents.add(l_LastHovered);
        }

        RenderHelper.enableGUIStandardItemLighting();

        GlStateManager.enableRescaleNormal();
        GlStateManager.popMatrix();
        
        int l_Scrolling = Mouse.getEventDWheel();
        
        /// up
        if (l_Scrolling > 0)
        {
            OffsetY = Math.max(0, OffsetY-1);
        }
        /// down
        else if (l_Scrolling < 0)
        {
            OffsetY = Math.min(100, OffsetY + 1);
        }
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

        /// Save Settings

        for (MenuComponent l_Component : MenuComponents)
        {
            try
            {
                GsonBuilder builder = new GsonBuilder();

                Gson gson = builder.setPrettyPrinting().create();

                Writer writer = Files.newBufferedWriter(Paths.get("SalHack/GUI/" + l_Component.GetDisplayName() + ".json"));
                Map<String, String> map = new HashMap<>();

                map.put("PosX", String.valueOf(l_Component.GetX()));
                map.put("PosY", String.valueOf(l_Component.GetY()));

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

    public boolean AllowsOverflow()
    {
        return ClickGuiMod.AllowOverflow.getValue();
    }
}
