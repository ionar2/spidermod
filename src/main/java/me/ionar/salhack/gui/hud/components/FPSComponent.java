package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

public class FPSComponent extends HudComponentItem
{
    public FPSComponent()
    {
        super("FPS", 2, 140);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String l_FPS = String.format(ChatFormatting.GRAY + "FPS %s%s", ChatFormatting.WHITE, Minecraft.getDebugFPS());

        RenderUtil.drawStringWithShadow(l_FPS, GetX(), GetY(), -1);
        
        SetWidth(RenderUtil.getStringWidth(l_FPS));
        SetHeight(RenderUtil.getStringHeight(l_FPS) + 1);
    }

}
