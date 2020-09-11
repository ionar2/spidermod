package me.ionar.salhack.module.combat;

import me.ionar.salhack.module.Module;

public final class FastFall extends Module {

    public FastFall()
    {
        super("FastFall", new String[] {"FastFall"}, "Makes you fall faster to get into holes easier", "NONE", 0x24CADB, ModuleType.COMBAT);
    }
    public void onUpdate() {
        if (mc.player.onGround)
            --mc.player.motionY;
    }
}
