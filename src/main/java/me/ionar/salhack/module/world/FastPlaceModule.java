package me.ionar.salhack.module.world;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;

public final class FastPlaceModule extends Module
{

    public final Value<Boolean> xp = new Value<Boolean>("XP", new String[]
    { "EXP" }, "Only activate while holding XP bottles.", false);
    public final Value<Boolean> Crystals = new Value<Boolean>("Crystals", new String[]
    { "Cry" }, "Active only when using crystals", false);

    public FastPlaceModule()
    {
        super("FastPlace", new String[]
        { "Fp" }, "Removes place delay", "NONE", 0x66DB24, ModuleType.WORLD);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.rightClickDelayTimer = 6;
    }

    @Override
    public String getMetaData()
    {
        if (xp.getValue())
            return "EXP:" + this.getItemCount(Items.EXPERIENCE_BOTTLE);

        return null;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (this.xp.getValue())
        {
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle
                    || mc.player.getHeldItemOffhand().getItem() instanceof ItemExpBottle)
            {
                mc.rightClickDelayTimer = 0;
            }
        }
        else if (Crystals.getValue())
        {
            if (mc.player.inventory.getCurrentItem().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)
                mc.rightClickDelayTimer = 0;
        }
        else
        {
            mc.rightClickDelayTimer = 0;
        }
    });

    private int getItemCount(Item input)
    {
        int items = 0;

        for (int i = 0; i < 45; i++)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == input)
            {
                items += stack.getCount();
            }
        }

        return items;
    }
}
