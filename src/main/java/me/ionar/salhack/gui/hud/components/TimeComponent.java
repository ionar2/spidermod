package me.ionar.salhack.gui.hud.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;

/// @todo: Needs enum options

public class TimeComponent extends HudComponentItem
{
    public TimeComponent()
    {
        super("Time", 2, 110);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String time = new SimpleDateFormat("h:mm a").format(new Date());

        RenderUtil.drawStringWithShadow(time, GetX(), GetY(), -1);

        SetWidth(RenderUtil.getStringWidth(time));
        SetHeight(RenderUtil.getStringHeight(time));
    }
}
