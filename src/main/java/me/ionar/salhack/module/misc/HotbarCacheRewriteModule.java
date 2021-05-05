package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class HotbarCacheRewriteModule extends Module
{
    public final Value<ItemList> Item = new Value<ItemList>("Item", new String[] {"M"}, "The mode of refilling to use, Refill may cause desync", ItemList.Pickaxe);
    public final Value<PickMat> PickMaterial = new Value<PickMat>("PickMaterial", new String[] {"M"}, "Pick Material", PickMat.Netherite);

    public enum ItemList {
        Pickaxe,
        Gapple,
        Crystal,
    }

    public enum PickMat {
        Netherite,
        Diamond,
        None,
    }

    public HotbarCacheRewriteModule()
    {
        super("HotbarCacheRewrite", new String[] {"HC"}, "Automatically refills your hotbar similar to how autototem works", "NONE", 0xB324DB, ModuleType.MISC);
    }


    @Override
    public String getMetaData()
    {
        return String.valueOf(Item.getValue());
    }

    @Override
    public void toggleNoSave()
    {

    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        ItemStack curentItem = mc.player.inventory.getCurrentItem();

        switch (Item.getValue())
        {
            case Pickaxe:
                if (String.valueOf(PickMaterial.getValue()) == "Netherite")
                {
                    if (curentItem.getItem() != Items.STONE_PICKAXE)
                    {
                        for (int l_I = 0; l_I < 9; ++l_I)
                        {
                            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                            if (mc.player.inventory.getStackInSlot(l_I).getItem() == Items.STONE_PICKAXE)
                            {
                                mc.player.inventory.currentItem = l_I;
                                break;
                            }
                        }
                    }
                }
                else if (String.valueOf(PickMaterial.getValue()) == "Diamond")
                {
                    if (curentItem.getItem() != Items.DIAMOND_PICKAXE)
                    {
                        for (int l_I = 0; l_I < 9; ++l_I)
                        {
                            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                            if (mc.player.inventory.getStackInSlot(l_I).getItem() == Items.DIAMOND_PICKAXE)
                            {
                                mc.player.inventory.currentItem = l_I;
                                break;
                            }
                        }
                    }
                }
                break;

            case Crystal:
                if (curentItem.getItem() != Items.END_CRYSTAL)
                    for (int l_I = 0; l_I < 9; ++l_I)
                    {
                        ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                        if (mc.player.inventory.getStackInSlot(l_I).getItem() == Items.END_CRYSTAL)
                        {
                            mc.player.inventory.currentItem = l_I;
                            break;
                        }
                    }
                break;

            case Gapple:
                if (curentItem.getItem() != Items.GOLDEN_APPLE)
                    for (int l_I = 0; l_I < 9; ++l_I)
                    {
                        ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                        if (mc.player.inventory.getStackInSlot(l_I).getItem() == Items.GOLDEN_APPLE)
                        {
                            mc.player.inventory.currentItem = l_I;
                            break;
                        }
                    }
                break;
        }
    });


}
