package me.ionar.salhack.events.render;

import com.google.common.base.Predicate;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class EventRenderGetEntitiesINAABBexcluding extends MinecraftEvent
{

    public EventRenderGetEntitiesINAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate)
    {
        // TODO Auto-generated constructor stub
    }

}
