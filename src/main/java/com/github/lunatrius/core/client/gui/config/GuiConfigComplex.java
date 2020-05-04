package com.github.lunatrius.core.client.gui.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

// TODO: remove this
@Deprecated
public abstract class GuiConfigComplex extends GuiConfig {
    public GuiConfigComplex(final GuiScreen parent, final String modID, final Configuration configuration, final String langPrefix) {
        super(parent, getConfigElements(configuration, langPrefix), modID, false, false, GuiConfig.getAbridgedConfigPath(configuration.toString()));
    }

    private static List<IConfigElement> getConfigElements(final Configuration configuration, final String langPrefix) {
        final List<IConfigElement> elements = new ArrayList<IConfigElement>();
        for (final String name : configuration.getCategoryNames()) {
            final ConfigCategory category = configuration.getCategory(name).setLanguageKey(langPrefix + ".category." + name);
            if (category.parent == null) {
                elements.add(new ConfigElement(category));
            }
        }

        return elements;
    }
}
