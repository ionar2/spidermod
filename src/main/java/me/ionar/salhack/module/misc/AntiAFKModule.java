package me.ionar.salhack.module.misc;

import java.util.Timer;
import java.util.TimerTask;

import me.ionar.salhack.module.Module;

public final class AntiAFKModule extends Module
{
    public AntiAFKModule()
    {
        super("AntiAFK", new String[]
        { "BuildH", "BHeight" }, "AntiAFK", "NONE", 0xDB24C4, ModuleType.MISC);
    }
    
    private Timer timer = new Timer();

    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.player == null)
        {
            toggle();
            return;
        }
        
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                mc.player.sendChatMessage("/stats");
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
