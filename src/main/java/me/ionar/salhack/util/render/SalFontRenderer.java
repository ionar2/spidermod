package me.ionar.salhack.util.render;

/// Credit: https://github.com/HyperiumClient/Hyperium/blob/mcgradle/src/main/java/cc/hyperium/utils/HyperiumFontRenderer.java

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import me.ionar.salhack.managers.DirectoryManager;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SalFontRenderer
{
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile(ChatColor.COLOR_CHAR+"[0123456789abcdefklmnor]");
    public final int FONT_HEIGHT = 9;
    private final int[] colorCodes =
    { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };
    private final Map<String, Float> cachedStringWidth = new HashMap<>();
    private float antiAliasingFactor;
    private UnicodeFont unicodeFont;
    private int prevScaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
    private String name;
    private float size;

    public SalFontRenderer(String fontName, float fontSize)
    {
        name = fontName;
        size = fontSize;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        try
        {
            prevScaleFactor = resolution.getScaleFactor();
            unicodeFont = new UnicodeFont(getFontByName(fontName).deriveFont(fontSize * prevScaleFactor / 2));
            unicodeFont.addAsciiGlyphs();
            unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
            unicodeFont.loadGlyphs();
        }
        catch (FontFormatException | IOException | SlickException e)
        {
            e.printStackTrace();
            
            prevScaleFactor = resolution.getScaleFactor();
            try
            {
                unicodeFont = new UnicodeFont(getFontByName("Tw Cen MT").deriveFont(fontSize * prevScaleFactor / 2));
                unicodeFont.addAsciiGlyphs();
                unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
                unicodeFont.loadGlyphs();
            } catch (Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        this.antiAliasingFactor = resolution.getScaleFactor();
    }

    public SalFontRenderer(Font font)
    {
        this(font.getFontName(), font.getSize());
    }

    public SalFontRenderer(String fontName, int fontType, int size)
    {
        this(new Font(fontName, fontType, size));
    }

    public static Font getFontByName(String name) throws IOException, FontFormatException
    {
        if (name == "Tw Cen MT")
            return getFontFromInput("/assets/salhack/fonts/tcm.TTF");
        if (name == "Tw Cen MT Std")
            return getFontFromInput("/assets/salhack/fonts/TwCenMTStd-Bold.ttf");
        if (name == "Verdana Bold")
            return getFontFromInput("/assets/salhack/fonts/verdanabold.ttf");
        if (name == "Lucida Console")
            return getFontFromInput("/assets/salhack/fonts/lucdia-console.ttf");
        if (name == "VerdanaBold")
            return getFontFromInput("/assets/salhack/fonts/verdanabold.ttf");
        
        // Attempt to find custom fonts
        return Font.createFont(Font.TRUETYPE_FONT, new File(DirectoryManager.Get().GetCurrentDirectory() + "\\SalHack\\Fonts\\" + name + ".ttf"));
    }
    
    static Font font = null;

    public static Font getFontFromInput(String path) throws IOException, FontFormatException
    {
        Font newFont = Font.createFont(Font.TRUETYPE_FONT, SalFontRenderer.class.getResourceAsStream(path));
        
        if (newFont != null)
            font = newFont;
        
        return font;
    }

    public void drawStringScaled(String text, int givenX, int givenY, int color, double givenScale)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        drawString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public int drawString(String text, float x, float y, int color)
    {
        if (text == null)
            return 0;

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        try
        {
            if (resolution.getScaleFactor() != prevScaleFactor)
            {
                prevScaleFactor = resolution.getScaleFactor();
                unicodeFont = new UnicodeFont(getFontByName(name).deriveFont(size * prevScaleFactor / 2));
                unicodeFont.addAsciiGlyphs();
                unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
                unicodeFont.loadGlyphs();
            }
        }
        catch (FontFormatException | IOException | SlickException e)
        {
            e.printStackTrace();
        }

        this.antiAliasingFactor = resolution.getScaleFactor();

        GL11.glPushMatrix();
        GlStateManager.scale(1 / antiAliasingFactor, 1 / antiAliasingFactor, 1 / antiAliasingFactor);
        x *= antiAliasingFactor;
        y *= antiAliasingFactor;
        float originalX = x;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);

        int currentColor = color;

        char[] characters = text.toCharArray();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        String[] parts = COLOR_CODE_PATTERN.split(text);
        int index = 0;
        for (String s : parts)
        {
            for (String s2 : s.split("\n"))
            {
                for (String s3 : s2.split("\r"))
                {

                    unicodeFont.drawString(x, y, s3, new org.newdawn.slick.Color(currentColor));
                    x += unicodeFont.getWidth(s3);

                    index += s3.length();
                    if (index < characters.length && characters[index] == '\r')
                    {
                        x = originalX;
                        index++;
                    }
                }
                if (index < characters.length && characters[index] == '\n')
                {
                    x = originalX;
                    y += getHeight(s2) * 2;
                    index++;
                }
            }
            if (index < characters.length)
            {
                char colorCode = characters[index];
                if (colorCode == ChatColor.COLOR_CHAR)
                {
                    char colorChar = characters[index + 1];
                    int codeIndex = ("0123456789" + "abcdef").indexOf(colorChar);
                    if (codeIndex < 0)
                    {
                        if (colorChar == 'r')
                        {
                            currentColor = color;
                        }
                    }
                    else
                    {
                        currentColor = colorCodes[codeIndex];
                    }
                    index += 2;
                }
            }
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.bindTexture(0);
        GlStateManager.popMatrix();
        return (int) getWidth(text);
    }

    public int drawStringWithShadow(String text, float x, float y, int color)
    {
        if (text == null || text == "")
            return 0;

        drawString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, 0x000000);
        return drawString(text, x, y, color);
    }

    public void drawCenteredString(String text, float x, float y, int color)
    {
        drawString(text, x - ((int) getWidth(text) >> 1), y, color);
    }

    /**
     * Draw Centered Text Scaled
     *
     * @param text       - Given Text String
     * @param givenX     - Given X Position
     * @param givenY     - Given Y Position
     * @param color      - Given Color (HEX)
     * @param givenScale - Given Scale
     */
    public void drawCenteredTextScaled(String text, int givenX, int givenY, int color, double givenScale)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        drawCenteredString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, int color)
    {
        drawCenteredString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, color);
        drawCenteredString(text, x, y, color);
    }

    public float getWidth(String text)
    {
        if (cachedStringWidth.size() > 1000)
            cachedStringWidth.clear();
        return cachedStringWidth.computeIfAbsent(text, e -> unicodeFont.getWidth(ChatColor.stripColor(text)) / antiAliasingFactor);
    }

    public float getCharWidth(char c)
    {
        return unicodeFont.getWidth(String.valueOf(c));
    }

    public float getHeight(String s)
    {
        return unicodeFont.getHeight(s) / 2.0F;
    }

    public UnicodeFont getFont()
    {
        return unicodeFont;
    }

    public void drawSplitString(ArrayList<String> lines, int x, int y, int color)
    {
        drawString(String.join("\n\r", lines), x, y, color);
    }

    public List<String> splitString(String text, int wrapWidth)
    {
        List<String> lines = new ArrayList<>();

        String[] splitText = text.split(" ");
        StringBuilder currentString = new StringBuilder();

        for (String word : splitText)
        {
            String potential = currentString + " " + word;

            if (getWidth(potential) >= wrapWidth)
            {
                lines.add(currentString.toString());
                currentString = new StringBuilder();
            }

            currentString.append(word).append(" ");
        }

        lines.add(currentString.toString());
        return lines;
    }

    public float getStringWidth(String p_Name)
    {   
        return unicodeFont.getWidth(ChatColor.stripColor(p_Name)) / 2;
    }

    public float getStringHeight(String p_Name)
    {
        return getHeight(p_Name);
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width)
    {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse)
    {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k)
        {
            char c0 = text.charAt(l);
            float i1 = this.getWidth(text);

            if (flag)
            {
                flag = false;

                if (c0 != 'l' && c0 != 'L')
                {
                    if (c0 == 'r' || c0 == 'R')
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (i1 < 0)
            {
                flag = true;
            }
            else
            {
                i += i1;

                if (flag1)
                {
                    ++i;
                }
            }

            if (i > width)
            {
                break;
            }

            if (reverse)
            {
                stringbuilder.insert(0, c0);
            }
            else
            {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }
}
