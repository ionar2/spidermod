package me.ionar.salhack.util.render;

import java.util.regex.Pattern;

public enum ChatColor
{

    // A list of color codes
    BLACK('0'), DARK_BLUE('1'), DARK_GREEN('2'), DARK_AQUA('3'), DARK_RED('4'), DARK_PURPLE('5'), GOLD('6'), GRAY('7'), DARK_GRAY('8'), BLUE('9'), GREEN('a'), AQUA('b'), RED('c'), LIGHT_PURPLE('d'),
    YELLOW('e'), WHITE('f'), MAGIC('k', true), BOLD('l', true), STRIKETHROUGH('m', true), UNDERLINE('n', true), ITALIC('o', true), RESET('r');

    // The typical color character, compiles into '§'
    public static final char COLOR_CHAR = '\u00A7';

    // Color character
    private final char code;

    // Formatted
    private final boolean isFormat;

    // Color char + color code turned into a string
    private final String toString;

    /**
     * Set the character
     *
     * @param code the color character
     */
    ChatColor(char code)
    {
        this(code, false);
    }

    /**
     * Set the character and if it should be formatted
     *
     * @param code     color character
     * @param isFormat a formatted string
     */
    ChatColor(char code, boolean isFormat)
    {
        this.code = code;
        this.isFormat = isFormat;
        toString = new String(new char[]
        { COLOR_CHAR, code });
    }

    /**
     * Strip all of the color from text
     *
     * @param input text that's being stripped
     * @return the string without any color formatting
     */
    public static String stripColor(final String input)
    {
        return input == null ? null : Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]").matcher(input).replaceAll("");
    }

    /**
     * Translate '§' into another character, such as '&', '*', etc, for more readability
     *
     * @param altColorChar    the new color code character
     * @param textToTranslate the string to translate, could be '&cHyperium'
     * @return translated string
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate)
    {
        char[] b = textToTranslate.toCharArray();
        int bound = b.length - 1;
        for (int i = 0; i < bound; i++)
        {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1)
            {
                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public char getChar()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return toString;
    }

    public boolean isFormat()
    {
        return isFormat;
    }

    /**
     * Check if the string is colored
     *
     * @return true if it's colored
     */
    public boolean isColor()
    {
        return !isFormat && this != RESET;
    }
}
