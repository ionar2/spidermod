package me.ionar.salhack.module.world;

import java.util.Comparator;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

public class AutoNameTagModule extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] {"R"}, "Radius to search for entities", 4, 0, 10, 1);
    public final Value<Boolean> ReplaceOldNames = new Value<Boolean>("ReplaceOldNames", new String [] {""}, "Automatically replaces old names of the mobs if a previous nametag was used", true);
    public final Value<Boolean> AutoSwitch = new Value<Boolean>("AutoSwitch", new String [] {""}, "Automatically switches to a nametag in your hotbar", false);
    public final Value<Boolean> WithersOnly = new Value<Boolean>("WithersOnly", new String [] {""}, "Only renames withers", true);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);
    
    public AutoNameTagModule()
    {
        super("AutoNameTag", new String[] {""}, "Automatically name tags entities in range, if they meet the requirements.", "NONE", -1, ModuleType.MISC);
    }

    private Timer timer = new Timer();
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.currentScreen != null)
            return;
        
        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemNameTag))
        {
            int l_Slot = -1;

            if (AutoSwitch.getValue())
            {
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                    
                    if (l_Stack.isEmpty())
                        continue;
                    
                    if (l_Stack.getItem() instanceof ItemNameTag)
                    {
                        if (!l_Stack.hasDisplayName())
                            continue;
                        
                        l_Slot = l_I;
                        mc.player.inventory.currentItem = l_Slot;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }
            
            if (l_Slot == -1)
                return;
        }
        
        ItemStack l_Stack = mc.player.getHeldItemMainhand();
        
        if (!l_Stack.hasDisplayName())
            return;

        EntityLivingBase l_Entity = mc.world.loadedEntityList.stream()
                .filter(p_Entity -> IsValidEntity(p_Entity, l_Stack.getDisplayName()))
                .map(p_Entity -> (EntityLivingBase) p_Entity)
                .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                .orElse(null);
        
        if (l_Entity != null)
        {
            timer.reset();
            p_Event.cancel();

            final double l_Pos[] =  EntityUtil.calculateLookAt(
                    l_Entity.posX,
                    l_Entity.posY,
                    l_Entity.posZ,
                    mc.player);
            
            SendMessage(String.format("Gave %s the nametag of %s", l_Entity.getName(), l_Stack.getDisplayName()));
            
            mc.player.rotationYawHead = (float) l_Pos[0];
            
            PlayerUtil.PacketFacePitchAndYaw((float)l_Pos[1], (float)l_Pos[0]);
            
            mc.getConnection().sendPacket(new CPacketUseEntity(l_Entity, EnumHand.MAIN_HAND));
        }
    });
    
    private boolean IsValidEntity(Entity p_Entity, final String p_Name)
    {
        if (!(p_Entity instanceof EntityLivingBase))
            return false;
        
        if (p_Entity.getDistance(mc.player) > Radius.getValue())
            return false;
        
        if (p_Entity instanceof EntityPlayer)
            return false;
        
        if (!p_Entity.getCustomNameTag().isEmpty() && !ReplaceOldNames.getValue())
            return false;
        
        if (ReplaceOldNames.getValue())
        {
            if (!p_Entity.getCustomNameTag().isEmpty() && p_Entity.getName().equals(p_Name))
                return false;
        }
        
        if (WithersOnly.getValue() && !(p_Entity instanceof EntityWither))
            return false;
        
        return true;
    }
}
