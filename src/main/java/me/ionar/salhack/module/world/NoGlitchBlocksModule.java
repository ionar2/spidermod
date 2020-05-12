package me.ionar.salhack.module.world;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerDestroyBlock;
import me.ionar.salhack.events.render.EventRenderRainStrength;
import me.ionar.salhack.events.world.EventWorldSetBlockState;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketMultiBlockChange;

public class NoGlitchBlocksModule extends Module
{
    public final Value<Boolean> Destroy = new Value<Boolean>("Destroy", new String[]
            { "destroy" }, "Syncs Destroying", true);
    public final Value<Boolean> Place = new Value<Boolean>("Place", new String[]
            { "placement" }, "Syncs placement.", true);

    public NoGlitchBlocksModule()
    {
        super("NoGlitchBlocks", new String[]
                { "AntiGhostBlocks" }, "Synchronizes client and server communication by canceling clientside destroy/place for blocks", "NONE", -1, ModuleType.WORLD);
    }

    /*@EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketBlockChange)
        {
            SPacketBlockChange l_Packet = (SPacketBlockChange)p_Event.getPacket();

            SendMessage(String.format("%s %s", l_Packet.getBlockPosition().toString(), l_Packet.getBlockState().toString()));
        }
    });*/

    @EventHandler
    private Listener<EventPlayerDestroyBlock> OnPlayerDestroyBlock = new Listener<>(p_Event ->
    {
        if (!Destroy.getValue())
            return;
        // Wait for server to process this, and send back a packet later.
        p_Event.cancel();
    });

    @EventHandler
    private Listener<EventWorldSetBlockState> OnSetBlockState = new Listener<>(p_Event ->
    {
        if (!Place.getValue())
            return;
        /**
         * Flag 1 will cause a block update. Flag 2 will send the change to clients. Flag 4 will prevent the block from
         * being re-rendered, if this is a client world. Flag 8 will force any re-renders to run on the main thread instead
         * of the worker pool, if this is a client world and flag 4 is clear. Flag 16 will prevent observers from seeing
         * this change. Flags can be OR-ed
         */
        /// Flag 3 is from the packet
        if (p_Event.Flags != 3)
            p_Event.cancel();
    });
}
