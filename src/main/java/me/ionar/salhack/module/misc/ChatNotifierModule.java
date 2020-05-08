package me.ionar.salhack.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.salhack.EventSalHackModuleDisable;
import me.ionar.salhack.events.salhack.EventSalHackModuleEnable;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public class ChatNotifierModule extends Module
{
    public ChatNotifierModule()
    {
        super("ChatNotifier", new String[]
        { "" }, "Notifiys you in chat and notification system when a mod is enabled/disabled", "NONE", -1,
                ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventSalHackModuleEnable> OnModEnable = new Listener<>(p_Event ->
    {
        String l_Msg = String.format("%s was enabled.",
                ChatFormatting.GREEN + p_Event.Mod.getDisplayName() + ChatFormatting.AQUA);

        SendMessage(l_Msg);
        NotificationManager.Get().AddNotification("ChatNotifier", l_Msg);
    });

    @EventHandler
    private Listener<EventSalHackModuleDisable> OnModDisable = new Listener<>(p_Event ->
    {
        String l_Msg = String.format("%s was disabled.",
                ChatFormatting.RED + p_Event.Mod.getDisplayName() + ChatFormatting.AQUA);

        SendMessage(l_Msg);
        NotificationManager.Get().AddNotification("ChatNotifier", l_Msg);
    });
}
