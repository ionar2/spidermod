package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PingComponent extends HudComponentItem
{
    public PingComponent()
    {
        super("Ping", 2, 230);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        if (mc.world == null || mc.player == null || mc.player.getUniqueID() == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());

        if (playerInfo == null)
            return;

        final String ping = ChatFormatting.GRAY + "Ping " + ChatFormatting.WHITE + playerInfo.getResponseTime() + "ms";

        this.SetWidth(RenderUtil.getStringWidth(ping));
        this.SetHeight(RenderUtil.getStringHeight(ping)+1);

        RenderUtil.drawStringWithShadow(ping, this.GetX(), this.GetY(), -1);
    }

}
