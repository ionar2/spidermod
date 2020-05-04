package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TotemCountComponent extends HudComponentItem
{
    public TotemCountComponent()
    {
        super("TotemCount", 2, 215);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);
        
        int l_TotemCount = 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            
            if (s.isEmpty())
                continue;
            
            if (s.getItem() == Items.TOTEM_OF_UNDYING)
            {
                ++l_TotemCount;
            }
        }

        final String totemCount = ChatFormatting.GRAY + "Totems: " + ChatFormatting.WHITE + l_TotemCount;

        SetWidth(RenderUtil.getStringWidth(totemCount));
        SetHeight(RenderUtil.getStringHeight(totemCount));
        RenderUtil.drawStringWithShadow(totemCount, GetX(), GetY(), -1);
    }

}
