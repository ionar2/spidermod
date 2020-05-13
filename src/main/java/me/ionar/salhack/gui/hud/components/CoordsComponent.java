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
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "Mode" }, "Mode of displaying coordinates", Modes.Inline);

    public enum Modes
    {
        Inline, NextLine,
    }

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

        switch (Mode.getValue())
        {
            case Inline:
                String l_Coords = String.format("%sXYZ %s%s, %s, %s", ChatFormatting.GRAY, ChatFormatting.WHITE,
                        format(mc.player.posX), format(mc.player.posY), format(mc.player.posZ));
    
                if (NetherCoords.getValue())
                {
                    l_Coords += String.format(" %s[%s%s, %s%s]", ChatFormatting.GRAY, ChatFormatting.WHITE,
                            mc.player.dimension != -1 ? format(mc.player.posX / 8) : format(mc.player.posX * 8),
                            mc.player.dimension != -1 ? format(mc.player.posZ / 8) : format(mc.player.posZ * 8),
                            ChatFormatting.GRAY);
                }
    
                SetWidth(RenderUtil.getStringWidth(l_Coords));
                SetHeight(RenderUtil.getStringHeight(l_Coords));
    
                RenderUtil.drawStringWithShadow(l_Coords, GetX(), GetY(), -1);
                break;
            case NextLine:
                String l_X = String.format("%sX: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(mc.player.posX), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(mc.player.posX / 8) : format(mc.player.posX * 8) : "");
                String l_Y = String.format("%sY: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(mc.player.posY), NetherCoords.getValue() ? format(mc.player.posY) : "");
                String l_Z = String.format("%sZ: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(mc.player.posZ), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(mc.player.posZ / 8) : format(mc.player.posZ * 8) : "");
                RenderUtil.drawStringWithShadow(l_X, GetX(), GetY(), -1);
                RenderUtil.drawStringWithShadow(l_Y, GetX(), GetY()+RenderUtil.getStringHeight(l_X), -1);
                RenderUtil.drawStringWithShadow(l_Z, GetX(), GetY()+RenderUtil.getStringHeight(l_X)+RenderUtil.getStringHeight(l_Y), -1);

                SetWidth(RenderUtil.getStringWidth(l_X));
                SetHeight(RenderUtil.getStringHeight(l_X)*3);
                break;
            default:
                break;
        }

    }
}
