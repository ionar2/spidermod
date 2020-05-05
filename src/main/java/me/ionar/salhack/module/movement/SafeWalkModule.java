package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.player.EventPlayerMove;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class SafeWalkModule extends Module
{

    public final Value<Integer> height = new Value<Integer>("Height", new String[]
    { "Hei", "H" }, "The distance from the player on the Y-axis to run safe-walk checks for.", 1, 0, 32, 1);

    public SafeWalkModule()
    {
        super("SafeWalk", new String[]
        { "SWalk" }, "Prevents you from walking off certain blocks", "NONE", 0x6B24DB, ModuleType.MOVEMENT);
    }
    
    @EventHandler
    private Listener<EventPlayerMove> OnPlayerMove = new Listener<>(p_Event ->
    {
        double x = p_Event.X;
        double y = p_Event.Y;
        double z = p_Event.Z;
        
        if (mc.player.onGround && !mc.player.noClip)
        {
            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, -this.height.getValue(), 0.0D);)
            {
                if (x < increment && x >= -increment)
                {
                    x = 0.0D;
                }
                else if (x > 0.0D)
                {
                    x -= increment;
                }
                else
                {
                    x += increment;
                }
            }
            for (; z != 0.0D && isOffsetBBEmpty(0.0D, -this.height.getValue(), z);)
            {
                if (z < increment && z >= -increment)
                {
                    z = 0.0D;
                }
                else if (z > 0.0D)
                {
                    z -= increment;
                }
                else
                {
                    z += increment;
                }
            }
            for (; x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, -this.height.getValue(), z);)
            {
                if (x < increment && x >= -increment)
                {
                    x = 0.0D;
                }
                else if (x > 0.0D)
                {
                    x -= increment;
                }
                else
                {
                    x += increment;
                }
                if (z < increment && z >= -increment)
                {
                    z = 0.0D;
                }
                else if (z > 0.0D)
                {
                    z -= increment;
                }
                else
                {
                    z += increment;
                }
            }
        }
        
        p_Event.X = x;
        p_Event.Y = y;
        p_Event.Z = z;
    });

    private boolean isOffsetBBEmpty(double x, double y, double z)
    {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, y, z)).isEmpty();
    }

}
