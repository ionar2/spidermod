package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;

public class ChestStealerModule extends Module
{
    public Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "M" }, "The mode for chest stealer", Modes.Steal);
    public Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "D" }, "Delay for each tick", 1f, 0f, 10f, 1f);
    public Value<Boolean> DepositShulkers = new Value<Boolean>("DepositShulkers", new String[]
    { "S" }, "Only deposit shulkers", false);
    public Value<Boolean> EntityChests = new Value<Boolean>("EntityChests", new String[]
    { "EC" }, "Take from entity chests", false);
    public Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[]
    { "EC" }, "Take from shulkers", false);

    public enum Modes
    {
        Steal,
        Store,
        Drop,
    }

    public ChestStealerModule()
    {
        super("ChestStealer", new String[]
        { "Chest" }, "Steals the contents from chests", "NONE", 0xDB5E24, ModuleType.MISC);
    }

    private Timer timer = new Timer();

    @Override
    public String getMetaData()
    {
        return Mode.getValue().toString();
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (!timer.passed(Delay.getValue() * 100f))
            return;

        timer.reset();

        if (mc.currentScreen instanceof GuiChest)
        {
            GuiChest l_Chest = (GuiChest) mc.currentScreen;

            for (int l_I = 0; l_I < l_Chest.lowerChestInventory.getSizeInventory(); ++l_I)
            {
                ItemStack l_Stack = l_Chest.lowerChestInventory.getStackInSlot(l_I);

                if ((l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR) && Mode.getValue() == Modes.Store)
                {
                    HandleStoring(l_Chest.inventorySlots.windowId, l_Chest.lowerChestInventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(l_Stack.getItem() instanceof ItemShulkerBox))
                    continue;
                
                if (l_Stack.isEmpty())
                    continue;

                switch (Mode.getValue())
                {
                    case Steal:
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case Drop:
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
        else if (mc.currentScreen instanceof GuiScreenHorseInventory && EntityChests.getValue())
        {
            GuiScreenHorseInventory l_Chest = (GuiScreenHorseInventory)mc.currentScreen;
            
            for (int l_I = 0; l_I < l_Chest.horseInventory.getSizeInventory(); ++l_I)
            {
                ItemStack l_Stack = l_Chest.horseInventory.getStackInSlot(l_I);

                if ((l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR) && Mode.getValue() == Modes.Store)
                {
                    HandleStoring(l_Chest.inventorySlots.windowId, l_Chest.horseInventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(l_Stack.getItem() instanceof ItemShulkerBox))
                    continue;
                
                if (l_Stack.isEmpty())
                    continue;

                switch (Mode.getValue())
                {
                    case Steal:
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case Drop:
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
        else if (mc.currentScreen instanceof GuiShulkerBox && Shulkers.getValue())
        {
            GuiShulkerBox l_Chest = (GuiShulkerBox)mc.currentScreen;
            
            for (int l_I = 0; l_I < l_Chest.inventory.getSizeInventory(); ++l_I)
            {
                ItemStack l_Stack = l_Chest.inventory.getStackInSlot(l_I);

                if ((l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR) && Mode.getValue() == Modes.Store)
                {
                    HandleStoring(l_Chest.inventorySlots.windowId, l_Chest.inventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(l_Stack.getItem() instanceof ItemShulkerBox))
                    continue;

                if (l_Stack.isEmpty())
                    continue;
                
                switch (Mode.getValue())
                {
                    case Steal:
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case Drop:
                        mc.playerController.windowClick(l_Chest.inventorySlots.windowId, l_I, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
    });

    private void HandleStoring(int p_WindowId, int p_Slot)
    {
        if (Mode.getValue() == Modes.Store)
        {
            for (int l_Y = 9; l_Y < mc.player.inventoryContainer.inventorySlots.size() - 1; ++l_Y)
            {
                ItemStack l_InvStack = mc.player.inventoryContainer.getSlot(l_Y).getStack();

                if (l_InvStack.isEmpty() || l_InvStack.getItem() == Items.AIR)
                    continue;

                if (Shulkers.getValue() && !(l_InvStack.getItem() instanceof ItemShulkerBox))
                    continue;

                mc.playerController.windowClick(p_WindowId, l_Y + p_Slot, 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }
}
