package me.ionar.salhack.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;

public final class OffhandModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"Mode"}, "If you are above the required health for a totem, x will be used in offhand instead.", Modes.Gap);
    public final Value<Float> ToggleHealth = new Value<Float>("ToggleHealth", new String[]
    { "TH" }, "When you are below this value, this will disable the module.", 10.0f, 0.0f, 20.0f, 0.5f);
    
    public enum Modes
    {
        Gap,
        Crystal,
        Bow,
    }
    
    public OffhandModule()
    {
        super("Offhand", new String[]
        { "Totem" }, "Pauses AutoTotem and places something else in your offhand.", "NONE", 0xDADB24, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }
    
    private void SwitchOffHandIfNeed(Modes p_Val)
    {
        Item l_Item = GetItemFromModeVal(p_Val);
        
        if (mc.player.getHeldItemOffhand().getItem() != l_Item)
        {
            int l_Slot = PlayerUtil.GetRecursiveItemSlot(l_Item);
            
            if (l_Slot != -1)
            {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                        ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP,
                        mc.player);
                
                /// @todo: this might cause desyncs, we need a callback for windowclicks for transaction complete packet.
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                        ClickType.PICKUP, mc.player);
                mc.playerController.updateController();
                
                SendMessage(ChatFormatting.LIGHT_PURPLE + "Offhand now has a " + GetItemNameFromModeVal(Mode.getValue()));
            }
        }
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen != null && (!(mc.currentScreen instanceof GuiInventory) && !(mc.currentScreen instanceof SalGuiScreen)))
            return;
        
        if (PlayerUtil.GetHealthWithAbsorption() < ToggleHealth.getValue())
        {
            SendMessage("You are below the ToggleHealth requirement, toggling..");
            toggle();
            return;
        }
        
        SwitchOffHandIfNeed(Mode.getValue());
    });
    
    public Item GetItemFromModeVal(Modes p_Val)
    {
        switch (p_Val)
        {
            case Crystal:
                return Items.END_CRYSTAL;
            case Gap:
                return Items.GOLDEN_APPLE;
            case Bow:
                return Items.BOW;
            default:
                break;
        }
        
        return Items.TOTEM_OF_UNDYING;
    }

    private String GetItemNameFromModeVal(Modes p_Val)
    {
        switch (p_Val)
        {
            case Crystal:
                return "End Crystal";
            case Gap:
                return "Gap";
            case Bow:
                return "Bow";
            default:
                break;
        }
        
        return "Totem";
    }
}