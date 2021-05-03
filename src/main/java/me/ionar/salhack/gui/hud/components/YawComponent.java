package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

public class YawComponent extends HudComponentItem
{
    public YawComponent()
    {
        super("Yaw", 2, 200);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        DecimalFormat l_Format = new DecimalFormat("#.##");
        float l_Yaw = MathHelper.wrapDegrees(mc.player.rotationYaw);
        
        String direction = l_Hud.Rainbow.getValue() ? "Yaw: " + l_Format.format(l_Yaw) : ChatFormatting.GRAY + "Yaw: " + ChatFormatting.WHITE + l_Format.format(l_Yaw);
        
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

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(direction, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : -1);
    }

}
