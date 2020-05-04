package me.ionar.salhack.module.render;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

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
}
