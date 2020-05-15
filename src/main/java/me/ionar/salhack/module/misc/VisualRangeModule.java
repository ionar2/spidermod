package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class VisualRangeModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "Mode of notifying to use", Modes.Both);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[]
            { "Friend" }, "Notifies if a friend comes in range", true);
    public final Value<Boolean> Enter = new Value<Boolean>("Enter", new String[]
            { "Enters" }, "Notifies when the entity enters range", true);
    public final Value<Boolean> Leave = new Value<Boolean>("Leave", new String[]
            { "Leaves" }, "Notifies when the entity leaves range", true);

    private enum Modes
    {
        Chat,
        Notification,
        Both,
    }

    public VisualRangeModule()
    {
        super("VisualRange", new String[]
                { "VR" }, "Notifies you when one enters or leaves your visual range.", "NONE", -1, Module.ModuleType.MISC);
    }

    @Override
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }

    @EventHandler
    private Listener<EventEntityAdded> OnEntityAdded = new Listener<>(p_Event ->
    {
        if (!Enter.getValue())
            return;

        if (!VerifyEntity(p_Event.GetEntity()))
            return;

        Notify(String.format("%s has entered your visual range.", p_Event.GetEntity().getName()));
    });

    @EventHandler
    private Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(p_Event ->
    {
        if (!Leave.getValue())
            return;

        if (!VerifyEntity(p_Event.GetEntity()))
            return;

        Notify(String.format("%s has left your visual range.", p_Event.GetEntity().getName()));
    });

    private boolean VerifyEntity(Entity p_Entity)
    {
        if (!(p_Entity instanceof EntityPlayer))
            return false;

        if (p_Entity == mc.player)
            return false;

        if (!Friends.getValue() && FriendManager.Get().IsFriend(p_Entity))
            return false;

        return true;
    }

    private void Notify(String p_Msg)
    {
        switch (Mode.getValue())
        {
            case Chat:
                SendMessage(p_Msg);
                break;
            case Notification:
                NotificationManager.Get().AddNotification("VisualRange", p_Msg);
                break;
            case Both:
                SendMessage(p_Msg);
                NotificationManager.Get().AddNotification("VisualRange", p_Msg);
                break;
        }
    }
}
