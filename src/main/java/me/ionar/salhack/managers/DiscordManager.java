package me.ionar.salhack.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;

public class DiscordManager
{
    public DiscordManager Get()
    {
        return SalHack.GetDiscordManager();
    }

    public void Start()
    {
        DiscordRPC lib = DiscordRPC.INSTANCE;
        String applicationId = "701991938206990397";
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Ready!");
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000; // epoch second
        lib.Discord_UpdatePresence(presence);
        // in a worker thread
        new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted())
            {
                lib.Discord_RunCallbacks();
                presence.details = String.format("%s | %s | %s", Wrapper.GetMC().getSession().getUsername(), Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "none", "SPW on Top!");
                presence.state = "Is this a Brix Reference?";
                lib.Discord_UpdatePresence(presence);
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored)
                {
                }
            }
        }, "RPC-Callback-Handler").start();
    }
}
