package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public final class NoRotateModule extends Module
{
    public NoRotateModule()
    {
        super("NoRotate", new String[]
        { "NoRot", "AntiRotate" }, "Prevents you from processing server rotations", "NONE", 0x24B2DB, ModuleType.MOVEMENT);
    }
    
    @Override
    public String getMetaData()
    {
        return "Packet";
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketPlayerPosLook)
        {
            if (mc.player != null && mc.currentScreen == null)
            {
                p_Event.cancel();
                EntityPlayer entityplayer = mc.player;
                final SPacketPlayerPosLook packetIn = (SPacketPlayerPosLook) p_Event.getPacket();
                double d0 = packetIn.getX();
                double d1 = packetIn.getY();
                double d2 = packetIn.getZ();
                float f = packetIn.getYaw();
                float f1 = packetIn.getPitch();

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X))
                {
                    d0 += entityplayer.posX;
                }
                else
                {
                    entityplayer.motionX = 0.0D;
                }

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y))
                {
                    d1 += entityplayer.posY;
                }
                else
                {
                    entityplayer.motionY = 0.0D;
                }

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z))
                {
                    d2 += entityplayer.posZ;
                }
                else
                {
                    entityplayer.motionZ = 0.0D;
                }

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT))
                {
                    f1 += entityplayer.rotationPitch;
                }

                if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT))
                {
                    f += entityplayer.rotationYaw;
                }
                entityplayer.setPosition(d0, d1, d2);
                mc.getConnection().sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
                mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(entityplayer.posX, entityplayer.getEntityBoundingBox().minY, entityplayer.posZ, packetIn.yaw, packetIn.pitch, false));
            }
        }
    });

}
