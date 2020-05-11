package me.ionar.salhack.module.render;

import me.ionar.salhack.events.blocks.EventCanCollideCheck;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.render.EventRenderEntityName;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.events.render.EventRenderSetupFog;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.friend.Friend;
import me.ionar.salhack.managers.FontManager;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.ItemUtil;
import me.ionar.salhack.util.render.GLUProjection;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;

import static org.lwjgl.opengl.GL11.*;

public class NametagsModule extends Module
{
    public final Value<Boolean> Armor = new Value<Boolean>("Armor", new String[]{ "" }, "", true);
    public final Value<Boolean> Durability = new Value<Boolean>("Durability", new String[]{ "" }, "", true);
    public final Value<Boolean> ItemName = new Value<Boolean>("ItemName", new String[]{ "" }, "", true);
    public final Value<Boolean> Health = new Value<Boolean>("Health", new String[]{ "" }, "", true);
    public final Value<Boolean> Invisibles = new Value<Boolean>("Invisibles", new String[]{ "" }, "", false);
    public final Value<Boolean> EntityID = new Value<Boolean>("EntityID", new String[]{ "" }, "", false);
    public final Value<Boolean> GameMode = new Value<Boolean>("GameMode", new String[]{ "" }, "", false);
    public final Value<Boolean> Ping = new Value<Boolean>("Ping", new String[]{ "" }, "", true);
    
    private ICamera camera = new Frustum();
    private final ResourceLocation inventory = new ResourceLocation("textures/gui/container/inventory.png");

    public NametagsModule()
    {
        super("NameTags", new String[]
        { "Nametag" }, "Highlights entities", "NONE", -1, ModuleType.RENDER);
    }

