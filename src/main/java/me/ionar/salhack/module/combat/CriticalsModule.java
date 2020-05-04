package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;

public class CriticalsModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "Mode to change to for criticals", Modes.Packet);
    
    public enum Modes
    {
        Packet,
        Jump,
    }
    
    public CriticalsModule()
    {
        super("Criticals", new String[]
        { "BS" }, "Releases the bow as fast as possible", "NONE", 0xF2190E, ModuleType.COMBAT);
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof CPacketUseEntity)
        {
            CPacketUseEntity l_Packet = (CPacketUseEntity)p_Event.getPacket();
            
            if (l_Packet.getAction() == CPacketUseEntity.Action.ATTACK)
            {
                if (l_Packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase && mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown())
                {
                    switch (Mode.getValue())
                    {
                        case Jump:
                            mc.player.jump();
                            break;
                        case Packet:
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    });
}
