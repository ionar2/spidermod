package me.ionar.salhack.module.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public final class GlobalLocationModule extends Module
{
    public final Value<Boolean> thunder = new Value<Boolean>("Thunder", new String[]
            { "thund" }, "Logs positions of thunder/lightning sounds.", true);
    public final Value<Boolean> slimes = new Value<Boolean>("Slimes", new String[]
            { "slime" }, "Logs positions of slime spawns.", false);
    public final Value<Boolean> Wither = new Value<Boolean>("Wither", new String[]
            { "Wither" }, "Logs positions of Wither spawns.", false);
    public final Value<Boolean> EndPortal = new Value<Boolean>("End Portal", new String[]
            { "EndPortal" }, "Logs positions of EndPortal spawns.", false);
    public final Value<Boolean> EnderDragon = new Value<Boolean>("Ender Dragon", new String[]
            { "ED" }, "Logs positions of EnderDragon spawns.", false);
    public final Value<Boolean> Donkey = new Value<Boolean>("Donkey", new String[]
            { "Donkey" }, "logs location of donkey spawns", false);
    public final Value<Boolean> Llama = new Value<Boolean>("Llama", new String[]
            { "Llama" }, "logs location of llama spawns", false);
    public final Value<Boolean> Mule = new Value<Boolean>("Mule", new String[]
            { "Mule" }, "logs location of mule spawns", false);

    public GlobalLocationModule()
    {
        super("GlobalLocation", new String[]
                { "WitherLocationModule" }, "Logs in chat where a global sound happened (Warning can send current location if server changed the packet!)", "NONE", 0xDBB024, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketSpawnMob)
        {
            final SPacketSpawnMob packet = (SPacketSpawnMob) p_Event.getPacket();
            if (this.slimes.getValue())
            {
                final Minecraft mc = Minecraft.getMinecraft();

                if (packet.getEntityType() == 55 && packet.getY() <= 40 && !mc.world.getBiome(mc.player.getPosition()).getBiomeName().toLowerCase().contains("swamp"))
                {
                    final BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                    SalHack.SendMessage("Slime Spawned in chunk X:" + Math.round(mc.world.getChunk(pos).x) + " Z:" + Math.round(mc.world.getChunk(pos).z));
                }
            }

            if (this.Donkey.getValue() && packet.getEntityType() == 31)
            {
                SalHack.SendMessage(String.format("Donkey spawned at %s %s %s", Math.round(packet.getX()), Math.round(packet.getY()), Math.round(packet.getZ())));
            } else if (this.Llama.getValue() && packet.getEntityType() == 103)
            {
                SalHack.SendMessage(String.format("Llama spawned at %s %s %s", Math.round(packet.getX()), Math.round(packet.getY()), Math.round(packet.getZ())));
            } else if (this.Mule.getValue() && packet.getEntityType() == 32)
            {
                SalHack.SendMessage(String.format("Mule spawned at %s %s %s", Math.round(packet.getX()), Math.round(packet.getY()), Math.round(packet.getZ())));
            }
        }
        else if (p_Event.getPacket() instanceof SPacketSoundEffect)
        {
            final SPacketSoundEffect packet = (SPacketSoundEffect) p_Event.getPacket();
            if (this.thunder.getValue())
            {
                if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER)
                {
                    float yaw = 0;
                    final double difX = packet.getX() - Minecraft.getMinecraft().player.posX;
                    final double difZ = packet.getZ() - Minecraft.getMinecraft().player.posZ;

                    yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                    SalHack.SendMessage("Lightning spawned X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                }
            }
        }
        else if (p_Event.getPacket() instanceof SPacketEffect)
        {
            final SPacketEffect packet = (SPacketEffect) p_Event.getPacket();
            if (packet.getSoundType() == 1023 && Wither.getValue())
            {
                double theta = Math.atan2(packet.getSoundPos().getZ() - Minecraft.getMinecraft().player.posZ, packet.getSoundPos().getX() - Minecraft.getMinecraft().player.posX);
                theta += Math.PI / 2.0;
                double angle = Math.toDegrees(theta);
                if (angle < 0)
                    angle += 360;
                angle -= 180;

                SalHack.SendMessage("Wither spawned in direction " + angle + " with y position: " + packet.getSoundPos().getY());
                // salhack.INSTANCE.logChat("Wither spawned at " + packet.getSoundPos().toString() + "You currently are at: X:" + Minecraft.getMinecraft().player.posX + " Z:" +
                // Minecraft.getMinecraft().player.posZ);
            }
            else if (packet.getSoundType() == 1038 && EndPortal.getValue())
            {
                SalHack.SendMessage("End portal spawned at " + packet.getSoundPos().toString());
            }
            else if (packet.getSoundType() == 1028 && EnderDragon.getValue())
            {
                SalHack.SendMessage("Ender dragon died at " + packet.getSoundPos().toString());
            }
        }
    });
}
