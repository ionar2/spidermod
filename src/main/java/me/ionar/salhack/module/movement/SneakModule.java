package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.player.EventPlayerMove;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;

public final class SneakModule extends Module
{

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
    { "Mode", "M" }, "The sneak mode to use.", Mode.NCP);

    private enum Mode
    {
        Vanilla,
        NCP,
        Always
    }

    public SneakModule()
    {
        super("Sneak", new String[]
        { "Sneek" }, "Allows you to sneak at full speed", "NONE", 0xDB2493, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (mc.world != null && !mc.player.isSneaking())
        {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public String getMetaData()
    {
        return this.mode.getValue().name();
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        switch (this.mode.getValue())
        {
            case Vanilla:
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                break;
            case NCP:
                if (!mc.player.isSneaking())
                {
                    if (this.isMoving())
                    {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                    }
                    else
                    {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    }
                }
                break;
            case Always:
                mc.gameSettings.keyBindSneak.pressed = true;
                break;
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (this.mode.getValue() != Mode.Always)
        {
            if (p_Event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !mc.player.isSneaking())
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    });

    private boolean isMoving()
    {
        return GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
                || GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
    }

}
