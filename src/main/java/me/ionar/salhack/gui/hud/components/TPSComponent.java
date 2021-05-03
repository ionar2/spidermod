package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

public class TPSComponent extends HudComponentItem
{
    public TPSComponent()
    {
        super("TPS", 2, 125);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String tickrate = l_Hud.Rainbow.getValue() ? String.format("TPS %.2f", TickRateManager.Get().getTickRate()) : String.format(ChatFormatting.GRAY + "TPS%s %.2f", ChatFormatting.WHITE, TickRateManager.Get().getTickRate());

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(tickrate, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
        
        SetWidth(RenderUtil.getStringWidth(tickrate));
        SetHeight(RenderUtil.getStringHeight(tickrate) + 1);
    }

}
