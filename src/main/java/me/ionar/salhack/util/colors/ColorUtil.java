package me.ionar.salhack.util.colors;

import java.awt.Color;

public class ColorUtil
{
    public Color m_BaseColor;
    private float[] m_HSB;
    private float m_Alpha;

    public ColorUtil(final Color p_ColorBase)
    {
        super();
        m_BaseColor = p_ColorBase;
        m_HSB = GenerateHSB(p_ColorBase);
        m_Alpha = p_ColorBase.getAlpha() / 255.0f;
    }

    public ColorUtil(final float n, final float n2, final float n3)
    {
        this(n, n2, n3, 1.0f);
    }

    public ColorUtil(final float[] array)
    {
        this(array, 1.0f);
    }

    public ColorUtil(final float[] d, final float k)
    {
        super();
        this.m_HSB = d;
        this.m_Alpha = k;
        this.m_BaseColor = GetRainbowColorFromArray(d, k);
    }

    public ColorUtil(final float n, final float n2, final float n3, final float k)
    {
        super();
        final int n4 = 3;
        final float[] d = new float[n4];
        d[0] = n;
        d[1] = n2;
        d[2] = n3;
        this.m_HSB = d;
        this.m_Alpha = k;
        this.m_BaseColor = GetRainbowColorFromArray(this.m_HSB, k);
    }

    public String toString()
    {
        return new StringBuilder().insert(0, "HSLColor[h=").append(this.m_HSB[0]).append(",s=").append(this.m_HSB[1])
                .append(",l=").append(this.m_HSB[2]).append(",alpha=").append(this.m_Alpha).append("]").toString();
    }

    public Color GetColorWithLightnessMax(float max)
    {
        max = (100.0f - max) / 100.0f;
        max = Math.max(0.0f, this.m_HSB[2] * max);
        return GetRainbowColor(this.m_HSB[0], this.m_HSB[1], max, this.m_Alpha);
    }

    public Color GetColorWithLightnessMin(float min)
    {
        min = (100.0f + min) / 100.0f;
        min = Math.min(100.0f, this.m_HSB[2] * min);
        return GetRainbowColor(this.m_HSB[0], this.m_HSB[1], min, this.m_Alpha);
    }

    public float GetAlpha()
    {
        return m_Alpha;
    }

    public Color GetColorWithBrightness(final float p_Brightness)
    {
        return GetRainbowColor(this.m_HSB[0], this.m_HSB[1], p_Brightness, this.m_Alpha);
    }

    public float GetHue()
    {
        return m_HSB[0];
    }

    public float GetSaturation()
    {
        return m_HSB[1];
    }
    
    public float GetLightness()
    {
        return this.m_HSB[2];
    }

    public Color GetLocalColor()
    {
        return this.m_BaseColor;
    }

    public Color GetColorWithHue(final float p_Hue)
    {
        return GetRainbowColor(p_Hue, this.m_HSB[1], this.m_HSB[2], this.m_Alpha);
    }

    public Color GetColorWithSaturation(final float p_Saturation)
    {
        return GetRainbowColor(this.m_HSB[0], p_Saturation, this.m_HSB[2], this.m_Alpha);
    }

    public static float[] GenerateHSB(final Color color)
    {
        final float[] rgbColorComponents = color.getRGBColorComponents(null);
        final float n = rgbColorComponents[0];
        final float n2 = rgbColorComponents[1];
        final float n3 = rgbColorComponents[2];
        final float min = Math.min(n, Math.min(n2, n3));
        final float max = Math.max(n, Math.max(n2, n3));
        float n4 = 0.0f;
        float n5;
        if (max == min)
        {
            n4 = 0.0f;
            n5 = max;
        }
        else if (max == n)
        {
            n4 = (60.0f * (n2 - n3) / (max - min) + 360.0f) % 360.0f;
            n5 = max;
        }
        else if (max == n2)
        {
            n4 = 60.0f * (n3 - n) / (max - min) + 120.0f;
            n5 = max;
        }
        else
        {
            if (max == n3)
            {
                n4 = 60.0f * (n - n2) / (max - min) + 240.0f;
            }
            n5 = max;
        }
        final float n6 = (n5 + min) / 2.0f;
        float n7;
        if (max == min)
        {
            n7 = 0.0f;
        }
        else
        {
            final float n8 = Math.min(n6, 0.5f); //maybe max?
            final float n9 = max;
            if (n8 <= 0)
            {
                n7 = (n9 - min) / (max + min);
            }
            else
            {
                n7 = (n9 - min) / (2.0f - max - min);
            }
        }
        return new float[]
        { n4, n7 * 100.0f, n6 * 100.0f };
    }

