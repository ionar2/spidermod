package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.RenderUtil;

public class CoordsComponent extends HudComponentItem
{
    public final Value<Boolean> NetherCoords = new Value<Boolean>("NetherCoords", new String[]
    { "NC" }, "Displays nether coords.", true);

    final DecimalFormat Formatter = new DecimalFormat("#.#");
    
    public CoordsComponent()
    {
        super("Coords", 2, 245);
    }
    
    public String format(double p_Input)
    {
        String l_Result = Formatter.format(p_Input);
        
        if (!l_Result.contains("."))
            l_Result += ".0";
        
        return l_Result;
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        String l_Coords = String.format("%sXYZ %s%s, %s, %s",
                ChatFormatting.GRAY,
                ChatFormatting.WHITE,
                format(mc.player.posX),
                format(mc.player.posY),
                format(mc.player.posZ));
        
        
        if (NetherCoords.getValue())
        {
            l_Coords += String.format(" %s[%s%s, %s%s]",
                    ChatFormatting.GRAY,
                    ChatFormatting.WHITE,
                    mc.player.dimension != -1 ? format(mc.player.posX / 8) : format(mc.player.posX * 8),
                    mc.player.dimension != -1 ? format(mc.player.posZ / 8) : format(mc.player.posZ * 8),
                    ChatFormatting.GRAY);
        }
        
        SetWidth(RenderUtil.getStringWidth(l_Coords));
        SetHeight(RenderUtil.getStringHeight(l_Coords));

        RenderUtil.drawStringWithShadow(l_Coords, GetX(), GetY(), -1);
    }
}
