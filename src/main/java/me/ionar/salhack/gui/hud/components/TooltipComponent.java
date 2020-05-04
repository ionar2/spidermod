package me.ionar.salhack.gui.hud.components;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public final class TooltipComponent extends HudComponentItem
{
    final Minecraft mc = Minecraft.getMinecraft();
    private EntityPlayer m_LastPlayer = null;
    private ArrayList<ItemStack> HotbarGuesser = new ArrayList<ItemStack>();

    private enum ThreatLevels
    {
        None,
        Matched,
        High,
    }

    public TooltipComponent()
    {
        super("Tooltip", 700, 600);
    }

    private void drawCharacter(float posX, float posY, int size, int cursorX, int cursorY, EntityPlayer p_Ent)
    {
        GameProfile profile = new GameProfile(p_Ent.getUniqueID(), "");

        /*
         * EntityOtherPlayerMP l_Ent = new EntityOtherPlayerMP(mc.world, profile); l_Ent.copyLocationAndAnglesFrom(p_Ent); l_Ent.rotationYaw = p_Ent.rotationYaw; l_Ent.rotationYawHead =
         * p_Ent.rotationYawHead; l_Ent.inventory.copyInventory(p_Ent.inventory);
         */

        final float mouseX = (float) posX - cursorX;
        final float mouseY = (float) posY - size * 1.67F - cursorY;

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GuiInventory.drawEntityOnScreen((int) posX, (int) posY, size, mouseX, mouseY, p_Ent);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player == null)
            return;

        EntityPlayer l_Entity = null;

        l_Entity = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityPlayer && entity != mc.player && entity.getName() != mc.player.getName() && !FriendManager.Get().IsFriend(entity))
                .map(entity -> (EntityPlayer) entity).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);

        if (l_Entity == null)
            return;

        if (m_LastPlayer == null || l_Entity.getName() != m_LastPlayer.getName())
        {
            m_LastPlayer = l_Entity;
            HotbarGuesser.clear();
        }

        SetWidth(260);
        SetHeight(120);

        InternalThreatLevel[0] = 2;
        InternalThreatLevel[1] = 2;
        InternalThreatLevel[2] = 2;
        InternalThreatLevel[3] = 2;

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        RenderUtil.drawRect(GetX(), this.GetY(), GetX() + this.GetWidth(), this.GetY() + this.GetHeight(), 0x75101010); // background
        drawCharacter(GetX() + 25, GetY() + 95, 40, (int) GetX(), (int) GetY(), l_Entity);

        int responseTime = -1;
        try
        {
            responseTime = (int) MathUtil.clamp(mc.getConnection().getPlayerInfo(l_Entity.getUniqueID()).getResponseTime(), 0, 300);
        }
        catch (NullPointerException np)
        {
        }

        RenderUtil.drawStringWithShadow(responseTime + "ms", GetX() + GetWidth() - (RenderUtil.getStringWidth(responseTime + "ms") + 5), GetY() + 5, 0xFFFFFF);

        RenderUtil.drawStringWithShadow(l_Entity.getName(), GetX() + 52, GetY() + 2, 0xFFFFFF);

        DecimalFormat l_2D = new DecimalFormat("#.##");

        double l_Distance = mc.player.getDistance(l_Entity);

        String l_Line1 = "Distance: " + l_2D.format(l_Distance);

        if (!l_Line1.contains("."))
            l_Line1 += ".00";
        else
        {
            String[] l_Split = l_Line1.split("\\.");

            if (l_Split != null && l_Split[1] != null && l_Split[1].length() != 2)
                l_Line1 += 0;
        }

        float l_TheirHealth = l_Entity.getHealth() + l_Entity.getAbsorptionAmount();

        l_Line1 += " | Health: " + l_2D.format(l_TheirHealth);

        RenderUtil.drawStringWithShadow(l_Line1, GetX() + 52, GetY() + 15, 0xFFFFFF);

        final Iterator<ItemStack> items = l_Entity.getArmorInventoryList().iterator();
        final ArrayList<ItemStack> stacks = new ArrayList<>();

        while (items.hasNext())
        {
            final ItemStack stack = items.next();
            if (stack != null && stack.getItem() != Items.AIR)
            {
                stacks.add(stack);
            }
        }

        Collections.reverse(stacks);

        int l_X = (int) (GetX() + 50);
        int l_Y = (int) (GetY() + 10);

        int l_TextY = (int) (GetY() + 35);

        int l_Itr = 0;

        ThreatLevels l_ThreatLevel = ThreatLevels.None;

        for (ItemStack stack : stacks)
        {
            if (stack == null)
                continue;

            final Item item = stack.getItem();
            if (item != Items.AIR)
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                // l_X += 20;
                l_Y += 22;

                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, l_X, l_Y);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, l_X, l_Y);

                // int l_LineY = (int) (GetY() + 55) + l_Itr*18;
                // RenderUtil.drawRect(l_X, l_LineY, l_X+121, l_LineY+1, 0x99F0FF00);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.popMatrix();

                final List<String> stringsToDraw = Lists.newArrayList();

                if (stack.getEnchantmentTagList() != null)
                {
                    final NBTTagList tags = stack.getEnchantmentTagList();
                    final ArrayList<NBTTagCompound> l_EnchantmentList = new ArrayList<NBTTagCompound>();
                    for (int i = 0; i < tags.tagCount(); i++)
                    {
                        final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                        if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null)
                        {
                            final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                            final short lvl = tagCompound.getShort("lvl");
                            if (enchantment != null)
                            {
                                String ench = "";
                                if (enchantment.isCurse())
                                {
                                    if (enchantment.getTranslatedName(lvl).contains("Vanish"))
                                        ench = ChatFormatting.RED + "Vanishing";
                                    else
                                        ench = ChatFormatting.RED + "Binding";

                                    stringsToDraw.add(ench);
                                    continue;
                                }
                                /*else if (ItemUtil.isIllegalEnchant(enchantment, lvl))
                                {
                                    ench = ChatFormatting.AQUA + enchantment.getTranslatedName(lvl);
                                }*/
                                else
                                {
                                    ench = enchantment.getTranslatedName(lvl);
                                }
                                stringsToDraw.add(ench);
                                /*
                                 * l_EnchantmentList.add(tagCompound);
                                 * 
                                 * String[] l_EnchantString = ench.split(" ");
                                 * 
                                 * String l_BuiltString = "";
                                 * 
                                 * boolean l_IgnoreCheck = lvl > 0 && !ench.contains("Mending");
                                 * 
                                 * for (int l_I = 0; l_I < (l_IgnoreCheck ? l_EnchantString.length - 1 : l_EnchantString.length); ++l_I) l_BuiltString += l_EnchantString[l_I].substring(0, 1);
                                 * 
                                 * if (l_IgnoreCheck) l_BuiltString += lvl;
                                 * 
                                 * stringsToDraw.add(l_BuiltString);
                                 */
                            }
                        }
                    }

                    CompareEnchantsToSelf(l_Itr, l_EnchantmentList);

                    float l_LastStringWidth = 0.0f;
                    int offsetY = 0;
                    for (int l_I = 0; l_I < stringsToDraw.size(); ++l_I)
                    {
                        String string = stringsToDraw.get(l_I);

                        if (l_I != stringsToDraw.size() - 1)
                            string += "   ";

                        if (l_I > 6)
                            break;

                        int offsetX = (int) (l_X + (l_I % 2) * l_LastStringWidth + 18);
                        offsetY = (int) l_TextY + (l_I / 2) * 8;

                        l_LastStringWidth = RenderUtil.getStringWidth(string);

                        RenderUtil.drawStringWithShadow(string, offsetX, offsetY, -1);
                    }

                    l_TextY = l_Y + 23;
                    /*
                     * String l_EnchString = "";
                     * 
                     * for (String s : stringsToDraw) l_EnchString += s + " ";
                     * 
                     * RenderUtil.drawStringWithShadow(l_EnchString, l_X+16, l_Y+4, -1);
                     */
                }
            }

            l_Itr++;
        }

        if (l_Entity.getHeldItemMainhand() != null && l_Entity.getHeldItemMainhand().getItem() != Items.AIR)
        {
            final List<String> stringsToDraw = Lists.newArrayList();

            if (l_Entity.getHeldItemMainhand().getEnchantmentTagList() != null)
            {
                final NBTTagList tags = l_Entity.getHeldItemMainhand().getEnchantmentTagList();
                for (int i = 0; i < tags.tagCount(); i++)
                {
                    final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                    if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null)
                    {
                        final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                        final short lvl = tagCompound.getShort("lvl");
                        if (enchantment != null)
                        {
                            String ench = "";
                            if (enchantment.isCurse())
                            {
                                if (enchantment.getTranslatedName(lvl).contains("Vanish"))
                                    ench = ChatFormatting.RED + "Vanishing";
                                else
                                    ench = ChatFormatting.RED + "Binding";
                            }
                            /*else if (ItemUtil.isIllegalEnchant(enchantment, lvl))
                            {
                                ench = ChatFormatting.AQUA + enchantment.getTranslatedName(lvl);
                            }*/
                            else
                            {
                                ench = enchantment.getTranslatedName(lvl);
                            }

                            stringsToDraw.add(ench);
                        }
                    }
                }
            }

            if (!stringsToDraw.isEmpty())
                stringsToDraw.sort(Comparator.comparing(String::length).thenComparing(s -> !s.contains("Vanishing")));

            String l_String = l_Entity.getHeldItemMainhand().getDisplayName();

            if (l_String.length() > 25)
                l_String = l_String.substring(0, 25);

            float l_X2 = GetX() + GetWidth() - (RenderUtil.getStringWidth(l_String) + 5);
            float l_Y2 = GetY() + GetHeight() - (6 * stringsToDraw.size()) - 5 - RenderUtil.getStringHeight(l_String) - 16;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(l_Entity.getHeldItemMainhand(), (int) l_X2 - 17, (int) l_Y2 - 5);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, l_Entity.getHeldItemMainhand(), (int) l_X2 - 17, (int) l_Y2 - 5);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            RenderUtil.drawStringWithShadow(ChatFormatting.LIGHT_PURPLE + l_String, l_X2, l_Y2, 0xFFFFFF);

            int l_I = 0;
            for (String s : stringsToDraw)
            {
                if (l_I > 8)
                    break;

                l_X2 = GetX() + GetWidth() - (RenderUtil.getStringWidth(s) + 5);
                l_Y2 = GetY() + GetHeight() - 5 - RenderUtil.getStringHeight(s) - (6 * l_I++) - 16;

                RenderUtil.drawStringWithShadow(s, l_X2, l_Y2, 0xFFFFFF);
            }
        }

        if (l_Entity.getHeldItemOffhand() != null && l_Entity.getHeldItemOffhand().getItem() != Items.AIR)
        {
            final List<String> stringsToDraw = Lists.newArrayList();

            String l_String = l_Entity.getHeldItemOffhand().getDisplayName();

            if (l_String.length() > 25)
                l_String = l_String.substring(0, 25);

            float l_X2 = GetX() + GetWidth() - (RenderUtil.getStringWidth(l_String) + 5);
            float l_Y2 = GetY() + GetHeight() - (6 * stringsToDraw.size()) - 5 - RenderUtil.getStringHeight(l_String) - 3;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(l_Entity.getHeldItemOffhand(), (int) l_X2 - 17, (int) l_Y2 - 5);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, l_Entity.getHeldItemOffhand(), (int) l_X2 - 17, (int) l_Y2 - 5);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            RenderUtil.drawStringWithShadow(ChatFormatting.LIGHT_PURPLE + l_String, l_X2, l_Y2, 0xFFFFFF);
        }

        if (l_Entity.getHeldItemMainhand() != ItemStack.EMPTY)
        {
            ItemStack l_ItemToRemove = null;

            boolean l_Readd = true;

            for (ItemStack l_PrevItem : HotbarGuesser)
            {
                if (l_PrevItem == ItemStack.EMPTY || l_PrevItem.getItem() == Items.AIR)
                    continue;

                if (l_PrevItem.getItem() == l_Entity.getHeldItemMainhand().getItem())
                {
                    if (l_PrevItem.getCount() != l_Entity.getHeldItemMainhand().getCount())
                        l_ItemToRemove = l_PrevItem;
                    else
                        l_Readd = false;

                    break;
                }
            }

            if (l_ItemToRemove != null)
                HotbarGuesser.remove(l_ItemToRemove);
            else if (l_Readd)
                HotbarGuesser.add(l_Entity.getHeldItemMainhand());
        }

        if (HotbarGuesser.size() > 9)
            HotbarGuesser.remove(0);

        {
            int l_I = 0;
            for (ItemStack itemStack : HotbarGuesser)
            {
                int offsetX = (int) GetX() + 16 * l_I++;
                int offsetY = (int) (GetY() + GetHeight()) - 16;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.popMatrix();
            }
        }

        /// Render the threat
        /// Compare gear
        int l_NoThreatMatches = 0;
        int l_MatchedThreatMatches = 0;
        int l_HighThreatMatches = 0;

        for (int l_I = 0; l_I < 4; ++l_I)
        {
            switch (InternalThreatLevel[l_I])
            {
                case 0:
                    ++l_HighThreatMatches;
                    break;
                case 1:
                    ++l_MatchedThreatMatches;
                    break;
                case 2:
                    ++l_NoThreatMatches;
                    break;
            }
        }

        if (l_HighThreatMatches > l_MatchedThreatMatches && l_HighThreatMatches > l_NoThreatMatches)
        {
            l_ThreatLevel = ThreatLevels.High;
        }
        else if ((l_MatchedThreatMatches > l_HighThreatMatches && l_MatchedThreatMatches > l_NoThreatMatches) || l_MatchedThreatMatches == l_HighThreatMatches)
        {
            l_ThreatLevel = ThreatLevels.Matched;

            float l_OurHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();

            if (l_OurHealth >= l_TheirHealth)
                l_ThreatLevel = ThreatLevels.Matched;
        }
        else
            l_ThreatLevel = ThreatLevels.None;

        String l_String = "";

        switch (l_ThreatLevel)
        {
            case None:
                l_String = ChatFormatting.GREEN + "Kill";
                break;
            case Matched:
                l_String = ChatFormatting.YELLOW + "Matched";
                break;
            case High:
                l_String = ChatFormatting.RED + "Threat";
                break;
        }

        RenderUtil.drawStringWithShadow(l_String, (GetX() + 27) - RenderUtil.getStringWidth(l_String), GetY() + 5, 0xFFFFFF);
        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.popMatrix();
    }

    private int[] InternalThreatLevel = new int[4];

    private void CompareEnchantsToSelf(int p_Itr, ArrayList<NBTTagCompound> p_EnchantmentList)
    {
        ItemStack l_Stack = mc.player.inventory.armorInventory.get(p_Itr);
        if (l_Stack == ItemStack.EMPTY)
        {
            InternalThreatLevel[p_Itr] = 0;
            return;
        }

        if (l_Stack.getEnchantmentTagList() != null)
        {
            final NBTTagList tags = l_Stack.getEnchantmentTagList();
            for (int i = 0; i < tags.tagCount(); i++)
            {
                final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null)
                {
                    final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                    final short lvl = tagCompound.getShort("lvl");
                    if (enchantment != null)
                    {
                        if (enchantment.isCurse())
                            continue;

                        for (NBTTagCompound l_EnemyTag : p_EnchantmentList)
                        {
                            if (l_EnemyTag.getShort("id") == tagCompound.getShort("id"))
                            {
                                /// todo: check all enchants
                                short l_EnemyLevel = l_EnemyTag.getShort("lvl");
                                if (l_EnemyLevel == lvl)
                                {
                                    InternalThreatLevel[p_Itr] = 1;
                                    return;
                                }
                                else if (lvl < l_EnemyLevel)
                                {
                                    InternalThreatLevel[p_Itr] = 0;
                                    return;
                                }
                                else
                                {
                                    InternalThreatLevel[p_Itr] = 2;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        InternalThreatLevel[p_Itr] = 0;
    }

    private String ToRomainNumerals(int p_Input)
    {
        switch (p_Input)
        {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
        }

        return "";
    }
}
