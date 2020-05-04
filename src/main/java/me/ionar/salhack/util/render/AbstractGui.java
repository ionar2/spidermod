package me.ionar.salhack.util.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGui
{

    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(
            "textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation(
            "textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    protected int blitOffset;

    public static void fill(int x0, int y1, int x1, int y0, int color)
    {
        if (x0 < x1)
        {
            int i = x0;
            x0 = x1;
            x1 = i;
        }

        if (y1 < y0)
        {
            int j = y1;
            y1 = y0;
            y0 = j;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double) x0, (double) y0, 0.0D).endVertex();
        bufferbuilder.pos((double) x1, (double) y0, 0.0D).endVertex();
        bufferbuilder.pos((double) x1, (double) y1, 0.0D).endVertex();
        bufferbuilder.pos((double) x0, (double) y1, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void blit(int x, int y, int z, int width, int height, TextureAtlasSprite sprite)
    {
        innerBlit(x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(),
                sprite.getMaxV());
    }

    public static void blit(int x, int y, int z, float u, float v, int width, int height, int vScale, int uScale)
    {
        innerBlit(x, x + width, y, y + height, z, width, height, u, v, uScale, vScale);
    }

    public static void blit(int x, int y, int width, int height, float minU, float minV, int maxU, int maxV, int uScale,
            int vScale)
    {
        innerBlit(x, x + width, y, y + height, 0, maxU, maxV, minU, minV, uScale, vScale);
    }

    public static void blit(int x, int y, float minU, float minV, int width, int height, int uScale, int vScale)
    {
        blit(x, y, width, height, minU, minV, width, height, uScale, vScale);
    }

    private static void innerBlit(int x0, int x1, int y0, int y1, int z, int maxU, int maxV, float minU, float minV,
            int uScale, int vScale)
    {
        innerBlit(x0, x1, y0, y1, z, (minU + 0.0F) / (float) uScale, (minU + (float) maxU) / (float) uScale,
                (minV + 0.0F) / (float) vScale, (minV + (float) maxV) / (float) vScale);
    }

    protected static void innerBlit(int x0, int x1, int y1, int y0, int z, float minU, float maxU, float minV,
            float maxV)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double) x0, (double) y0, (double) z).tex((double) minU, (double) maxV).endVertex();
        bufferbuilder.pos((double) x1, (double) y0, (double) z).tex((double) maxU, (double) maxV).endVertex();
        bufferbuilder.pos((double) x1, (double) y1, (double) z).tex((double) maxU, (double) minV).endVertex();
        bufferbuilder.pos((double) x0, (double) y1, (double) z).tex((double) minU, (double) minV).endVertex();
        tessellator.draw();
    }

    protected void hLine(int x, int width, int y, int color)
    {
        if (width < x)
        {
            int i = x;
            x = width;
            width = i;
        }

        fill(x, y, width + 1, y + 1, color);
    }

    protected void vLine(int x, int y, int height, int color)
    {
        if (height < y)
        {
            int i = y;
            y = height;
            height = i;
        }

        fill(x, y + 1, x + 1, height, color);
    }

    protected void fillGradient(int x1, int y0, int x0, int y1, int color0, int color1)
    {
        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double) x0, (double) y0, (double) this.blitOffset).color(r0, g0, b0, a0).endVertex();
        bufferbuilder.pos((double) x1, (double) y0, (double) this.blitOffset).color(r0, g0, b0, a0).endVertex();
        bufferbuilder.pos((double) x1, (double) y1, (double) this.blitOffset).color(r1, g1, b1, a1).endVertex();
        bufferbuilder.pos((double) x0, (double) y1, (double) this.blitOffset).color(r1, g1, b1, a1).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    
    public void blit(int x, int y, int u, int v, int width, int height)
    {
        blit(x, y, this.blitOffset, (float) u, (float) v, width, height, 256, 256);
    }

}
