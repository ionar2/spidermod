package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.combat.AutoCrystalModule;
import me.ionar.salhack.module.combat.AutoTotemModule;
import me.ionar.salhack.module.combat.KillAuraModule;
import me.ionar.salhack.util.render.RenderUtil;

public class PvPInfoComponent extends HudComponentItem
{
    public PvPInfoComponent()
    {
        super("PvPInfo", 2, 290);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String l_KillAura = ChatFormatting.GRAY + "KA " + ChatFormatting.WHITE + (ModuleManager.Get().GetMod(KillAuraModule.class).isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String l_AutoCrystal = ChatFormatting.GRAY + "CA " + ChatFormatting.WHITE + (ModuleManager.Get().GetMod(AutoCrystalModule.class).isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String l_AutoTotem = ChatFormatting.GRAY + "AT " + ChatFormatting.WHITE + (ModuleManager.Get().GetMod(AutoTotemModule.class).isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        
        RenderUtil.drawStringWithShadow(l_KillAura, GetX(), GetY(), -1);
        RenderUtil.drawStringWithShadow(l_AutoCrystal, GetX(), GetY()+12, -1);
        RenderUtil.drawStringWithShadow(l_AutoTotem, GetX(), GetY()+24, -1);

        SetWidth(RenderUtil.getStringWidth(l_AutoCrystal));
        SetHeight(RenderUtil.getStringHeight(l_AutoCrystal)+RenderUtil.getStringHeight(l_KillAura)+RenderUtil.getStringHeight(l_AutoTotem));
    }
}
