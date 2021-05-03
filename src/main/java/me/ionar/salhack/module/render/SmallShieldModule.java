package me.ionar.salhack.module.render;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.EventRenderUpdateEquippedItem;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;

public class SmallShieldModule extends Module
{
    public final Value<Float> MainHand = new Value<Float>("MainHand", new String[] {""}, "Mainhand progress", 0.5f, 0.0f, 3.0f, 0.1f);
    public final Value<Float> OffHand = new Value<Float>("OffHand", new String[] {""}, "Offhand progress", 0.5f, 0.0f, 3.0f, 0.1f);
    public final Value<Boolean> TallSword = new Value<Boolean>("TallSword", new String[] {""}, "Makes sword tall.", false);
    
    public SmallShieldModule()
    {
        super("SmallShield", new String[]
        { "SmallShield", "SS" }, "Smaller view of mainhand/offhand, smallshield", "NONE", 0xDB244B, ModuleType.RENDER);
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (TallSword.getValue()) {
            if(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
                mc.getItemRenderer().equippedProgressMainHand = 2.0f;
            } else {
                mc.entityRenderer.itemRenderer.equippedProgressMainHand = MainHand.getValue();
            }
            mc.entityRenderer.itemRenderer.equippedProgressOffHand = OffHand.getValue();
        } else {
            mc.entityRenderer.itemRenderer.equippedProgressMainHand = MainHand.getValue();
            mc.entityRenderer.itemRenderer.equippedProgressOffHand = OffHand.getValue();
        }
    });

    @EventHandler
    private Listener<EventRenderUpdateEquippedItem> OnUpdateEquippedItem = new Listener<>(p_Event ->
    {
       // p_Event.cancel();
        mc.entityRenderer.itemRenderer.itemStackMainHand = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        mc.entityRenderer.itemRenderer.itemStackOffHand = mc.player.getHeldItem(EnumHand.OFF_HAND);
    });
}