    public Color GetColorWithModifiedHue()
    {
        return ColorRainbowWithDefaultAlpha((this.m_HSB[0] + 180.0f) % 360.0f, this.m_HSB[1], this.m_HSB[2]);
    }

    public static Color GetRainbowColorFromArray(final float[] p_HSB, final float p_Alpha)
    {
        return GetRainbowColor(p_HSB[0], p_HSB[1], p_HSB[2], p_Alpha);
    }

    public static Color GetColorWithHSBArray(final float[] HSB)
    {
        return GetRainbowColorFromArray(HSB, 1.0f);
    }

    public static String GenerateMCColorString(String p_String)
    {
        final char c = 'q';
        final char c2 = '\u0018';
        final int length = p_String.length();
        final char[] array = new char[length];
        int n;
        int i = n = length - 1;
        final char[] array2 = array;
        final char c3 = c2;
        final char c4 = c;
        while (i >= 0)
        {
            final char[] array3 = array2;
            final int n2 = n;
            final char char1 = p_String.charAt(n2);
            --n;
            array3[n2] = (char) (char1 ^ c4);
            if (n < 0)
            {
                break;
            }
            final char[] array4 = array2;
            final int n3 = n--;
            array4[n3] = (char) (p_String.charAt(n3) ^ c3);
            i = n;
        }
        return new String(array2);
    }

    private static float FutureClientColorCalculation(final float n, final float n2, float n3)
    {
        if (n3 < 0.0f)
        {
            ++n3;
        }
        if (n3 > 1.0f)
        {
            --n3;
        }
        if (6.0f * n3 < 1.0f)
        {
            return n + (n2 - n) * 6.0f * n3;
        }
        if (2.0f * n3 < 1.0f)
        {
            return n2;
        }
        if (3.0f * n3 < 2.0f)
        {
            return n + (n2 - n) * 6.0f * (0.6666667f - n3);
        }
        return n;
    }

    public static Color ColorRainbowWithDefaultAlpha(final float n, final float n2, final float n3)
    {
        return GetRainbowColor(n, n2, n3, 1.0f);
    }

    public static Color GetRainbowColor(float p_Hue, float p_Saturation, float p_Lightness, final float p_Alpha)
    {
        if (p_Saturation < 0.0f || p_Saturation > 100.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Saturation");
        }
        if (p_Lightness < 0.0f || p_Lightness > 100.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Lightness");
        }
        if (p_Alpha < 0.0f || p_Alpha > 1.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Alpha");
        }
        p_Hue = (p_Hue %= 360.0f) / 360.0f;
        p_Saturation /= 100.0f;
        p_Lightness /= 100.0f;
        float n5;
        if (p_Lightness < 0.0)
        {
            n5 = p_Lightness * (1.0f + p_Saturation);
        }
        else
        {
            n5 = p_Lightness + p_Saturation - p_Saturation * p_Lightness;
        }
        p_Saturation = 2.0f * p_Lightness - n5;
        p_Lightness = Math.max(0.0f, FutureClientColorCalculation(p_Saturation, n5, p_Hue + 0.33333334f));
        final float max = Math.max(0.0f, FutureClientColorCalculation(p_Saturation, n5, p_Hue));
        p_Saturation = Math.max(0.0f, FutureClientColorCalculation(p_Saturation, n5, p_Hue - 0.33333334f));
        p_Lightness = Math.min(p_Lightness, 1.0f);
        final float min = Math.min(max, 1.0f);
        p_Saturation = Math.min(p_Saturation, 1.0f);
        return new Color(p_Lightness, min, p_Saturation, p_Alpha);
    }

}
