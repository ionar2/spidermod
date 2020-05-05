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
import net.minecraft.item.ItemStack;

public final class AutoArmorModule extends Module
{

    public final Value<Float> delay = new Value("Delay", new String[]
    { "Del" }, "The amount of delay in milliseconds.", 50.0f, 0.0f, 1000.0f, 1.0f);
    public final Value<Boolean> curse = new Value("Curse", new String[]
    { "Curses" }, "Prevents you from equipping armor with cursed enchantments.", false);

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

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen instanceof GuiInventory)
        {
            return;
        }
        
        if (AutoMend != null && AutoMend.isEnabled())
            return;

        final ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();

        if (helm.getItem() == Items.AIR)
        {
            final int slot = this.findArmorSlot(EntityEquipmentSlot.HEAD);

            if (slot != -1)
            {
                if (slot <= 4)
                {
                    if (this.timer.passed(this.delay.getValue()))
                    {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 5, 0, ClickType.PICKUP, mc.player);
                        this.timer.reset();
                    }
                }
                else
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
            }
        }

        final ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();

        if (chest.getItem() == Items.AIR)
        {
            final int slot = this.findArmorSlot(EntityEquipmentSlot.CHEST);

            if (slot != -1)
            {
                if (slot <= 4)
                {
                    if (this.timer.passed(this.delay.getValue()))
                    {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                        this.timer.reset();
                    }
                }
                else
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
            }
        }

        final ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();

        if (legging.getItem() == Items.AIR)
        {
            final int slot = this.findArmorSlot(EntityEquipmentSlot.LEGS);

            if (slot != -1)
            {
                if (slot <= 4)
                {
                    if (this.timer.passed(this.delay.getValue()))
                    {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 7, 0, ClickType.PICKUP, mc.player);
                        this.timer.reset();
                    }
                }
                else
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
            }
        }

        final ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();

        if (feet.getItem() == Items.AIR)
        {
            final int slot = this.findArmorSlot(EntityEquipmentSlot.FEET);

            if (slot != -1)
            {
                if (slot <= 4)
                {
                    if (this.timer.passed(this.delay.getValue()))
                    {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 8, 0, ClickType.PICKUP, mc.player);
                        this.timer.reset();
                    }
                }
                else
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
            }
        }
    });

    private void clickSlot(int slot, int mouse, ClickType type)
    {
        if (this.timer.passed(this.delay.getValue()))
        {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, mouse, type, mc.player);
            this.timer.reset();
        }
    }

    private int findArmorSlot(EntityEquipmentSlot type)
    {
        int slot = -1;
        float damage = 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
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
            }
        }

        return slot;
    }

}
