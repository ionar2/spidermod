package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.*;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class AutoMountModule extends Module
{
    public final Value<Boolean> Boats = new Value<Boolean>("Boats", new String[] {"Boat"}, "Mounts boats", true);
    public final Value<Boolean> Horses = new Value<Boolean>("Horses", new String[] {"Horse"}, "Mounts Horses", true);
    public final Value<Boolean> SkeletonHorses = new Value<Boolean>("SkeletonHorses", new String[] {"SkeletonHorse"}, "Mounts SkeletonHorses", true);
    public final Value<Boolean> Donkeys = new Value<Boolean>("Donkeys", new String[] {"Donkey"}, "Mounts Donkeys", true);
    public final Value<Boolean> Pigs = new Value<Boolean>("Pigs", new String[] {"Pig"}, "Mounts Pigs", true);
    public final Value<Boolean> Llamas = new Value<Boolean>("Llamas", new String[] {"Llama"}, "Mounts Llamas", true);
    public final Value<Integer> Range = new Value<Integer>("Range", new String[] {"R"}, "Range to search for mountable entities", 4, 0, 10, 1);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);

    public AutoMountModule()
    {
        super("AutoMount", new String[] {""}, "Automatically attempts to mount an entity near you", "NONE", -1, ModuleType.MISC);
    }

    private Timer timer = new Timer();

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.player.isRiding())
            return;

        if (!timer.passed(Delay.getValue() * 1000))
            return;

        timer.reset();

        Entity l_Entity = mc.world.loadedEntityList.stream()
                .filter(p_Entity -> isValidEntity(p_Entity))
                .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
                .orElse(null);

        if (l_Entity != null)
            mc.playerController.interactWithEntity(mc.player, l_Entity, EnumHand.MAIN_HAND);
    });

    private boolean isValidEntity(Entity entity)
    {
        if (entity.getDistance(mc.player) > Range.getValue())
            return false;

        if (entity instanceof AbstractHorse)
        {
            AbstractHorse horse = (AbstractHorse) entity;

            if (horse.isChild())
                return false;
        }

        if (entity instanceof EntityBoat && Boats.getValue())
            return true;

        if (entity instanceof EntitySkeletonHorse && SkeletonHorses.getValue())
            return true;

        if (entity instanceof EntityHorse && Horses.getValue())
            return true;

        if (entity instanceof EntityDonkey && Donkeys.getValue())
            return true;

        if (entity instanceof EntityPig && Pigs.getValue())
        {
            EntityPig pig = (EntityPig) entity;

            if (pig.getSaddled())
                return true;

            return false;
        }

        if (entity instanceof EntityLlama && Llamas.getValue())
        {
            EntityLlama l_Llama = (EntityLlama) entity;

            if (!l_Llama.isChild())
                return true;
        }

        return false;
    }
}
