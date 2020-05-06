package me.ionar.salhack.module.misc;

import java.util.ArrayList;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HotbarCacheModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "The mode of refilling to use, Refill may cause desync", Modes.Cache);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);
    
    public enum Modes
    {
        Cache,
        Refill,
    }
    
    public HotbarCacheModule()
    {
        super("HotbarCache", new String[] {"HC"}, "Automatically refills your hotbar similar to how autototem works", "NONE", 0xB324DB, ModuleType.MISC);
    }
    
    private ArrayList<Item> Hotbar = new ArrayList<Item>();
    private Timer timer = new Timer();
    
    @Override
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        Hotbar.clear();
        
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (!l_Stack.isEmpty() && !Hotbar.contains(l_Stack.getItem()))
                Hotbar.add(l_Stack.getItem());
            else
                Hotbar.add(Items.AIR);
        }
    }
    
    /// Don't activate on startup
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen != null)
            return;
        
        if (!timer.passed(Delay.getValue() * 1000))
            return;
        
        switch (Mode.getValue())
        {
            case Cache:
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    if (SwitchSlotIfNeed(l_I))
                    {
                        timer.reset();
                        return;
                    }
                }
                break;
            case Refill:
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    if (RefillSlotIfNeed(l_I))
                    {
                        timer.reset();
                        return;
                    }
                }
                break;
            default:
                break;
        }
    });

    private boolean SwitchSlotIfNeed(int p_Slot)
    {
        Item l_Item = Hotbar.get(p_Slot);
        
        if (l_Item == Items.AIR)
            return false;
        
        if (!mc.player.inventory.getStackInSlot(p_Slot).isEmpty() && mc.player.inventory.getStackInSlot(p_Slot).getItem() == l_Item)
            return false;
        
        int l_Slot = PlayerUtil.GetItemSlot(l_Item);
        
        if (l_Slot != -1 && l_Slot != 45)
        {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                    ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, p_Slot+36, 0, ClickType.PICKUP,
                    mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                    ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
            
            return true;
        }
        
        return false;
    }

    private boolean RefillSlotIfNeed(int p_Slot)
    {
        ItemStack l_Stack = mc.player.inventory.getStackInSlot(p_Slot);
        
        if (l_Stack.isEmpty() || l_Stack.getItem() == Items.AIR)
            return false;
        
        if (!l_Stack.isStackable())
            return false;
        
        if (l_Stack.getCount() >= l_Stack.getMaxStackSize())
            return false;
        
        /// We're going to search the entire inventory for the same stack, WITH THE SAME NAME, and use quick move.
        for (int l_I = 9; l_I < 36; ++l_I)
        {
            final ItemStack l_Item = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Item.isEmpty())
                continue;
            
            if (CanItemBeMergedWith(l_Stack, l_Item))
            {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_I, 0,
                        ClickType.QUICK_MOVE, mc.player);
                mc.playerController.updateController();
                
                /// Check again for more next available tick
                return true;
            }
        }
        
        return false;
    }
    
    private boolean CanItemBeMergedWith(ItemStack p_Source, ItemStack p_Target)
    {
        return p_Source.getItem() == p_Target.getItem() && p_Source.getDisplayName().equals(p_Target.getDisplayName());
    }
}
