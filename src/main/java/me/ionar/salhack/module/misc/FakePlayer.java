package me.ionar.salhack.module.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.net.URL;
import java.util.UUID;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.apache.commons.io.IOUtils;

public class FakePlayer extends Module {
    public FakePlayer() {
        super("FakePlayer", new String[]
                { "Fake" }, "Summons a fake player", "NONE", 0xDADB25, ModuleType.MISC);
    }

    public final Value<String> name = new Value<>("Name", new String[] {"name"}, "Name of the fake player", "IHackedXVIDEOs");

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.world == null)
            return;

        String uuid = getUuid(name.getValue());
        EntityOtherPlayerMP fakePlayer;

        //If get uuid from mojang don't work we use another uuid
        try{
            fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString(uuid), name.getValue()));
        }catch (Exception e){
            fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("a79203a2-2067-45cf-bd83-f1b3e67ba25a"), name.getValue()));
            SendMessage("Failed to load uuid, setting another one.");
        }
        SendMessage(String.format("%s has been spawned.", name.getValue()));

        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntityToWorld(-100, fakePlayer);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.world.removeEntityFromWorld(-100);
    }

    //Getting uuid from a name
    public String getUuid(String name) {
        JsonParser parser = new JsonParser();
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            @SuppressWarnings("deprecation")
            String UUIDJson = IOUtils.toString(new URL(url));
            if(UUIDJson.isEmpty()) return "invalid name";
            JsonObject UUIDObject = (JsonObject) parser.parse(UUIDJson);
            return reformatUuid(UUIDObject.get("id").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    //Reformating a short uuid type into a long uuid type
    private static String reformatUuid(String uuid){
        String longUuid = "";

        longUuid += uuid.substring(1, 9) + "-";
        longUuid += uuid.substring(9, 13) + "-";
        longUuid += uuid.substring(13, 17) + "-";
        longUuid += uuid.substring(17, 21) + "-";
        longUuid += uuid.substring(21, 33);

        return longUuid;
    }
}
