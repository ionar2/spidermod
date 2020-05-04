package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class YawComponent extends HudComponentItem
{
    public YawComponent()
    {
        super("Yaw", 2, 200);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        DecimalFormat l_Format = new DecimalFormat("#.##");
        float l_Yaw = MathHelper.wrapDegrees(mc.player.rotationYaw);
        
        String direction = ChatFormatting.GRAY + "Yaw: " + ChatFormatting.WHITE + l_Format.format(l_Yaw);
        
        if (!direction.contains("."))
            direction += ".00";
        else
        {
            String[] l_Split = direction.split("\\.");
            
            if (l_Split != null && l_Split[1] != null && l_Split[1].length() != 2)
                direction += 0;
        }
        
        SetWidth(RenderUtil.getStringWidth(direction));
        SetHeight(RenderUtil.getStringHeight(direction));

        RenderUtil.drawStringWithShadow(direction, GetX(), GetY(), -1);
    }

}
