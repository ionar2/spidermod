package me.ionar.salhack.module.render;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.EventRenderUpdateEquippedItem;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.util.EnumHand;

public class HandProgressModule extends Module
{
    public final Value<Float> MainProgress = new Value<Float>("MainProgress", new String[] {""}, "Mainhand progress", 0.5f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> OffProgress = new Value<Float>("OffProgress", new String[] {""}, "Offhand progress", 0.5f, 0.0f, 1.0f, 0.1f);
    
    public HandProgressModule()
    {
        super("HandProgress", new String[]
        { "SmallShield", "SS" }, "Smaller view of mainhand/offhand, smallshield", "NONE", 0xDB244B, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        mc.entityRenderer.itemRenderer.equippedProgressMainHand = MainProgress.getValue();
        mc.entityRenderer.itemRenderer.equippedProgressOffHand = OffProgress.getValue();
    });

    @EventHandler
    private Listener<EventRenderUpdateEquippedItem> OnUpdateEquippedItem = new Listener<>(p_Event ->
    {
       // p_Event.cancel();
        mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        mc.entityRenderer.itemRenderer.itemStackOffHand = mc.player.getHeldItem(EnumHand.OFF_HAND);
    });
}
