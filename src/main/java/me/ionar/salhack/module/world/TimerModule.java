package me.ionar.salhack.module.world;

import java.text.DecimalFormat;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public final class TimerModule extends Module
{

    public final Value<Float> speed = new Value<Float>("Speed", new String[]
    { "Spd" }, "Tick-rate multiplier. [(20tps/second) * (this value)]", 4.0f, 0.0f, 10.0f, 0.1f);
    public final Value<Boolean> Accelerate = new Value<Boolean>("Accelerate", new String[]
    { "Acc" }, "Accelerate's from 1.0 until the anticheat lags you back", true);
    public final Value<Boolean> TPSSync = new Value<Boolean>("TPSSync", new String[]
    { "TPS" }, "Syncs the game time to the current TPS", false);

    private Timer timer = new Timer();

    public TimerModule()
    {
        super("Timer", new String[]
        { "Time", "Tmr" }, "Speeds up the client tick rate", "NONE", 0x24DBA3, ModuleType.WORLD);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.timer.tickLength = 50;
    }
    
    private float OverrideSpeed = 1.0f;
    
    /// store this as member to save cpu
    private DecimalFormat l_Format = new DecimalFormat("#.#");

    @Override
    public String getMetaData()
    {
        if (OverrideSpeed != 1.0f)
            return String.valueOf(OverrideSpeed);
        
        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            return l_Format.format((l_TPS/20));
        }
        
        return l_Format.format(speed.getValue());
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (OverrideSpeed != 1.0f)
        {
            mc.timer.tickLength = 50.0f / OverrideSpeed;
            return;
        }
        
        if (TPSSync.getValue())
        {
            float l_TPS = TickRateManager.Get().getTickRate();

            mc.timer.tickLength = 50.0f * (20/l_TPS);
        }
        else
            mc.timer.tickLength = 50.0f / speed.getValue();

        if (Accelerate.getValue() && timer.passed(2000))
        {
            timer.reset();
            speed.setValue(speed.getValue() + 0.1f);
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketPlayerPosLook && Accelerate.getValue())
        {
            speed.setValue(1.0f);
        }
    });

    public void SetOverrideSpeed(float f)
    {
        OverrideSpeed = f;
    }

}
