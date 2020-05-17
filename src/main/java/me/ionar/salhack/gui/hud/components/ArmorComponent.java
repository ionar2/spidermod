package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FontManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ArmorComponent extends HudComponentItem
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"Mode"}, "Modes", Modes.Bars);
    
    public enum Modes
    {
        Bars,
        SimplePct,
    }
    
    public ArmorComponent()
    {
        super("Armor", 200, 200);
    }
    
    DecimalFormat Formatter = new DecimalFormat("#");

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        
        super.render(mouseX, mouseY, partialTicks);
        
        final Iterator<ItemStack> l_Items = mc.player.getArmorInventoryList().iterator();
        final ArrayList<ItemStack> l_Stacks = new ArrayList<>();

        while (l_Items.hasNext())
        {
            final ItemStack l_Stack = l_Items.next();
            if (l_Stack != ItemStack.EMPTY && l_Stack.getItem() != Items.AIR)
            {
                l_Stacks.add(l_Stack);
            }
        }

        Collections.reverse(l_Stacks);
        
        switch (Mode.getValue())
        {
            case Bars:

                RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x990C0C0C);
                
                int l_Y = 0;
                
                for (int l_I = 0; l_I < l_Stacks.size(); ++l_I)
                {
                    ItemStack l_Stack = l_Stacks.get(l_I);
                    
                    float l_X = (GetX() + 1);

                    float l_ArmorPct = ((float)(l_Stack.getMaxDamage()-l_Stack.getItemDamage()) /  (float)l_Stack.getMaxDamage())*100.0f;
                    float l_ArmorBarPct = Math.min(l_ArmorPct, 100.0f);

                    int l_ColorMin = 0x999FF365;
                    int l_ColorMax = 0x9913FF00;
                    
                    if (l_ArmorBarPct < 80f && l_ArmorPct > 30f)
                    {
                        l_ColorMin = 0x99FFB600;
                        l_ColorMax = 0x99FFF700;
                    }
                    else if (l_ArmorBarPct < 30f) 
                    {
                        l_ColorMin = 0x99FF0000;
                        l_ColorMax = 0x99DA1A1A;
                    }
                    
                    RenderUtil.drawGradientRect(l_X, GetY()+l_Y, l_X+(GetWidth()*(l_ArmorBarPct/100.0f)), GetY()+l_Y+15, l_ColorMin, l_ColorMax);

                    mc.getRenderItem().renderItemAndEffectIntoGUI(l_Stack, (int)l_X, (int)GetY() + l_Y);
                //    mc.getRenderItem().renderItemOverlays(mc.fontRenderer, l_Stack, l_X, (int)GetY() + l_Y); - enable if you want normal bar to show, but it looks worse
                    
                    String l_Durability = String.format("%s %s / %s", Formatter.format(l_ArmorBarPct) + "%", l_Stack.getMaxDamage()-l_Stack.getItemDamage(), l_Stack.getMaxDamage());
                    
                    l_X = GetX()+18;
                    
                    RenderUtil.drawStringWithShadow(l_Durability, l_X, GetY()+l_Y+2, 0xFFFFFF);
                    
                    l_Y += 15;
                }
                
                this.SetWidth(100);
                this.SetHeight(l_Y);
                break;
            case SimplePct:
                
                float l_X = 0;
                float l_TextX = 4;
                for (int l_I = 0; l_I < l_Stacks.size(); ++l_I)
                {
                    ItemStack l_Stack = l_Stacks.get(l_I);
                    
                    mc.getRenderItem().renderItemAndEffectIntoGUI(l_Stack, (int)(GetX() + l_X), (int)GetY()+10);
                    mc.getRenderItem().renderItemOverlays(mc.fontRenderer, l_Stack, (int)(GetX() + l_X), (int)GetY()+10);
                    
                    FontManager.Get().TWCenMt18. drawCenteredString(Formatter.format(GetPctFromStack(l_Stack)), GetX() + l_TextX, GetY(), -1);
                    
                    l_X += 20;
                    
                    if (l_I < l_Stacks.size()-1)
                    {
                        float l_Pct = GetPctFromStack(l_Stacks.get(l_I+1));
                        
                        if (l_Pct == 100.0f)
                            l_TextX += 22;
                        else if (l_Pct >= 10.0)
                            l_TextX += 21f;
                        else
                            l_TextX += 25f;
                    }
                }
                
                SetWidth(75);
                SetHeight(24);
                break;
            default:
                break;
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
    
    private float GetPctFromStack(ItemStack p_Stack)
    {
        float l_ArmorPct = ((float)(p_Stack.getMaxDamage()-p_Stack.getItemDamage()) /  (float)p_Stack.getMaxDamage())*100.0f;
        float l_ArmorBarPct = Math.min(l_ArmorPct, 100.0f);

        return l_ArmorBarPct;
    }
}
