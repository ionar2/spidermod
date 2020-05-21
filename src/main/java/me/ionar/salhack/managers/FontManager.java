package me.ionar.salhack.managers;

import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.util.render.SalFontRenderer;

public class FontManager
{
    public SalFontRenderer[] FontRenderers = new SalFontRenderer[25];
    
    public SalFontRenderer TWCenMt18 = null;
    public SalFontRenderer TwCenMtStd28 = null;
    public SalFontRenderer VerdanaBold = null;
    
    public FontManager()
    {
    }
    
    public void Load()
    {
        TWCenMt18 = new SalFontRenderer("Tw Cen MT", 18);
        TwCenMtStd28 = new SalFontRenderer("Tw Cen MT Std", 28.14f);
        VerdanaBold = new SalFontRenderer("VerdanaBold", 20f);
        
        for (int l_I = 0; l_I < FontRenderers.length; ++l_I)
            FontRenderers[l_I] = new SalFontRenderer("Tw Cen MT", l_I);
    }
    
    public void LoadCustomFont(String customFont)
    {
        for (int l_I = 0; l_I < FontRenderers.length; ++l_I)
            FontRenderers[l_I] = new SalFontRenderer(customFont, l_I);
    }
    
    public SalFontRenderer GetFontBySize(int p_Size)
    {
        if (p_Size > FontRenderers.length)
            p_Size = FontRenderers.length-1;
        
        return FontRenderers[p_Size];
    }

    public float DrawStringWithShadow(String p_Name, float p_X, float p_Y, int p_Color)
    {
        return FontRenderers[22].drawStringWithShadow(p_Name, p_X, p_Y, p_Color);
    }

    public float GetStringHeight(String p_Name)
    {
        return FontRenderers[22].getStringHeight(p_Name);
    }

    public float GetStringWidth(String p_Name)
    {
        return FontRenderers[22].getStringWidth(p_Name);
    }

    public static FontManager Get()
    {
        return SalHack.GetFontManager();
    }
}
