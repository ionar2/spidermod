package com.github.lunatrius.core.client.gui;

import net.minecraft.client.gui.FontRenderer;

public class FontRendererHelper {
    public static void drawLeftAlignedString(final FontRenderer fontRenderer, final String str, final int x, final int y, final int color) {
        fontRenderer.drawStringWithShadow(str, x, y, color);
    }

    public static void drawCenteredString(final FontRenderer fontRenderer, final String str, final int x, final int y, final int color) {
        fontRenderer.drawStringWithShadow(str, x - fontRenderer.getStringWidth(str) / 2, y, color);
    }

    public static void drawRightAlignedString(final FontRenderer fontRenderer, final String str, final int x, final int y, final int color) {
        fontRenderer.drawStringWithShadow(str, x - fontRenderer.getStringWidth(str), y, color);
    }

}
