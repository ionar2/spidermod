package me.ionar.salhack.module.render;

import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;


public class MapBoundaries extends Module {
    public MapBoundaries()
    {
        super("MapBoundaries", new String[] {""}, "Shows where a 1x1 map will go", "NONE", -1, ModuleType.RENDER);
    }
    public final Value<Float> Red = new Value<Float>("Red", new String[] {"MBRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Green = new Value<Float>("Green", new String[] {"MBGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> Blue = new Value<Float>("Blue", new String[] {"MBBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> Alpha = new Value<Float>("Alpha", new String[] {"MBAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        final float partialTicks = p_Event.getPartialTicks();

        final EntityPlayer player = mc.player;

        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;

        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;

        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        int xCenter = MathHelper.floor((d0 + 64.0D) / 128) * 128;

        int zCenter = MathHelper.floor((d2 + 64.0D) / 128) * 128;

        double x = xCenter - 64 - mc.getRenderManager().viewerPosX;

        double z = zCenter - 64 - mc.getRenderManager().viewerPosZ;

        final AxisAlignedBB bb = new AxisAlignedBB(x, -mc.getRenderManager().viewerPosY, z, x+128, 256-mc.getRenderManager().viewerPosY, z+128);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1.5f);

        RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, Red.getValue(), Green.getValue(), Blue.getValue(), Alpha.getValue());

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();




});
}
