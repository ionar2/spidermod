package me.ionar.salhack.gui.hud.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.tileentity.TileEntityChest;

/// @todo: Needs enum options

public class ChestCountComponent extends HudComponentItem
{
    public ChestCountComponent()
    {
        super("ChestCount", 2, 245);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        long chest = mc.world.loadedTileEntityList.stream()
                .filter(e -> e instanceof TileEntityChest).count();

        final String l_Chests = ChatFormatting.GRAY + "Chests: " + ChatFormatting.WHITE + chest;
        
        RenderUtil.drawStringWithShadow(l_Chests, GetX(), GetY(), -1);

        SetWidth(RenderUtil.getStringWidth(l_Chests));
        SetHeight(RenderUtil.getStringHeight(l_Chests));
    }
}
