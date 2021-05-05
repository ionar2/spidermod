package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.movement.AutoWalkModule;
import me.ionar.salhack.module.world.NukerBypassModule;
import me.ionar.salhack.module.world.NukerModule;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import me.ionar.salhack.util.Timer;

import static net.minecraft.init.MobEffects.FIRE_RESISTANCE;

public class AutoGapModule extends Module
{
    public final Value<Modes> NukerToOverride = new Value<Modes>("NukerToOverride", new String[] { "Mode" }, "Nuker to Override", Modes.NukerBypass);

    public enum Modes
    {
        Nuker,
        NukerBypass
    }

    public AutoGapModule()
    {
        super("AutoGap", new String[] {"Eat"}, "Automatically eats food, depending on hunger, or health", "NONE", 0xFFFB11, ModuleType.MISC);
    }

    private boolean m_WasEating = false;
    private boolean m_FireRes = false;
    private boolean needToEat = false;
    private Timer eatTimer = new Timer();

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
        m_FireRes = mc.player.isPotionActive(FIRE_RESISTANCE);
        if (m_FireRes && !m_WasEating)
        {
            for (int l_I = 0; l_I < 9; ++l_I)
            {
                if (mc.player.inventory.getStackInSlot(l_I).isEmpty() || mc.player.inventory.getStackInSlot(l_I).getItem() != Items.GOLDEN_APPLE)
                    continue;
                mc.player.inventory.currentItem = l_I;
                mc.playerController.updateController();
                break;
            }
            m_FireRes = mc.player.isPotionActive(FIRE_RESISTANCE);

            if (mc.currentScreen == null) {
                if (NukerToOverride.getValue() == Modes.Nuker) {
                    ModuleManager.Get().GetMod(NukerModule.class).setEnabled(false);
                    ModuleManager.Get().GetMod(AutoWalkModule.class).setEnabled(false);
                }
                else if (NukerToOverride.getValue() == Modes.NukerBypass) {
                    ModuleManager.Get().GetMod(NukerBypassModule.class).setEnabled(false);
                    ModuleManager.Get().GetMod(AutoWalkModule.class).setEnabled(false);
                }
                m_WasEating = false;
                mc.gameSettings.keyBindUseItem.pressed = true;
                return;
            }
            else {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
            }
            m_WasEating = true;
            return;
        }

        if (m_WasEating) {
            m_WasEating = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
            if (NukerToOverride.getValue() == Modes.Nuker && eatTimer.passed(10000)) {
                ModuleManager.Get().GetMod(NukerModule.class).setEnabled(true);
                ModuleManager.Get().GetMod(AutoWalkModule.class).setEnabled(true);
                eatTimer.reset();
            }
            if (NukerToOverride.getValue() == Modes.NukerBypass && eatTimer.passed(10000)) {
                ModuleManager.Get().GetMod(NukerBypassModule.class).setEnabled(true);
                ModuleManager.Get().GetMod(AutoWalkModule.class).setEnabled(true);
                eatTimer.reset();
            }
        }
        else {
            return;
        }

    });
}
