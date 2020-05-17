package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.misc.AutoMendArmorModule;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;

public final class AutoArmorModule extends Module
{
    public final Value<Float> delay = new Value<Float>("Delay", new String[]
    { "Del" }, "The amount of delay in milliseconds.", 50.0f, 0.0f, 1000.0f, 1.0f);
    public final Value<Boolean> curse = new Value<Boolean>("Curse", new String[]
    { "Curses" }, "Prevents you from equipping armor with cursed enchantments.", false);
    public final Value<Boolean> PreferElytra = new Value<Boolean>("Elytra", new String[] {"Wings"}, "Prefers elytra over chestplate if available", false);

    private Timer timer = new Timer();

    public AutoArmorModule()
    {
        super("AutoArmor", new String[]
        { "AutoArm", "AutoArmour" }, "Automatically equips armor", "NONE", 0x249FDB, ModuleType.COMBAT);
    }
    
    private AutoMendArmorModule AutoMend = null;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        AutoMend = (AutoMendArmorModule)ModuleManager.Get().GetMod(AutoMendArmorModule.class);
    }
    
    private void SwitchItemIfNeed(ItemStack p_Stack, EntityEquipmentSlot p_Slot, int p_ArmorSlot)
    {
        if (p_Stack.getItem() == Items.AIR)
        {
            if (!timer.passed(delay.getValue()))
                return;
            
            timer.reset();
            
            final int l_FoundSlot = findArmorSlot(p_Slot);

            if (l_FoundSlot != -1)
            {
                /// support for xcarry
                if (l_FoundSlot <= 4)
                {
                    /// We can't use quick move for this. have to send 2 packets, pickup and drop down.
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_FoundSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, p_ArmorSlot, 0, ClickType.PICKUP, mc.player);
                }
                else
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_FoundSlot, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen instanceof GuiInventory)
            return;
        
        if (AutoMend != null && AutoMend.isEnabled())
            return;
        
        SwitchItemIfNeed(mc.player.inventoryContainer.getSlot(5).getStack(), EntityEquipmentSlot.HEAD, 5);
        SwitchItemIfNeed(mc.player.inventoryContainer.getSlot(6).getStack(), EntityEquipmentSlot.CHEST, 6);
        SwitchItemIfNeed(mc.player.inventoryContainer.getSlot(7).getStack(), EntityEquipmentSlot.LEGS, 7);
        SwitchItemIfNeed(mc.player.inventoryContainer.getSlot(8).getStack(), EntityEquipmentSlot.FEET, 8);
    });

    private int findArmorSlot(EntityEquipmentSlot type)
    {
        int slot = -1;
        float damage = 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            /// @see: https://wiki.vg/Inventory, 0 is crafting slot, and 5,6,7,8 are Armor slots
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;
            
            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            if (s != null && s.getItem() != Items.AIR)
            {
                if (s.getItem() instanceof ItemArmor)
                {
                    final ItemArmor armor = (ItemArmor) s.getItem();
                    if (armor.armorType == type)
                    {
                        final float currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s));

                        final boolean cursed = this.curse.getValue() ? (EnchantmentHelper.hasBindingCurse(s)) : false;

                        if (currentDamage > damage && !cursed)
                        {
                            damage = currentDamage;
                            slot = i;
                        }
                    }
                }
                else if (type == EntityEquipmentSlot.CHEST && PreferElytra.getValue() && s.getItem() instanceof ItemElytra)
                    return i;
            }
        }

        return slot;
    }

}
