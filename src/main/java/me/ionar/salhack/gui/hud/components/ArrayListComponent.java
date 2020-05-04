package me.ionar.salhack.gui.hud.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class ArrayListComponent extends HudComponentItem
{
    public final Value<Boolean> RainbowVal = new Value<Boolean>("Rainbow", new String[]
    { "" }, "Makes a dynamic rainbow", true);
    public final Value<Boolean> NoBackground = new Value<Boolean>("NoBackground", new String[]
    { "" }, "NoBackground on arraylist", false);

    public ArrayListComponent()
    {
        super("ArrayList", 0, 0);
        SetHidden(false);
        Clamped = true;
    }

    private HashMap<Module, String> m_StaticModuleNames = new HashMap<Module, String>();
    private Timer ReorderTimer = new Timer();
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);

    public String GenerateModuleDisplayName(final Module p_Mod, final boolean p_WithColor)
    {
        String l_DisplayName = p_Mod.GetArrayListDisplayName();

        if (p_Mod.getMetaData() != null)
        {
            l_DisplayName += " " + ChatFormatting.GRAY + p_Mod.getMetaData();
        }

        return l_DisplayName;
    }

    public String GetStaticModuleNames(final Module p_Mod)
    {
        if (!m_StaticModuleNames.containsKey(p_Mod))
            m_StaticModuleNames.put(p_Mod, GenerateModuleDisplayName(p_Mod, false));
        return m_StaticModuleNames.get(p_Mod);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        super.render(mouseX, mouseY, partialTicks);

        final ArrayList<Module> mods = new ArrayList<>();

        for (Module mod : ModuleManager.Get().GetModuleList())
        {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden())
            {
                mods.add(mod);
            }
        }

        if (ReorderTimer.passed(1000))
        {
            ReorderTimer.reset();
            m_StaticModuleNames.clear();
        }

        final Comparator<Module> comparator = (first, second) ->
        {
            final String firstName = GetStaticModuleNames(first);
            final String secondName = GetStaticModuleNames(second);
            final float dif = RenderUtil.getStringWidth(secondName) - RenderUtil.getStringWidth(firstName);
            return dif != 0 ? (int) dif : secondName.compareTo(firstName);
        };

        mods.sort(comparator);

        float xOffset = 0f;
        float yOffset = 0f;
        float maxWidth = 0f;
        
        Rainbow.OnRender();

        int l_I = 0;
        
        final float textheight = 10.5f;

        for (Module mod : mods)
        {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden())
            {
                String name = GenerateModuleDisplayName(mod, true);

                final float width = RenderUtil.getStringWidth(name);

                if (width >= maxWidth)
                    maxWidth = width;
                
                float l_StringYHeight = 12;

                switch (Side)
                {
                    case 0:
                    case 1:
                        xOffset = GetWidth() - RenderUtil.getStringWidth(name);
                        break;
                    case 2:
                    case 3:
                        xOffset = 0;
                        xOffset = 0;
                        break;
                }

                l_I += 20;
                
                if (l_I >= 355)
                    l_I = 0;

                switch (Side)
                {
                    case 0:
                    case 3:
                    	if (!NoBackground.getValue())
                    	{
	                        RenderUtil.drawRect(GetX() + xOffset - 2 + mod.GetRemainingXArraylistOffset(),
	                                GetY() + yOffset,
	                                GetX() + xOffset + RenderUtil.getStringWidth(name) + 4 + mod.GetRemainingXArraylistOffset(),
	                                GetY() + yOffset + (l_StringYHeight + 1.5f),
	                                0x75101010);
                    	}
                        RenderUtil.drawStringWithShadow(name,
                                (GetX() + xOffset) + mod.GetRemainingXArraylistOffset(),
                                GetY() + yOffset,
                                RainbowVal.getValue() ? Rainbow.GetRainbowColorAt(l_I) : mod.getColor());
                        yOffset += (l_StringYHeight + 1.5f);
                        break;
                    case 1:
                    case 2:
                    	if (!NoBackground.getValue())
                    	{
	                        RenderUtil.drawRect(GetX() + xOffset - 2 + mod.GetRemainingXArraylistOffset(),
	                                GetY() + (GetHeight() - l_StringYHeight) + yOffset,
	                                GetX() + xOffset + RenderUtil.getStringWidth(name) + 4 + mod.GetRemainingXArraylistOffset(),
	                                GetY() + (GetHeight() - l_StringYHeight) + yOffset+ (l_StringYHeight + 1.5f),
	                                0x75101010);
                    	}
                        RenderUtil.drawStringWithShadow(name,
                                (GetX() + xOffset) + mod.GetRemainingXArraylistOffset(),
                                GetY() + (GetHeight() - l_StringYHeight) + yOffset,
                                RainbowVal.getValue() ? Rainbow.GetRainbowColorAt(l_I) : mod.getColor());
                        yOffset -= (l_StringYHeight + 1.5f);
                        break;
                }
            }
        }
        
        if (Clamped)
        { 
            ScaledResolution l_Res = new ScaledResolution(mc);
            
            switch (Side)
            {
                case 0: 
                    SetX(l_Res.getScaledWidth()-maxWidth+8.0f);
                    SetY(1);
                    break;
                case 1: 
                    SetX(l_Res.getScaledWidth()-maxWidth+8.0f);
                    SetY(l_Res.getScaledHeight()+yOffset);
                    break;
                case 2: 
                    SetX(1);
                    SetY(l_Res.getScaledHeight()+yOffset);
                    break;
                case 3: 
                    SetX(1);
                    SetY(1);
                    break;
            }
        }

        SetWidth(maxWidth-10.0f);
        SetHeight(Math.abs(yOffset));
    }
}
