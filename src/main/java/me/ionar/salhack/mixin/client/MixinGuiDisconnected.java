package me.ionar.salhack.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.ionar.salhack.gui.minecraft.SalGuiReconnectButton;
import me.ionar.salhack.main.AlwaysEnabledModule;
import me.ionar.salhack.main.Wrapper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.resources.I18n;

@Mixin(value = GuiDisconnected.class, priority = Integer.MAX_VALUE)
public class MixinGuiDisconnected extends MixinGuiScreen
{
    @Shadow
    public int textHeight;
    
    private SalGuiReconnectButton ReconnectingButton;
    
    @Inject(method = "initGui", at = @At("RETURN"))
    public void initGui(CallbackInfo info)
    {
        /// Clear old ones :)
        buttonList.clear();

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT, this.height - 30), I18n.format("gui.toMenu")));
        this.buttonList.add(new GuiButton(421, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT+20, this.height - 10), "Reconnect"));
        this.buttonList.add(ReconnectingButton = new SalGuiReconnectButton(420, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT+40, this.height+10), "AutoReconnect"));
    }

    @Inject(method = "actionPerformed", at = @At("RETURN"))
    protected void actionPerformed(GuiButton button, CallbackInfo info)
    {
        if (button.id == 420)
        {
            ReconnectingButton.Clicked();
        }
        else if (button.id == 421)
            Wrapper.GetMC().displayGuiScreen(new GuiConnecting(null, Wrapper.GetMC(), AlwaysEnabledModule.LastIP, AlwaysEnabledModule.LastPort));
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo info)
    {
        if (buttonList.size() > 3)
        {
            this.buttonList.clear();
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT, this.height - 30), I18n.format("gui.toMenu")));
            this.buttonList.add(new GuiButton(421, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT+20, this.height - 10), "Reconnect"));
            this.buttonList.add(ReconnectingButton = new SalGuiReconnectButton(420, this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + this.fontRenderer.FONT_HEIGHT+40, this.height+10), "AutoReconnect"));
        }
    }
}
