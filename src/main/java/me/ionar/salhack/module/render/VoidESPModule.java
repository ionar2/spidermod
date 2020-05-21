package me.ionar.salhack.module.render;

import static me.ionar.salhack.util.render.ESPUtil.IsVoidHole;
import static me.ionar.salhack.util.render.ESPUtil.isBlockValid;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Hole;
import me.ionar.salhack.util.Hole.HoleTypes;
import me.ionar.salhack.util.render.ESPUtil.HoleModes;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class VoidESPModule extends Module
{
    public final Value<Integer> Radius = new Value<Integer>("Radius", new String[] { "Radius", "Range", "Distance" }, "Radius in blocks to scan for void blocks.", 8, 0, 32, 1);

    public VoidESPModule()
    {
        super("VoidESP", new String[] {""}, "Highlights void blocks", "NONE", -1, ModuleType.RENDER);
    }
    
    public final List<BlockPos> VoidBlocks = new ArrayList<>();
    private ICamera camera = new Frustum();

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (mc.player == null)
            return;
        
        VoidBlocks.clear();
        
        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);
        
        for (int x = playerPos.getX() - Radius.getValue(); x < playerPos.getX() + Radius.getValue(); x++)
        {
            for (int z = playerPos.getZ() - Radius.getValue(); z < playerPos.getZ() + Radius.getValue(); z++)
            {
                for (int y = playerPos.getY() + Radius.getValue(); y > playerPos.getY() - Radius.getValue(); y--)
                {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final IBlockState blockState = mc.world.getBlockState(blockPos);
                    
                    if (IsVoidHole(blockPos, blockState))
                        VoidBlocks.add(blockPos);
                }
            }
        }
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        new ArrayList<BlockPos>(VoidBlocks).forEach(p_Pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(p_Pos.getX() - mc.getRenderManager().viewerPosX, p_Pos.getY() - mc.getRenderManager().viewerPosY,
                    p_Pos.getZ() - mc.getRenderManager().viewerPosZ, p_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, p_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    p_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ)))
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glLineWidth(1.5f);

                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 0.5f, 0, 1, 0.50f);
                RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 0.5f, 0, 1, 0.22f);

                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    });
}
