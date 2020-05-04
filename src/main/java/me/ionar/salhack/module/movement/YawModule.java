package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;

/**
 * Author Seth 5/1/2019 @ 7:56 PM.
 */
public final class YawModule extends Module
{
    public final Value<Boolean> yawLock = new Value<Boolean>("Yaw", new String[]
    { "Y" }, "Lock the player's rotation yaw if enabled.", true);
    public final Value<Boolean> pitchLock = new Value<Boolean>("Pitch", new String[]
    { "P" }, "Lock the player's rotation pitch if enabled.", false);
    public final Value<Boolean> Cardinal = new Value<Boolean>("Cardinal", new String[]
    { "C" }, "Locks the yaw to one of the cardinal directions", false);

    private float Yaw;
    private float Pitch;

    public YawModule()
    {
        super("Yaw", new String[]
        { "RotLock", "Rotation" }, "Locks you rotation for precision", "NONE", 0xDA24DB, ModuleType.MOVEMENT);
    }
    
    @Override
    public String getMetaData()
    {
        if (Cardinal.getValue())
            return "Cardinal";
        
        return "One";
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player != null)
        {
            Yaw = mc.player.rotationYaw;
            Pitch = mc.player.rotationPitch;
        }
    }
    
    @Override
    public void toggleNoSave()
    {
        /// override don't trigger on logic, we access player at enable
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        Entity l_Entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;
        
        if (this.yawLock.getValue())
            l_Entity.rotationYaw = Yaw;

        if (this.pitchLock.getValue())
            l_Entity.rotationPitch = Pitch;

        if (Cardinal.getValue())
            l_Entity.rotationYaw = Math.round((l_Entity.rotationYaw + 1.0f) / 45.0f) * 45.0f;
    });
}
