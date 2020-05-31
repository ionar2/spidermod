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

import static me.ionar.salhack.util.render.ESPUtil.*;

public class Chams extends Module {
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]{"Players"}, "Renders Players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[]{"Mobs"}, "Renders Mobs", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]{"Animals"}, "Renders Animals", false);

    public Chams() {
        super("Chams", new String[]{""}, "Renders Entities through walls (WIP) ", "NONE", -1, ModuleType.RENDER);
    }
}


