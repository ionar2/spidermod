package me.ionar.salhack.module.misc;

import org.lwjgl.input.Mouse;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;

public class MiddleClickFriendsModule extends Module
{
    public MiddleClickFriendsModule()
    {
        super("MiddleClick", new String[] {"MCF", "MiddleClickF"}, "Middle click friends", "NONE", -1, ModuleType.MISC);
    }
    
    private boolean Clicked = false;

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen != null)
            return;
        
        if (!Mouse.isButtonDown(2))
        {
            Clicked = false;
            return;
        }
        
        if (!Clicked)
        {
            Clicked = true;
            
            final RayTraceResult l_Result = mc.objectMouseOver;
            
            if (l_Result == null || l_Result.typeOfHit != RayTraceResult.Type.ENTITY)
                return;
            
            Entity l_Entity = l_Result.entityHit;
            
            if (l_Entity == null || !(l_Entity instanceof EntityPlayer))
                return;
            
            if (FriendManager.Get().IsFriend(l_Entity))
            {
                FriendManager.Get().RemoveFriend(l_Entity.getName().toLowerCase());
                SendMessage(String.format("%s has been removed.", l_Entity.getName()));
            }
            else
            {
                FriendManager.Get().AddFriend(l_Entity.getName().toLowerCase());
                SendMessage(String.format("%s has been added.", l_Entity.getName()));
            }
        }
    });
}
