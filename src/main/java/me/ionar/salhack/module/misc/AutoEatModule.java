package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AutoEatModule extends Module
{
    public final Value<Float> HealthToEatAt = new Value<Float>("HealthToEatAt", new String[] {"Health"}, "Will eat gaps at required health", 15.0f, 0.0f, 36.0f, 3.0f);
    public final Value<Float> RequiredHunger = new Value<Float>("Hunger", new String[] {"Hunger"}, "Required hunger to eat", 18.0f, 0.0f, 20.0f, 1.0f);
    
    public AutoEatModule()
    {
        super("AutoEat", new String[] {"Eat"}, "Automatically eats food, depending on hunger, or health", "NONE", 0xFFFB11, ModuleType.MISC);
    }

    private boolean m_WasEating = false;

    @Override
    public void onDisable()
    {
        super.onDisable();
        
        if (m_WasEating)
        {
            m_WasEating = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        float l_Health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        
        if (HealthToEatAt.getValue() >= l_Health && !PlayerUtil.IsEating())
        {
            if (mc.player.getHeldItemMainhand().getItem() != Items.GOLDEN_APPLE)
            {
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    if (mc.player.inventory.getStackInSlot(l_I).isEmpty() || mc.player.inventory.getStackInSlot(l_I).getItem() != Items.GOLDEN_APPLE)
                        continue;
                    
                    mc.player.inventory.currentItem = l_I;
                    mc.playerController.updateController();
                    break;
                }

                if (mc.currentScreen == null)
                    mc.gameSettings.keyBindUseItem.pressed = true;
                else
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                
                m_WasEating = true;
            }
            return;
        }
        
        if (!PlayerUtil.IsEating() && RequiredHunger.getValue() >= mc.player.getFoodStats().getFoodLevel())
        {
            boolean l_CanEat = false;
            
            for (int l_I = 0; l_I < 9; ++l_I)
            {
                ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                
                if (mc.player.inventory.getStackInSlot(l_I).isEmpty())
                    continue;
                
                if (l_Stack.getItem() instanceof ItemFood)
                {
                    l_CanEat = true;
                    mc.player.inventory.currentItem = l_I;
                    mc.playerController.updateController();
                    break;
                }
            }
            
            if (l_CanEat)
            {
                if (mc.currentScreen == null)
                    mc.gameSettings.keyBindUseItem.pressed = true;
                else
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                
                m_WasEating = true;
            }
        }

        if (m_WasEating)
        {
            m_WasEating = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
    });
}
