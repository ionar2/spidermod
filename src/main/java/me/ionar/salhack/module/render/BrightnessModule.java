package me.ionar.salhack.module.render;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public final class BrightnessModule extends Module
{
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
    { "Mode", "M" }, "The brightness mode to use.", Mode.Gamma);

    private enum Mode
    {
        Gamma,
        Potion,
        Table
    }

    public final Value<Boolean> effects = new Value<Boolean>("Effects", new String[]
    { "Eff" }, "Blocks blindness & nausea effects if enabled.", true);

    private float lastGamma;

    private World world;
    private Timer timer = new Timer();

    public BrightnessModule()
    {
        super("Brightness", new String[]
        { "FullBright", "Bright" }, "Makes the world brighter", "NONE", 0xD5DB24, ModuleType.RENDER);
        setHidden(true);
    }
    
    @Override
    public String getMetaData()
    {
        return String.valueOf(mode.getValue());
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        if (this.mode.getValue() == Mode.Gamma)
        {
            this.lastGamma = mc.gameSettings.gammaSetting;
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (this.mode.getValue() == Mode.Gamma)
        {
            mc.gameSettings.gammaSetting = this.lastGamma;
        }

        if (this.mode.getValue() == Mode.Potion && mc.player != null)
        {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }

        if (this.mode.getValue() == Mode.Table)
        {
            if (mc.world != null)
            {
                float f = 0.0F;

                for (int i = 0; i <= 15; ++i)
                {
                    float f1 = 1.0F - (float) i / 15.0F;
                    mc.world.provider.getLightBrightnessTable()[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
                }
            }
        }
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        switch (this.mode.getValue())
        {
            case Gamma:
                /// todo: use timer here
                
                mc.gameSettings.gammaSetting = 1000;
                mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
                break;
            case Potion:
                mc.gameSettings.gammaSetting = 1.0f;
                mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
                break;
            case Table:
                if (this.world != mc.world)
                {
                    if (mc.world != null)
                    {
                        for (int i = 0; i <= 15; ++i)
                        {
                            mc.world.provider.getLightBrightnessTable()[i] = 1.0f;
                        }
                    }
                    this.world = mc.world;
                }
                break;
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.GetPacket() instanceof SPacketEntityEffect)
        {
            if (this.effects.getValue())
            {
                final SPacketEntityEffect packet = (SPacketEntityEffect) p_Event.GetPacket();
                if (mc.player != null && packet.getEntityId() == mc.player.getEntityId())
                {
                    if (packet.getEffectId() == 9 || packet.getEffectId() == 15)
                    {
                        p_Event.cancel();
                    }
                }
            }
        }
    });

}
