package me.ionar.salhack.module.misc;

import java.util.Timer;
import java.util.TimerTask;

import me.ionar.salhack.module.Module;

public final class AntiAim extends Module
{
    public AntiAim()
    {
        super("AntiAim", new String[]
        { "BuildH", "BHeight" }, "Moves your head in different directions", "NONE", 0xDB24C4, ModuleType.MISC);
    }

    private Timer timer = new Timer();

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player == null)
        {
            Yaw = mc.player.rotationYaw;
            Pitch = mc.player.rotationPitch;
        }

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                        if (p_Event.getEra() != Era.PRE)
            return;
        
        Entity l_Entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player; // for real what is this
            l_Entity.rotationYaw = 90f;
            l_Entity.rotationPitch = 180f;
            l_Entity.rotationYaw = 95f;
            l_Entity.rotationPitch = 175f;
            //mc.player.sendChatMessage("");
            }
        }, 0, 120000);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (timer != null)
            timer.cancel();
    }
}
