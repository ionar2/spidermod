package me.ionar.salhack.module.ui;

import java.lang.reflect.Field;

import me.ionar.salhack.gui.chat.SalGuiNewChat;
import me.ionar.salhack.gui.ingame.SalGuiIngame;
import me.ionar.salhack.gui.ingame.SalGuiPlayerTabOverlay;
import me.ionar.salhack.module.Module;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ReliantChatModule extends Module
{
    public ReliantChatModule()
    {
        super("ReliantChat", new String[]
        { "CustomChat" }, "TTF font rendering for chat", "NONE", -1, ModuleType.UI);
    }

    private SalGuiNewChat m_Chat = null;
    
    public void Activate()
    {
        if (mc.ingameGUI == null)
            return;
        
        if (m_Chat == null) m_Chat = new SalGuiNewChat(mc);

        ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, mc.ingameGUI, m_Chat, "field_73840_e");
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        Activate();
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();

        if (mc.ingameGUI == null)
            return;
        
        ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, mc.ingameGUI, new GuiNewChat(mc), "field_73840_e");
    }
}
