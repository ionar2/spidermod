package me.ionar.salhack.gui.hud.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.misc.StopWatchModule;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntityChest;

public class StopwatchComponent extends HudComponentItem
{
    private StopWatchModule Stopwatch = null;
    
    public StopwatchComponent()
    {
        super("Stopwatch", 2, 275);
        
        Stopwatch = (StopWatchModule) ModuleManager.Get().GetMod(StopWatchModule.class);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String l_Seconds = ChatFormatting.GRAY + "Seconds " + ChatFormatting.WHITE + TimeUnit.MILLISECONDS.toSeconds(Stopwatch.ElapsedMS - Stopwatch.StartMS);
        
        RenderUtil.drawStringWithShadow(l_Seconds, GetX(), GetY(), -1);

        SetWidth(RenderUtil.getStringWidth(l_Seconds));
        SetHeight(RenderUtil.getStringHeight(l_Seconds));
    }
}
