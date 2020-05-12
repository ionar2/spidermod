package me.ionar.salhack.module.render;

import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import static org.lwjgl.opengl.GL11.*;

public class BlockHighlightModule extends Module
{
    public final Value<HoleModes> HighlightMode = new Value<HoleModes>("HighlightModes", new String[] {"HM"}, "Mode for highlighting blocks", HoleModes.Full);
    public final Value<Float> ObsidianRed = new Value<Float>("Red", new String[] {"oRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianGreen = new Value<Float>("Green", new String[] {"oGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianBlue = new Value<Float>("Blue", new String[] {"oBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianAlpha = new Value<Float>("Alpha", new String[] {"oAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    private enum HoleModes
    {
        FlatOutline,
        Flat,
        Outline,
        Full,
    }

    public BlockHighlightModule()
    {
        super("BlockHighlight", new String[]
                        { "BlockHighlights" }, "Highlights the block you are looking at",
                "NONE", -1, Module.ModuleType.RENDER);
    }

    private ICamera camera = new Frustum();

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null)
            return;

        final RayTraceResult ray = mc.objectMouseOver;

        if (ray == null)
            return;

        if (ray.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        BlockPos l_Pos = ray.getBlockPos();

        final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX,
                l_Pos.getY() - mc.getRenderManager().viewerPosY, l_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                l_Pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                mc.getRenderViewEntity().posZ);

        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                bb.maxZ + mc.getRenderManager().viewerPosZ)))
        {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth(1.5f);

            Render(bb, ObsidianRed.getValue(), ObsidianGreen.getValue(), ObsidianBlue.getValue(), ObsidianAlpha.getValue());

            glDisable(GL_LINE_SMOOTH);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    });

    private void Render(final AxisAlignedBB bb, float p_Red, float p_Green, float p_Blue, float p_Alpha)
    {
        switch (HighlightMode.getValue())
        {
            case Flat:
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            case FlatOutline:
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            case Full:
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            case Outline:
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, p_Red, p_Green, p_Blue, p_Alpha);
                break;
            default:
                break;
        }
    }
}
