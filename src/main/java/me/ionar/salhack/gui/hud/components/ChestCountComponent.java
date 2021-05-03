package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.tileentity.TileEntityChest;

/// @todo: Needs enum options

public class ChestCountComponent extends HudComponentItem
{
    public ChestCountComponent()
    {
        super("ChestCount", 2, 245);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        long chest = mc.world.loadedTileEntityList.stream()
                .filter(e -> e instanceof TileEntityChest).count();

        final String l_Chests = l_Hud.Rainbow.getValue() ? "Chests: " + chest : ChatFormatting.GRAY + "Chests: " + ChatFormatting.WHITE + chest;

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(l_Chests, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);

        SetWidth(RenderUtil.getStringWidth(l_Chests));
        SetHeight(RenderUtil.getStringHeight(l_Chests));
    }
}
