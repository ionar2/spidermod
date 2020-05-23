package me.ionar.salhack.module.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import org.apache.commons.io.IOUtils;

public class FakePlayer extends Module
{
    public static final Value<String> name = new Value<>("Name", new String[] {"name"}, "Name of the fake player", "jared2013");

    public FakePlayer()
    {
        super("FakePlayer", new String[] {"Fake"}, "Summons a fake player", "NONE", 0xDADB25, ModuleType.MISC);
    }

    private EntityOtherPlayerMP _fakePlayer;

    @Override
    public void onEnable()
    {
        super.onEnable();
        _fakePlayer = null;

        if (mc.world == null)
        {
            this.toggle();
            return;
        }

        // If getting uuid from mojang doesn't work we use another uuid
        try
        {
            _fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString(getUuid(name.getValue())), name.getValue()));
        }
        catch (Exception e)
        {
            _fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("70ee432d-0a96-4137-a2c0-37cc9df67f03"), name.getValue()));
            SendMessage("Failed to load uuid, setting another one.");
        }
        SendMessage(String.format("%s has been spawned.", name.getValue()));

        _fakePlayer.copyLocationAndAnglesFrom(mc.player);
        _fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntityToWorld(-100, _fakePlayer);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.world.removeEntity(_fakePlayer);
    }

    @Override
    public void toggleNoSave()
    {
    }

    // Getting uuid from a name
    public static String getUuid(String name)
    {
        JsonParser parser = new JsonParser();
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try
        {
            String UUIDJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if(UUIDJson.isEmpty()) return "invalid name";
            JsonObject UUIDObject = (JsonObject) parser.parse(UUIDJson);
            return reformatUuid(UUIDObject.get("id").toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "error";
    }

    // Reformating a short uuid type into a long uuid type
    private static String reformatUuid(String uuid)
    {
        String longUuid = "";

        longUuid += uuid.substring(1, 9) + "-";
        longUuid += uuid.substring(9, 13) + "-";
        longUuid += uuid.substring(13, 17) + "-";
        longUuid += uuid.substring(17, 21) + "-";
        longUuid += uuid.substring(21, 33);

        return longUuid;
    }
}
