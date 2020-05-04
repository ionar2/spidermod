package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;
import java.util.Comparator;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;

public class NearestEntityFrameComponent extends HudComponentItem
{
    public NearestEntityFrameComponent()
    {
        super("NearestEntityFrame", 400, 2);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFFFFF);
        
        RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x990C0C0C);
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFEC00);
        
        EntityLivingBase l_Entity = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase && entity != mc.player)
                .map(entity -> (EntityLivingBase) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);
        
        if (l_Entity == null)
            return;
        
        float l_HealthPct = ((l_Entity.getHealth()+l_Entity.getAbsorptionAmount())/l_Entity.getMaxHealth())*100.0f ;
        float l_HealthBarPct = Math.min(l_HealthPct, 100.0f);
        
        DecimalFormat l_Format = new DecimalFormat("#.#");
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        
        GuiInventory.drawEntityOnScreen((int) GetX()+10, (int)GetY()+30, 15, p_MouseX, p_MouseY, l_Entity);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        RenderUtil.drawStringWithShadow(l_Entity.getName(), GetX()+20, GetY()+1, 0xFFFFFF);
        RenderUtil.drawGradientRect(GetX()+20, GetY()+11, GetX()+20+l_HealthBarPct, GetY()+22, 0x999FF365, 0x9913FF00);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", l_Format.format(l_HealthPct) + "%", l_Format.format(l_Entity.getHealth()+l_Entity.getAbsorptionAmount()), l_Format.format(l_Entity.getMaxHealth())), GetX()+20, GetY()+11, 0xFFFFFF);
        
        /*final Iterator<ItemStack> l_Items = mc.player.getArmorInventoryList().iterator();
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
        
        for (int l_I = 0; l_I < l_Stacks.size(); ++l_I)
        {
            ItemStack l_Stack = l_Stacks.get(l_I);
            
            int l_X = (int) (GetX() + 1);
            int l_Y = (int) (GetY() + 40) + (l_I * 15);
            mc.getRenderItem().renderItemAndEffectIntoGUI(l_Stack, l_X, l_Y);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, l_Stack, l_X, l_Y);
        }*/
        
        this.SetWidth(120);
        this.SetHeight(33);
    }

}
