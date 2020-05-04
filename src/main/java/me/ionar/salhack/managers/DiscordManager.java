package me.ionar.salhack.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.ionar.salhack.main.SalHack;

public class DiscordManager
{
    public DiscordManager Get()
    {
        return SalHack.GetDiscordManager();
    }
    
    private String Description = "";
    
    public void Start()
    {
        Description = "ItsGoingGood | 2b2t.org | X:69 Y:120 Z:319 | Hell";
        
       /* DiscordRPC lib = DiscordRPC.INSTANCE;
        String applicationId = "701991938206990397";
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Ready!");
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000; // epoch second
        presence.details = Description;
        lib.Discord_UpdatePresence(presence);
        // in a worker thread
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                presence.details = Description;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }, "RPC-Callback-Handler").start();*/
    }
    
    public void SetDescription(String p_Description)
    {
        Description = p_Description;
    }
}
