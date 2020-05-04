package me.ionar.salhack.managers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.util.CharsTrie.Iterator;

import akka.japi.Pair;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class ImageManager
{
    public NavigableMap<String, SalDynamicTexture> Pictures = new TreeMap<String, SalDynamicTexture>();

    public ImageManager()
    {
        //LoadImages();

    }
    
    public void Load()
    {
        LoadImage("BloodOverlay");
        LoadImage("RareFrame");
        LoadImage("OutlinedEllipse");
        LoadImage("Arrow");
        LoadImage("blockimg");
        LoadImage("BlueBlur");
        LoadImage("Eye");
        LoadImage("mouse");
        LoadImage("questionmark");
        LoadImage("robotimg");
        LoadImage("SalHackWatermark");
        LoadImage("Shield");
        LoadImage("skull");
    }
    
    public void LoadImage(String p_Img)
    {
        BufferedImage l_Image = null;
        
        InputStream l_Stream = ImageManager.class.getResourceAsStream("/assets/salhack/imgs/" + p_Img + ".png");
        
        try
        {
            l_Image = ImageIO.read(l_Stream);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (l_Image == null)
        {
            System.out.println("Couldn't load image: " + p_Img);
            return;
        }

        int l_Height = l_Image.getHeight();
        int l_Width = l_Image.getWidth();

        final SalDynamicTexture l_Texture = new SalDynamicTexture(l_Image, l_Height, l_Width);
        if (l_Texture != null)
        {
            l_Texture.SetResourceLocation(Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("salhack/textures", l_Texture));

            Pictures.put(p_Img, l_Texture);
            
            System.out.println("Loaded Img: " + p_Img);
        }
    }

    public SalDynamicTexture GetDynamicTexture(String p_Image)
    {
        if (Pictures.containsKey(p_Image))
            return Pictures.get(p_Image);

        return null;
    }
    
    public String GetNextImage(String value, boolean p_Recursive)
    {
        String l_String = null;

        for (Map.Entry<String, SalDynamicTexture> l_Itr : Pictures.entrySet())
        {
            if (!l_Itr.getKey().equalsIgnoreCase(value))
                continue;

            if (p_Recursive)
            {
                l_String = Pictures.lowerKey(l_Itr.getKey());

                if (l_String == null)
                    return Pictures.lastKey();
            }
            else
            {
                l_String = Pictures.higherKey(l_Itr.getKey());

                if (l_String == null)
                    return Pictures.firstKey();
            }

            return l_String;
        }

        return l_String;
    }

    public static ImageManager Get()
    {
        return SalHack.GetImageManager();
    }

}
