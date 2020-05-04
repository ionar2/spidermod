package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.render.RenderUtil;

public class TPSComponent extends HudComponentItem
{
    public TPSComponent()
    {
        super("TPS", 2, 125);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String tickrate = String.format(ChatFormatting.GRAY + "TPS%s %.2f", ChatFormatting.WHITE, TickRateManager.Get().getTickRate());

        RenderUtil.drawStringWithShadow(tickrate, GetX(), GetY(), -1);
        
        SetWidth(RenderUtil.getStringWidth(tickrate));
        SetHeight(RenderUtil.getStringHeight(tickrate) + 1);
    }

}
