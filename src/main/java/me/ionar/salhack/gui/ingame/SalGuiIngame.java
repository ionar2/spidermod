package me.ionar.salhack.gui.ingame;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.ReliantChatModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class SalGuiIngame extends GuiIngameForge
{
    public SalGuiIngame(Minecraft mc)
    {
        super(mc);

        ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, this, new SalGuiPlayerTabOverlay(mc, this), "field_175196_v");
        
        ReliantChatModule l_Mod = (ReliantChatModule)ModuleManager.Get().GetMod(ReliantChatModule.class);
        
        if (l_Mod.isEnabled())
            l_Mod.Activate();
    }

    @Override
    public void renderGameOverlay(float partialTicks)
    {
        super.renderGameOverlay(partialTicks);
        
        SalHackMod.EVENT_BUS.post(new EventRenderGameOverlay(partialTicks));
    }
}
