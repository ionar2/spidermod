package me.ionar.salhack.gui.hud.components;

import org.apache.commons.lang3.StringUtils;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.render.RenderUtil;

public class TabGUIComponent extends HudComponentItem
{
    public TabGUIComponent()
    {
        super("TabGUI", 3, 12);
     //   SetHidden(false);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);
        
        int l_Y = 0;
        
        RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x99443F3F);
        RenderUtil.drawOutlineRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 3, 0x443F3F);
        
        String[] l_Array = {"Combat", "Exploits", "Miscellaneous", "Movement", "Render", "World" };
        
        float l_MaxWidth = 0;
        
        String l_Hovered = "Combat";
        
        for (String l_String : l_Array)
        {
            if (l_Hovered == l_String)
            {
                RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+12, 0x9902C9FF);
                RenderUtil.drawOutlineRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+12, 3, 0x99443F3F);
            }
            
            float l_Width = RenderUtil.drawStringWithShadow(l_String, GetX()+2, GetY()+l_Y, 0xD1D1D1);
            
            if (l_Width >= l_MaxWidth)
                l_MaxWidth = l_Width;
            
            l_Y += 11;
        }
        
        SetWidth(l_MaxWidth+3.5f);
        SetHeight(l_Y);
    }
}
