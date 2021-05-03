package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

public class FPSComponent extends HudComponentItem
{
    public FPSComponent()
    {
        super("FPS", 2, 140);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        final String l_FPS = l_Hud.Rainbow.getValue() ? String.format("FPS %s", Minecraft.getDebugFPS()) : String.format(ChatFormatting.GRAY + "FPS %s%s", ChatFormatting.WHITE, Minecraft.getDebugFPS());

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(l_FPS, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
        
        SetWidth(RenderUtil.getStringWidth(l_FPS));
        SetHeight(RenderUtil.getStringHeight(l_FPS) + 1);
    }

}
