package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

//I got lazy and took this from https://github.com/pleasegivesource/SalHackSkid.
public class SpeedComponent extends HudComponentItem
{
    public final Value<UnitList> SpeedUnit = new Value<UnitList>("Speed Unit", new String[] {"SpeedUnit"}, "Unit of speed. Note that 1 metre = 1 block", UnitList.BPS);

    public enum UnitList
    {
        BPS,
        KMH,
    }

    final DecimalFormat FormatterBPS = new DecimalFormat("#.#");
    final DecimalFormat FormatterKMH = new DecimalFormat("#.#");

    public SpeedComponent()
    {
        super("Speed", 2, 80);
    }

    private double PrevPosX;
    private double PrevPosZ;
    private Timer timer = new Timer();
    private  String speed = "";

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        if (timer.passed(1000))
        {
            PrevPosX = mc.player.prevPosX;
            PrevPosZ = mc.player.prevPosZ;
        }

        final double deltaX = mc.player.posX - PrevPosX;
        final double deltaZ = mc.player.posZ - PrevPosZ;

        float l_Distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double l_BPS = l_Distance * 20;
        double l_KMH = Math.floor(( l_Distance/1000.0f ) / ( 0.05f/3600.0f ));

        if (SpeedUnit.getValue() == UnitList.BPS)
        {
            String l_FormatterBPS = FormatterBPS.format(l_BPS);

            //TODO Change BPS to m/s? 1 minecraft block is 1 real life metre iirc.
            speed = l_Hud.Rainbow.getValue() ? "Speed: " + l_FormatterBPS + " BPS" : ChatFormatting.GRAY + "Speed: " + ChatFormatting.WHITE + l_FormatterBPS + " BPS";

        }
        else if (SpeedUnit.getValue() == UnitList.KMH)
        {
            String l_FormatterKMH = FormatterKMH.format(l_KMH);

            speed = l_Hud.Rainbow.getValue() ? "Speed " + l_FormatterKMH + "km/h" : ChatFormatting.GRAY + "Speed " + ChatFormatting.WHITE + l_FormatterKMH + "km/h";

        }

        SetWidth(RenderUtil.getStringWidth(speed));
        SetHeight(RenderUtil.getStringHeight(speed)+1);

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(speed, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
    }
}
