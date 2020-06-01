package me.ionar.salhack.module.render;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ValueListeners;
import me.ionar.salhack.util.entity.EntityUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;

public class Chams extends Module {
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]{"Players"}, "Renders Players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[]{"Monsters"}, "Renders Monsters", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]{"Animals"}, "Renders Animals", false);

    public Chams()
    {
        super("CM", new String[]{"Chams"}, "Renders the entities through walls", "NONE", 0xDADB24, ModuleType.RENDER);
    }
        public final boolean renderChams (Entity entity)
        {
            return (entity instanceof EntityPlayer ? Players.getValue() : (EntityUtil.isPassive(entity) ? Animals.getValue() : Monsters.getValue()));
        }
    }
