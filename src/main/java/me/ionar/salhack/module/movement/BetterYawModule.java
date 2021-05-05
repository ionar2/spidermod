package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;

public final class BetterYawModule extends Module
{
    public final Value<Modes> Direction = new Value<Modes>("Mode", new String[]
            { "Mode" }, "Direction", Modes.PosX);

    public enum Modes
    {
        PosX,
        PosZ,
        NegX,
        NegZ
    }

    public BetterYawModule()
    {
        super("BetterYaw", new String[]
                { "RotLock", "Rotation" }, "Locks your rotation for precision", "NONE", 0xDA24DB, ModuleType.MOVEMENT);
    }

    @Override
    public String getMetaData()
    {
        return String.valueOf(Direction.getValue());
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event -> {
        if (mc.player != null && Direction.getValue() == Modes.PosX)
        {
            mc.player.rotationYaw = 270;
        }
        if (mc.player != null && Direction.getValue() == Modes.NegX)
        {
            mc.player.rotationYaw = 90;
        }
        if (mc.player != null && Direction.getValue() == Modes.PosZ)
        {
            mc.player.rotationYaw = 0;
        }
        if (mc.player != null && Direction.getValue() == Modes.NegZ)
        {
            mc.player.rotationYaw = 180;
        }
    });

}
