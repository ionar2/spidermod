package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

import java.util.Calendar;

public class WelcomeComponent extends HudComponentItem
{
    //I coded this but SalHack skid coded it better so props to https://github.com/pleasegivesource/SalHackSkid for this.
    public WelcomeComponent()
    {
        super("WelcomeComponent", 200, 2);
    }

    private HudModule l_Hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private int l_I = 0;

    String l_welcome = "";
    Calendar c = Calendar.getInstance();
    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        if (timeOfDay >= 6 && timeOfDay < 12)
        {
            l_welcome = l_Hud.Rainbow.getValue() ? "Good Morning, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Morning, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else if (timeOfDay >= 12 && timeOfDay < 17)
        {
            l_welcome = l_Hud.Rainbow.getValue() ? "Good Afternoon, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Afternoon, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else if (timeOfDay >= 17 && timeOfDay < 22)
        {
            l_welcome = l_Hud.Rainbow.getValue() ? "Good Evening, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Evening, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else if (timeOfDay >= 22 || timeOfDay < 6)
        {
            l_welcome = l_Hud.Rainbow.getValue() ? "Good Night, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Night, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        }
        else
        {
            l_welcome = l_Hud.Rainbow.getValue() ? "Hello, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Hello, " + ChatFormatting.WHITE + mc.getSession().getUsername() + ".. psst! something went wrong!";
        }

        SetWidth(RenderUtil.getStringWidth(l_welcome));
        SetHeight(RenderUtil.getStringHeight(l_welcome) + 1);

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(l_welcome, GetX(), GetY(), l_Hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber(l_I)) : 0x2ACCED);
    }
}
