package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

public class PlayerCountComponent extends HudComponentItem
{
    public PlayerCountComponent()
    {
        super("PlayerCount", 2, 185);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String playerCount = l_Hud.Rainbow.getValue() ? "Players: " + mc.player.connection.getPlayerInfoMap().size() : ChatFormatting.GRAY + "Players: " + ChatFormatting.WHITE + mc.player.connection.getPlayerInfoMap().size();

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(playerCount, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
        
        SetWidth(RenderUtil.getStringWidth(playerCount));
        SetHeight(RenderUtil.getStringHeight(playerCount) + 1);
    }

}
