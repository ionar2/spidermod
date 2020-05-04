package me.ionar.salhack.module.render;

import me.ionar.salhack.events.blocks.EventCanCollideCheck;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.render.EventRenderEntityName;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.mojang.realmsclient.gui.ChatFormatting;

import static org.lwjgl.opengl.GL11.*;

public class NametagsModule extends Module
{
    private Value<Boolean> players = new Value<Boolean>("Players", new String[] {"P"}, "", true);
    private Value<Boolean> animals = new Value<Boolean>("Animals", new String[] {"A"}, "", false);
    private Value<Boolean> mobs = new Value<Boolean>("Mobs", new String[] {"M"}, "", false);
    private Value<Double> range = new Value<Double>("Range", new String[] {"R"}, "", 200.0, 0.0, 1000.0, 100.0);
    private Value<Float> scale = new Value<Float>("Scale", new String[] {"S"}, "", 1f, 0.5f, 10.0f, 1.0f);
    private Value<Boolean> health = new Value<Boolean>("Health", new String[] {"H"}, "Displays Health", true);
    
    public NametagsModule()
    {
        super("Nametags", new String[]
        { "Nametags" }, "Displays nametags over entities", "NONE", 0x2CB1AA, ModuleType.RENDER);
    }

    RenderItem itemRenderer = mc.getRenderItem();
    
    @EventHandler
    private Listener<EventRenderEntityName> OnRenderEntityName = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager().options == null)
            return;

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        mc.world.loadedEntityList.stream().filter(EntityUtil::isLiving).filter(entity -> !EntityUtil.isFakeLocalPlayer(entity))
                .filter(entity -> (entity instanceof EntityPlayer ? players.getValue() && mc.player != entity : (EntityUtil.isPassive(entity) ? animals.getValue() : mobs.getValue())))
                .filter(entity -> mc.player.getDistance(entity) < range.getValue()).sorted(Comparator.comparing(entity -> -mc.player.getDistance(entity))).forEach(this::drawNametag);
        GlStateManager.disableTexture2D();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    });

    private void drawNametag(Entity entityIn)
    {
        GlStateManager.pushMatrix();

        Vec3d interp = 
                 entityIn.getPositionVector().subtract(Minecraft.getMinecraft().getRenderManager().renderPosX,
                        Minecraft.getMinecraft().getRenderManager().renderPosY,
                        Minecraft.getMinecraft().getRenderManager().renderPosZ);
        float yAdd = entityIn.height + 0.5F - (entityIn.isSneaking() ? 0.25F : 0.0F);
        double x = interp.x;
        double y = interp.y + yAdd;
        double z = interp.z;

        float viewerYaw = mc.getRenderManager().playerViewY;
        float viewerPitch = mc.getRenderManager().playerViewX;
        boolean isThirdPersonFrontal = mc.getRenderManager().options.thirdPersonView == 2;
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);

        float f = mc.player.getDistance(entityIn);
        float m = (f / 8f) ;
        
        if (f < 11.0f)
            m = f/4;
        
        GlStateManager.scale(m, m, m);

        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        
        String str = entityIn.getName();
        
        if (entityIn instanceof EntityLivingBase)
        {
            EntityLivingBase l_Base = (EntityLivingBase)entityIn;
            
            str = String.format("%s %s%s", entityIn.getName(), ChatFormatting.GREEN, l_Base.getHealth() + l_Base.getAbsorptionAmount());
            
            if (l_Base instanceof EntityPlayer)
            {
                EntityPlayer l_Player = (EntityPlayer)l_Base;
                
                int responseTime = -1;
                try
                {
                    responseTime = (int) MathUtil.clamp(
                            mc.getConnection().getPlayerInfo(l_Player.getUniqueID()).getResponseTime(), 0,
                            300);
                }
                catch (NullPointerException np)
                {}
                
                str =  String.format("%s %s %s", entityIn.getName(), responseTime + "ms", ChatFormatting.GREEN + "" + l_Base.getHealth() + l_Base.getAbsorptionAmount());
            }
        }
        
        float i = RenderUtil.getStringWidth(str) / 2;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();

        BufferBuilder bufferbuilder = tessellator.getBuffer();

        glTranslatef(0, -20, 0);
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos(-i - 1, 19, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos(i + 1, 19, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        tessellator.draw();

        bufferbuilder.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-i - 1, 8, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        bufferbuilder.pos(-i - 1, 19, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        bufferbuilder.pos(i + 1, 19, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        bufferbuilder.pos(i + 1, 8, 0.0D).color(.1f, .1f, .1f, .1f).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();

        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        RenderUtil.drawStringWithShadow(str, -i, 10, entityIn instanceof EntityPlayer ? FriendManager.Get().IsFriend(entityIn) ? 0x11EBEE : 0xFF2A11 : -1);
        GlStateManager.glNormal3f(0.0F, 0.0F, 0.0F);
        glTranslatef(0, 20, 0);

        GlStateManager.scale(-40, -40, 40);
        
        ArrayList<ItemStack> equipment = new ArrayList<>();

        if (entityIn instanceof EntityPlayer)
        {
            EntityPlayer l_Player = (EntityPlayer)entityIn;
            
            ArrayList<ItemStack> armour = new ArrayList<>();
            entityIn.getArmorInventoryList().forEach(itemStack ->
            {
                if (itemStack != null)
                    armour.add(itemStack);
            });
            Collections.reverse(armour);
            equipment.add(l_Player.getHeldItemMainhand());
            equipment.addAll(armour);
            equipment.add(l_Player.getHeldItemOffhand());
            if (equipment.size() == 0)
            {
                GlStateManager.popMatrix();
                return;
            }
        }
        else
        {
            entityIn.getHeldEquipment().forEach(itemStack ->
            {
                if (itemStack != null)
                    equipment.add(itemStack);
            });
            ArrayList<ItemStack> armour = new ArrayList<>();
            entityIn.getArmorInventoryList().forEach(itemStack ->
            {
                if (itemStack != null)
                    armour.add(itemStack);
            });
            Collections.reverse(armour);
            equipment.addAll(armour);
            if (equipment.size() == 0)
            {
                GlStateManager.popMatrix();
                return;
            }
        }

        Collection<ItemStack> a = equipment.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        GlStateManager.translate(((a.size() - 1) / 2f) * .5f, .6, 0);

        a.forEach(itemStack ->
        {
            GlStateManager.pushAttrib();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.scale(.5, .5, 0);
            GlStateManager.disableLighting();
            this.itemRenderer.zLevel = -5;
            this.itemRenderer.renderItem(itemStack, itemStack.getItem() == Items.SHIELD ? ItemCameraTransforms.TransformType.FIXED : ItemCameraTransforms.TransformType.NONE);
            this.itemRenderer.zLevel = 0;
            GlStateManager.scale(2, 2, 0);
            GlStateManager.popAttrib();
            GlStateManager.translate(-.5f, 0, 0);
        });

        GlStateManager.popMatrix();
    }
}
