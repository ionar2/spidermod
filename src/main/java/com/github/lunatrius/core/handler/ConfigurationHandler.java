package com.github.lunatrius.core.handler;

import com.github.lunatrius.core.reference.Names;
import com.github.lunatrius.core.reference.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigurationHandler {
    public static Configuration configuration;

    @Config(modid = Reference.MODID, category = Names.Config.Category.VERSION_CHECK)
    public static class VersionCheck {
        @RequiresMcRestart
        @Comment(Names.Config.CHECK_FOR_UPDATES_DESC)
        public static boolean checkForUpdates = true;
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(Reference.MODID)) {
            ConfigManager.sync(Reference.MODID, Config.Type.INSTANCE);
        }
    }
}
