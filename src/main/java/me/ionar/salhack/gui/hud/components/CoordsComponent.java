package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

import java.text.DecimalFormat;

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

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        switch (Mode.getValue())
        {
            case Inline:
                String l_Coords = l_Hud.Rainbow.getValue() ? String.format("XYZ %s, %s, %s",
                        format(getX()), format(mc.player.posY), format(getZ()))
                        : String.format("%sXYZ %s%s, %s, %s", ChatFormatting.GRAY, ChatFormatting.WHITE,
                        format(getX()), format(mc.player.posY), format(getZ()));

                if (NetherCoords.getValue())
                {
                    l_Coords += l_Hud.Rainbow.getValue() ? String.format(" [%s, %s]",
                            mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8),
                            mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8))
                            : String.format(" %s[%s%s, %s%s]", ChatFormatting.GRAY, ChatFormatting.WHITE,
                            mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8),
                            mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8),
                            ChatFormatting.GRAY);
                }
                SetWidth(RenderUtil.getStringWidth(l_Coords));
                SetHeight(RenderUtil.getStringHeight(l_Coords));

                Rainbow.OnRender();
                RenderUtil.drawStringWithShadow(l_Coords, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);

                break;
            case NextLine:
                String l_X = l_Hud.Rainbow.getValue() ? String.format("X: %s [%s]", format(getX()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8) : "") : String.format("%sX: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(getX()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getX() / 8) : format(getX() * 8) : "");
                String l_Y = l_Hud.Rainbow.getValue() ? String.format("Y: %s [%s]", format(mc.player.posY), NetherCoords.getValue() ? format(mc.player.posY) : "") : String.format("%sY: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(mc.player.posY), NetherCoords.getValue() ? format(mc.player.posY) : "");
                String l_Z = l_Hud.Rainbow.getValue() ? String.format("Z: %s [%s]", format(getZ()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8) : "") : String.format("%sZ: %s%s [%s]", ChatFormatting.GRAY, ChatFormatting.WHITE, format(getZ()), NetherCoords.getValue() ? mc.player.dimension != -1 ? format(getZ() / 8) : format(getZ() * 8) : "");
                Rainbow.OnRender();
                RenderUtil.drawStringWithShadow(l_X, GetX(), GetY(), -1);
                RenderUtil.drawStringWithShadow(l_Y, GetX(), GetY()+RenderUtil.getStringHeight(l_X), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
                RenderUtil.drawStringWithShadow(l_Z, GetX(), GetY()+RenderUtil.getStringHeight(l_X)+RenderUtil.getStringHeight(l_Y), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);

                SetWidth(RenderUtil.getStringWidth(l_X));
                SetHeight(RenderUtil.getStringHeight(l_X)*3);
                break;
            default:
                break;
        }

    }

}
