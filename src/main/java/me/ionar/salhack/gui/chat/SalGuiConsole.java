package me.ionar.salhack.gui.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.CommandManager;
import me.ionar.salhack.module.ui.ConsoleModule;
import me.ionar.salhack.util.render.RenderUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SalGuiConsole extends GuiChat
{
    public SalGuiConsole(ConsoleModule p_Console)
    {
        Console = p_Console;
    }
    
    private Command CurrentCommand = null;
    private ConsoleModule Console;
    
    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        
        this.inputField = new SalGuiTextField(2, this.fontRenderer, this.width-295, 5, 595, 12, true);
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText("");
        this.inputField.setCanLoseFocus(false);
        this.tabCompleter = new GuiChat.ChatTabCompleter(this.inputField);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.tabCompleter.resetRequested();

        if (keyCode == 15)
        {
            this.tabCompleter.complete();
        }
        else
        {
            this.tabCompleter.resetDidComplete();
        }

        if (keyCode == 1)
        {
            this.mc.displayGuiScreen((GuiScreen)null);
        }
        else if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 200)
            {
                this.getSentHistory(-1);
            }
            else if (keyCode == 208)
            {
                this.getSentHistory(1);
            }
            else if (keyCode == 201)
            {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            }
            else if (keyCode == 209)
            {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            }
            else
            {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        }
        else
        {
            String s = this.inputField.getText().trim();
            
            this.sendChatMessage(s);
        }
    }
    
    @Override
    public void sendChatMessage(String msg)
    {
        if (CurrentCommand != null)
            CurrentCommand.ProcessCommand(msg);
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        this.mc = mc;
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        this.width = width/2;
        this.height = height/2;
        if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList)))
        {
            this.buttonList.clear();
            this.initGui();
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawRect(width-300, 0, width + 300, 17, Integer.MIN_VALUE);
        this.inputField.drawTextBox();ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
        {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }
        
        ArrayList<String> l_Commands = new ArrayList<String>();
        
        String s = this.inputField.getText();
        
        if (s.isEmpty())
        {
            drawRect(width-300, 23, width + 300, 40, Integer.MIN_VALUE);
            RenderUtil.drawStringWithShadow(String.format("Type a %scommand%s to get help.", ChatFormatting.GREEN, ChatFormatting.RESET), width-295, 26, 0xFFFFFF);
            
            return;
        }
        
        String[] l_Split = s.split(" ");
        
        if (l_Split == null || l_Split.length == 0)
            return;

        List<Command> l_CommandsLike = CommandManager.Get().GetCommandsLike(l_Split[0]);
        
        if (l_CommandsLike == null || l_CommandsLike.isEmpty() || l_CommandsLike.size() == 0)
        {
            drawRect(width-300, 23, width + 300, 40, Integer.MIN_VALUE);
            RenderUtil.drawStringWithShadow("No commands found...", width-295, 26, 0xFF0000);
            return;
        }
        
        final float l_Divider = 17;
        
        CurrentCommand = l_CommandsLike.get(0);
        
        int l_RealItr = 0;
        
        for (int l_I = 0; l_I < l_CommandsLike.size(); ++l_I)
        {
            Command l_Command = l_CommandsLike.get(l_I);
            
            int l_Color = 0xFFFFFF;
            
            if (l_Command.GetName().equalsIgnoreCase(l_Split[0]))
            {
                for (String l_Addon : l_Command.GetChunks())
                {
                    String[] l_AddonSplit = l_Addon.split(" ");
                    
                    l_Color = 0xFFFFFF;
                    
                    String l_ToWrite = ChatFormatting.GREEN + l_Command.GetName() + ChatFormatting.RESET + " " + l_Addon;
                    
                    if (l_AddonSplit != null && l_AddonSplit.length > 0 && l_Split.length > 1)
                    {
                        if (l_AddonSplit[0].toLowerCase().startsWith(l_Split[1].toLowerCase()))
                        {
                            l_ToWrite = ChatFormatting.GREEN + l_Command.GetName() + " ";
                            
                            for (int l_Y = 0; l_Y < l_AddonSplit.length; ++l_Y)
                            {
                                if (l_Y == 0)
                                    l_ToWrite += ChatFormatting.GREEN + l_AddonSplit[l_Y] + ChatFormatting.RESET;
                                else
                                    l_ToWrite += " " + l_AddonSplit[l_Y];
                            }
                            
                            drawRect(width-300, 23+(int)(l_RealItr*l_Divider), width + 300, 40+(int)(l_RealItr*l_Divider), Integer.MIN_VALUE);
                            RenderUtil.drawStringWithShadow(l_ToWrite, width-295, 26+(l_RealItr++*l_Divider), l_Color);
                        }
                    }
                    else
                    {
                        drawRect(width-300, 23+(int)(l_RealItr*l_Divider), width + 300, 40+(int)(l_RealItr*l_Divider), Integer.MIN_VALUE);
                        RenderUtil.drawStringWithShadow(l_ToWrite, width-295, 26+(l_RealItr++*l_Divider), l_Color);
                    }
                }

                if (l_Split.length == 1)
                {
                    l_Color = 0xFFEC00;
                    drawRect(width-300, 23+(int)(l_RealItr*l_Divider), width + 300, 40+(int)(l_RealItr*l_Divider), Integer.MIN_VALUE);
                    RenderUtil.drawStringWithShadow(ChatFormatting.GREEN + l_Command.GetName() + ChatFormatting.RESET + " " + l_Command.GetDescription(), width-295, 26+(l_RealItr++*l_Divider), l_Color);
                }
            }
            else
            {
                drawRect(width-300, 23+(int)(l_RealItr*l_Divider), width + 300, 40+(int)(l_RealItr*l_Divider), Integer.MIN_VALUE);
                RenderUtil.drawStringWithShadow(l_Command.GetName(), width-295, 26+(l_RealItr++*l_Divider), l_Color);
            }
        }
        
        drawRect(2, this.height, this.width, this.height, Integer.MIN_VALUE);

        //super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (Console.isEnabled())
            Console.toggle();
    }
}
