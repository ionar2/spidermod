package com.github.lunatrius.core.client.gui.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

// TODO: remove this
@Deprecated
public abstract class GuiConfigSimple extends GuiConfig {
    public GuiConfigSimple(final GuiScreen guiScreen, final String modID, final Configuration configuration, final String categoryName) {
        super(guiScreen, getConfigElements(configuration, categoryName), modID, false, false, GuiConfig.getAbridgedConfigPath(configuration.toString()));
    }

    private static List<IConfigElement> getConfigElements(final Configuration configuration, final String categoryName) {
        final ConfigElement element = new ConfigElement(configuration.getCategory(categoryName));
        return element.getChildElements();
    }
}
