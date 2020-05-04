package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

public class PlayerCountComponent extends HudComponentItem
{
    public PlayerCountComponent()
    {
        super("PlayerCount", 2, 185);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String playerCount = ChatFormatting.GRAY + "Players: " + ChatFormatting.WHITE + mc.player.connection.getPlayerInfoMap().size();

        RenderUtil.drawStringWithShadow(playerCount, GetX(), GetY(), -1);
        
        SetWidth(RenderUtil.getStringWidth(playerCount));
        SetHeight(RenderUtil.getStringHeight(playerCount) + 1);
    }

}
