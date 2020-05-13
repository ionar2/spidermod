package me.ionar.salhack.gui.chest;

import java.io.IOException;

import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.misc.ChestStealerModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;

public class SalGuiChest extends GuiChest
{
    public SalGuiChest(IInventory upperInv, IInventory lowerInv)
    {
        super(upperInv, lowerInv);
        
        this.mc = Minecraft.getMinecraft();
        ScaledResolution l_Res = new ScaledResolution(mc);
        
        this.setWorldAndResolution(this.mc, l_Res.getScaledWidth(), l_Res.getScaledHeight());
    }
    
    private ChestStealerModule ChestStealer;
    private boolean WasEnabledByGUI = false;
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        ChestStealer = (ChestStealerModule) ModuleManager.Get().GetMod(ChestStealerModule.class);
        
        this.buttonList.add(new GuiButton(1337, this.width / 2 + 100, this.height / 2 - this.ySize + 110, 50, 20, "Steal"));
        this.buttonList.add(new GuiButton(1338, this.width / 2 + 100, this.height / 2 - this.ySize + 130, 50, 20, "Store"));
        this.buttonList.add(new GuiButton(1339, this.width / 2 + 100, this.height / 2 - this.ySize + 150, 50, 20, "Drop"));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        switch (button.id)
        {
            case 1337:
                ChestStealer.Mode.setValue(ChestStealerModule.Modes.Steal);
                
                if (!ChestStealer.isEnabled())
                {
                    WasEnabledByGUI = true;
                    ChestStealer.toggle();
                }
                break;
            case 1338:
                ChestStealer.Mode.setValue(ChestStealerModule.Modes.Store);

                if (!ChestStealer.isEnabled())
                {
                    WasEnabledByGUI = true;
                    ChestStealer.toggle();
                }
                break;
            case 1339:
                ChestStealer.Mode.setValue(ChestStealerModule.Modes.Drop);

                if (!ChestStealer.isEnabled())
                {
                    WasEnabledByGUI = true;
                    ChestStealer.toggle();
                }
                break;
        }
    }

    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        if (WasEnabledByGUI)
            if (ChestStealer.isEnabled())
                ChestStealer.toggle();
    }
}
