package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TotemCountComponent extends HudComponentItem
{
    public TotemCountComponent()
    {
        super("TotemCount", 2, 215);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

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

        final String totemCount = l_Hud.Rainbow.getValue() ? "Totems: " + l_TotemCount : ChatFormatting.GRAY + "Totems: " + ChatFormatting.WHITE + l_TotemCount;

        SetWidth(RenderUtil.getStringWidth(totemCount));
        SetHeight(RenderUtil.getStringHeight(totemCount));
        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(totemCount, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
    }

}
