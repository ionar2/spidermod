package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/// @todo: Needs enum options

public class TimeComponent extends HudComponentItem
{
    public TimeComponent()
    {
        super("Time", 2, 110);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String time = new SimpleDateFormat("h:mm a").format(new Date());

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(time, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);

        SetWidth(RenderUtil.getStringWidth(time));
        SetHeight(RenderUtil.getStringHeight(time));
    }
}
