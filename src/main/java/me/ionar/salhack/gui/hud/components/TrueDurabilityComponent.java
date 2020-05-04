package me.ionar.salhack.gui.hud.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntityChest;

public class TrueDurabilityComponent extends HudComponentItem
{
    public TrueDurabilityComponent()
    {
        super("TrueDurability", 2, 260);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        ItemStack l_Stack = mc.player.getHeldItemMainhand();

        if (!l_Stack.isEmpty() && (l_Stack.getItem() instanceof ItemTool || l_Stack.getItem() instanceof ItemArmor || l_Stack.getItem() instanceof ItemSword))
        {
            final String l_Durability = ChatFormatting.WHITE + "Durability: " + ChatFormatting.GREEN + (l_Stack.getMaxDamage()-l_Stack.getItemDamage());
            
            RenderUtil.drawStringWithShadow(l_Durability, GetX(), GetY(), -1);

            SetWidth(RenderUtil.getStringWidth(l_Durability));
            SetHeight(RenderUtil.getStringHeight(l_Durability));
        }
    }
}
