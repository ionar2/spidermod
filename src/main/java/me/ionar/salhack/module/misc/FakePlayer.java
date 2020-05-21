package me.ionar.salhack.module.misc;



import me.ionar.salhack.module.Module;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.minecraft.client.entity.EntityOtherPlayerMP;

import net.minecraft.entity.Entity;

import net.minecraft.world.World;



public class FakePlayer extends Module {

    public FakePlayer() {
        super("FakePlayer", new String[]
                { "Fake" }, "Summons a fake player", "NONE", 0xDADB25, ModuleType.MISC);
    }



    public void onEnable() {

        if (mc.world == null)

            return;

        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP((World)mc.world, new GameProfile(UUID.fromString("a79203a2-2067-45cf-bd83-f1b3e67ba25a"), "IHackedXVIDEOs"));

        fakePlayer.copyLocationAndAnglesFrom((Entity)mc.player);

        fakePlayer.rotationYawHead = mc.player.rotationYawHead;

        mc.world.addEntityToWorld(-100, (Entity)fakePlayer);

    }



    public void onDisable() {

        mc.world.removeEntityFromWorld(-100);

    }

}
