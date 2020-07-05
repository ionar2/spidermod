package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;
import java.util.Comparator;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;

public class NearestEntityFrameComponent extends HudComponentItem
{
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] {"P"}, "Displays players", true);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[] {"F"}, "Displays Friends", false);
    public final Value<Boolean> Mobs = new Value<Boolean>("Mobs", new String[] {"M"}, "Displays Mobs", true);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] {"A"}, "Displays Animals", true);
    
    public NearestEntityFrameComponent()
    {
        super("NearestEntityFrame", 400, 2);
    }

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks)
    {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        EntityLivingBase l_Entity = mc.world.loadedEntityList.stream()
                .filter(entity -> IsValidEntity(entity))
                .map(entity -> (EntityLivingBase) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);
        
        if (l_Entity == null)
            return;
        
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFFFFF);
        
        RenderUtil.drawRect(GetX(), GetY(), GetX()+GetWidth(), GetY()+GetHeight(), 0x990C0C0C);
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFEC00);
        
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
        
        
        this.SetWidth(120);
        this.SetHeight(33);
    }

    private boolean IsValidEntity(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntityLivingBase))
            return false;
        
        if (p_Entity instanceof EntityPlayer)
        {
            if (p_Entity == mc.player)
                return false;
            
            if (!Players.getValue())
                return false;
            
            if (FriendManager.Get().IsFriend(p_Entity) && !Friends.getValue())
                return false;
        }
        
        if (!Mobs.getValue() && (EntityUtil.isHostileMob(p_Entity) || (p_Entity instanceof EntityPigZombie) || (p_Entity instanceof EntityEnderman)))
            return false;

        if (!Animals.getValue() && (p_Entity instanceof EntityAnimal || p_Entity instanceof EntityAmbientCreature || p_Entity instanceof EntitySquid))
            return false;
        
        return true;
    }
}
