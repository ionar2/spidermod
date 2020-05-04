package me.ionar.salhack.module.misc;

import java.util.Comparator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

public class AutoDyeModule extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for sheep", 4, 0, 10, 1);
    
    public AutoDyeModule()
    {
        super("AutoDye", new String[] {""}, "Dyes sheep in range, if they are not same color as dye in hand.", "NONE", -1, ModuleType.MISC);
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemDye))
            return;

        EnumDyeColor l_Color = EnumDyeColor.byDyeDamage(mc.player.getHeldItemMainhand().getMetadata());
        
        EntitySheep l_Sheep = mc.world.loadedEntityList.stream()
                .filter(p_Entity -> IsValidSheep(p_Entity, l_Color))
                .map(p_Entity -> (EntitySheep) p_Entity)
                .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                .orElse(null);
        
        if (l_Sheep != null)
        {
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_Sheep.posX,
                    l_Sheep.posY,
                    l_Sheep.posZ,
                    mc.player);
            
            mc.player.rotationYawHead = (float) l_Pos[0];
            
            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);
            
            mc.getConnection().sendPacket(new CPacketUseEntity(l_Sheep, EnumHand.MAIN_HAND));
        }
    });
    
    private boolean IsValidSheep(Entity p_Entity, EnumDyeColor p_Color)
    {
        if (p_Entity.getDistance(mc.player) > Radius.getValue())
            return false;
        
        if (!(p_Entity instanceof EntitySheep))
            return false;
        
        EntitySheep l_Sheep = (EntitySheep)p_Entity;
        
        if (l_Sheep.getFleeceColor() == p_Color)
            return false;
        
        return true;
    }
}