    @EventHandler
    private Listener<EventRenderGameOverlay> OnRenderGameOverlay = new Listener<>(p_Event ->
    {
        mc.world.loadedEntityList.stream().filter(EntityUtil::isLiving).filter(entity -> !EntityUtil.isFakeLocalPlayer(entity))
                .filter(entity -> (entity instanceof EntityPlayer && mc.player != entity)).forEach(e ->
                {
                    RenderNameTagFor((EntityPlayer)e, p_Event);
                });
    });

    
    private void RenderNameTagFor(EntityPlayer e, EventRenderGameOverlay p_Event)
    {
        GlStateManager.pushMatrix();
        final float[] bounds = this.convertBounds(e, p_Event.getPartialTicks(),
                p_Event.getScaledResolution().getScaledWidth(),
                p_Event.getScaledResolution().getScaledHeight());

        if (bounds != null)
        {
            String name = StringUtils.stripControlCodes(e.getName());

            int color = -1;

            final Friend friend = FriendManager.Get().GetFriend(e);

            if (friend != null)
            {
                name = friend.GetAlias();
                color = 0xFF9900EE;
            }

            final EntityPlayer player = (EntityPlayer) e;
            int responseTime = -1;
            
            if (Ping.getValue())
            {
                try
                {
                    responseTime = (int) MathUtil.clamp(
                            mc.getConnection().getPlayerInfo(player.getUniqueID()).getResponseTime(), 0,
                            300);
                }
                catch (NullPointerException np)
                {}
            }
            
            String l_Name = String.format("%s %sms %s", name, responseTime, Math.floor(e.getHealth()+e.getAbsorptionAmount()));

            RenderUtil.drawStringWithShadow(l_Name,
                    bounds[0] + (bounds[2] - bounds[0]) / 2 - RenderUtil.getStringWidth(l_Name) / 2,
                    bounds[1] + (bounds[3] - bounds[1]) - 8 - 1, color);
                
           // if (this.armor.getValue())
            {
                final Iterator<ItemStack> items = e.getArmorInventoryList().iterator();
                final ArrayList<ItemStack> stacks = new ArrayList<>();
                
                
                stacks.add(e.getHeldItemOffhand());

                while (items.hasNext())
                {
                    final ItemStack stack = items.next();
                    if (stack != null && stack.getItem() != Items.AIR)
                    {
                        stacks.add(stack);
                    }
                }
                stacks.add(e.getHeldItemMainhand());

                Collections.reverse(stacks);

                int x = 0;
                
                if (!e.getHeldItemMainhand().isEmpty())
                {
                    l_Name = e.getHeldItemMainhand().getDisplayName();
                    
                    FontManager.Get().FontRenderers[15].drawStringWithShadow(l_Name, 
                            bounds[0] + (bounds[2] - bounds[0]) / 2 - FontManager.Get().FontRenderers[15].getStringWidth(l_Name) / 2,
                            bounds[1] + (bounds[3] - bounds[1])
                                    - mc.fontRenderer.FONT_HEIGHT - 35, -1);
                }

                for (ItemStack stack : stacks)
                {
                    if (stack != null)
                    {
                        final Item item = stack.getItem();
                        if (item != Items.AIR)
                        {
                            GlStateManager.pushMatrix();
                            GlStateManager.enableBlend();
                            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                            RenderHelper.enableGUIStandardItemLighting();
                            GlStateManager.translate(
                                    bounds[0] + (bounds[2] - bounds[0]) / 2 + x - (16 * stacks.size() / 2),
                                    bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 19,
                                    0);
                            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
                            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
                            RenderHelper.disableStandardItemLighting();
                            GlStateManager.disableBlend();
                            GlStateManager.color(1, 1, 1, 1);
                            GlStateManager.popMatrix();
                            x += 16;

                            //if (this.enchants.getValue())
                            {
                                final List<String> stringsToDraw = Lists.newArrayList();

                                if (stack.isItemDamaged())
                                {
                                    float l_ArmorPct = ((float)(stack.getMaxDamage()-stack.getItemDamage()) /  (float)stack.getMaxDamage())*100.0f;
                                    float l_ArmorBarPct = Math.min(l_ArmorPct, 100.0f);
                                
                                    stringsToDraw.add(String.format("%s", (int)l_ArmorBarPct + "%"));
                                }
                                int y = 0;

                                if (stack.getEnchantmentTagList() != null)
                                {
                                    final NBTTagList tags = stack.getEnchantmentTagList();
                                    for (int i = 0; i < tags.tagCount(); i++)
                                    {
                                        final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                                        if (tagCompound != null && Enchantment
                                                .getEnchantmentByID(tagCompound.getByte("id")) != null)
                                        {
                                            final Enchantment enchantment = Enchantment
                                                    .getEnchantmentByID(tagCompound.getShort("id"));
                                            final short lvl = tagCompound.getShort("lvl");
                                            if (enchantment != null)
                                            {
                                                String ench = "";
                                                if (enchantment.isCurse())
                                                {
                                                    ench = ChatFormatting.RED
                                                            + enchantment.getTranslatedName(lvl)
                                                                    .substring(11).substring(0, 2)
                                                            + ChatFormatting.GRAY + lvl;
                                                }
                                                else
                                                {
                                                    ench = enchantment.getTranslatedName(lvl).substring(0,
                                                            2) + lvl;
                                                }
                                                stringsToDraw.add(ench);
                                            }
                                        }
                                    }
                                }

                                // Enchanted gapple
                                if (item == Items.GOLDEN_APPLE)
                                {
                                    if (stack.getItemDamage() == 1)
                                    {
                                        stringsToDraw.add(ChatFormatting.DARK_RED + "God");
                                    }
                                }

                                for (String string : stringsToDraw)
                                {
                                    GlStateManager.pushMatrix();
                                    GlStateManager.disableDepth();
                                    GlStateManager
                                            .translate(
                                                    bounds[0] + (bounds[2] - bounds[0]) / 2 + x
                                                            - ((16.0f * stacks.size()) / 2.0f)
                                                            - (16.0f / 2.0f)
                                                            - (RenderUtil.getStringWidth(string)
                                                                    / 4.0f),
                                                    bounds[1] + (bounds[3] - bounds[1])
                                                            - mc.fontRenderer.FONT_HEIGHT - 23 - y,
                                                    0);
                                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                                    RenderUtil.drawStringWithShadow(string, 0, 0, -1);
                                    GlStateManager.scale(2, 2, 2);
                                    GlStateManager.enableDepth();
                                    GlStateManager.popMatrix();
                                    y += 4;
                                }
                            }
                        }
                    }
                }
            }
        }
        GlStateManager.popMatrix();
    }
    
    @EventHandler
    private Listener<EventRenderEntityName> OnRenderEntityName = new Listener<>(p_Event ->
    {
        p_Event.cancel();
    });

    private float[] convertBounds(Entity e, float partialTicks, int width, int height) {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        final Vec3d pos = MathUtil.interpolateEntity(e, partialTicks);

        if (pos == null) {
            return null;
        }

        AxisAlignedBB bb = e.getEntityBoundingBox();

        if (e instanceof EntityEnderCrystal) {
            bb = new AxisAlignedBB(bb.minX + 0.3f, bb.minY + 0.2f, bb.minZ + 0.3f, bb.maxX - 0.3f, bb.maxY, bb.maxZ - 0.3f);
        }

        if (e instanceof EntityItem) {
            bb = new AxisAlignedBB(bb.minX, bb.minY + 0.7f, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        }

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        camera.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(bb)) {
            return null;
        }

        final Vec3d corners[] = {
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),

                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2)
        };

        for (Vec3d vec : corners) {
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(pos.x + vec.x - Minecraft.getMinecraft().getRenderManager().viewerPosX, pos.y + vec.y - Minecraft.getMinecraft().getRenderManager().viewerPosY, pos.z + vec.z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

            if (projection == null) {
                return null;
            }

            x = Math.max(x, (float) projection.getX());
            y = Math.max(y, (float) projection.getY());

            w = Math.min(w, (float) projection.getX());
            h = Math.min(h, (float) projection.getY());
        }

        if (x != -1 && y != -1 && w != width + 1 && h != height + 1) {
            return new float[]{x, y, w, h};
        }

        return null;
    }
}
