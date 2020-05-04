package me.ionar.salhack.util;

import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.util.entity.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class SalUtil
{
    public static EntityPlayer findClosestTarget() 
    {
    	if (Minecraft.getMinecraft().world.playerEntities.isEmpty())
    		return null;
    	
    	EntityPlayer closestTarget = null;
    	
        for (final EntityPlayer target : Minecraft.getMinecraft().world.playerEntities) 
        {
            if (target == Minecraft.getMinecraft().player)
                continue;
            
            if (FriendManager.Get().IsFriend(target))
                continue;
            
            if (!EntityUtil.isLiving((Entity)target))
                continue;
            
            if (target.getHealth() <= 0.0f)
                continue;

            if (closestTarget != null)
            	if (Minecraft.getMinecraft().player.getDistance(target) > Minecraft.getMinecraft().player.getDistance(closestTarget))
            		continue;

            closestTarget = target;
        }
        
        return closestTarget;
    }

    public Vec3d GetCenter(double posX, double posY, double posZ)
    {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D ;
        
        return new Vec3d(x, y, z);
    }
}
