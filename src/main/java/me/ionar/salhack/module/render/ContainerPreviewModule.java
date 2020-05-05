package me.ionar.salhack.module.render;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import org.lwjgl.opengl.GL11;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.render.GLUProjection;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class ContainerPreviewModule extends Module
{
    public ContainerPreviewModule()
    {
        super("ContainerPreview", new String[]
        { "" }, "Shows you what's in a container", "NONE", 0xB3DB24, ModuleType.RENDER);
    }

    private HashMap<BlockPos, ArrayList<ItemStack>> PosItems = new HashMap<BlockPos, ArrayList<ItemStack>>();
    private ICamera camera = new Frustum();
    private int TotalSlots = 0;

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketWindowItems)
        {
            final RayTraceResult ray = mc.objectMouseOver;

            if (ray == null)
                return;

            if (ray.typeOfHit != RayTraceResult.Type.BLOCK)
                return;

            SPacketWindowItems l_Packet = (SPacketWindowItems) p_Event.getPacket();

            final BlockPos blockpos = ray.getBlockPos();

            if (PosItems.containsKey(blockpos))
                PosItems.remove(blockpos);

            ArrayList<ItemStack> l_List = new ArrayList<ItemStack>();

            for (int i = 0; i < l_Packet.getItemStacks().size(); ++i)
            {
                ItemStack itemStack = l_Packet.getItemStacks().get(i);
                if (itemStack == null)
                    continue;

                if (i > TotalSlots)
                    break;

                l_List.add(itemStack);
            }

            PosItems.put(blockpos, l_List);
        }
        else if (p_Event.getPacket() instanceof SPacketOpenWindow)
        {
            final SPacketOpenWindow l_Packet = (SPacketOpenWindow) p_Event.getPacket();

            TotalSlots = l_Packet.getSlotCount();
            return;
        }
    });

    @EventHandler
    private Listener<EventRenderGameOverlay> OnRenderGameOverlay = new Listener<>(p_Event ->
    {
        final RayTraceResult ray = mc.objectMouseOver;
        if (ray == null)
            return;
        if (ray.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        if (!PosItems.containsKey(ray.getBlockPos()))
            return;

        BlockPos l_Pos = ray.getBlockPos();
        
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        final RayTraceResult ray = mc.objectMouseOver;
        if (ray == null)
            return;
        if (ray.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        if (!PosItems.containsKey(ray.getBlockPos()))
            return;

        BlockPos l_Pos = ray.getBlockPos();

        final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX, l_Pos.getY() - mc.getRenderManager().viewerPosY, l_Pos.getZ() - mc.getRenderManager().viewerPosZ,
                l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, l_Pos.getY() + 1 - mc.getRenderManager().viewerPosY, l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

        RenderUtil.drawBoundingBox(bb, 1, 0x90000000);

        final ArrayList<ItemStack> l_List = PosItems.get(ray.getBlockPos());

        GlStateManager.pushMatrix();

        final boolean bobbing = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        mc.entityRenderer.setupCameraTransform(p_Event.getPartialTicks(), 0);
        RenderUtil.glBillboard((float) l_Pos.getX()+0.5f, (float) l_Pos.getY()+0.5f, (float) l_Pos.getZ()+0.5f);

        GL11.glTranslatef(0, 20, 0);

        GlStateManager.scale(-40, -40, 40);

        GlStateManager.disableDepth();
      //  GlStateManager.translate(((l_List.size() - 1) / 2f) * .5f, .6, 0);
        GlStateManager.translate(1.5f, 1f, 0f);
        
        int l_Iterations = l_List.size()/9;
        
        for (int x = 0; x < l_Iterations; x++)
        {
            GlStateManager.pushMatrix();
            
            int l_Itr = 0;
            
            for (int i = 0; i < 9; ++i)
            {
                ItemStack itemStack = l_List.get(i + l_Itr++);
                
                if (itemStack == null)
                    break;
                
                GlStateManager.pushAttrib();
                RenderHelper.enableStandardItemLighting();
                GlStateManager.scale(.25, .25, 0);
                GlStateManager.disableLighting();
                mc.getRenderItem().zLevel = -5;
                mc.getRenderItem().renderItem(itemStack,ItemCameraTransforms.TransformType.NONE);
                mc.getRenderItem().zLevel = 0;
                GlStateManager.scale(4, 4, 0);
                GlStateManager.popAttrib();
    
                GlStateManager.translate(-0.25f, 0f, 0f);
            }

            GlStateManager.popMatrix();
            GlStateManager.translate(0f, 0.25f, 0f);
        }

        mc.gameSettings.viewBobbing = bobbing;
        mc.entityRenderer.setupCameraTransform(p_Event.getPartialTicks(), 0);
        GlStateManager.popMatrix();
    });

    private float[] convertBounds(Entity e, float partialTicks, int width, int height)
    {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        final Vec3d pos = MathUtil.interpolateEntity(e, partialTicks);

        if (pos == null)
        {
            return null;
        }

        AxisAlignedBB bb = e.getEntityBoundingBox();

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        camera.setPosition(mc.getRenderViewEntity().posX,
                mc.getRenderViewEntity().posY,
                mc.getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(bb))
        {
            return null;
        }

        final Vec3d corners[] =
        { new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),

                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2) };

        for (Vec3d vec : corners)
        {
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(
                    pos.x + vec.x - mc.getRenderManager().viewerPosX,
                    pos.y + vec.y - mc.getRenderManager().viewerPosY,
                    pos.z + vec.z - mc.getRenderManager().viewerPosZ,
                    GLUProjection.ClampMode.NONE, false);

            if (projection == null)
            {
                return null;
            }

            x = Math.max(x, (float) projection.getX());
            y = Math.max(y, (float) projection.getY());

            w = Math.min(w, (float) projection.getX());
            h = Math.min(h, (float) projection.getY());
        }

        if (x != -1 && y != -1 && w != width + 1 && h != height + 1)
        {
            return new float[]
            { x, y, w, h };
        }

        return null;
    }

}
