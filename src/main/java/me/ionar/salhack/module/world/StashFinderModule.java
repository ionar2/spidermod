package me.ionar.salhack.module.world;

import java.util.ArrayList;
import java.util.List;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import static me.ionar.salhack.util.HilbertCurve.*;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLineWidth;

public class StashFinderModule extends Module
{
    public final Value<Integer> Curve = new Value<Integer>("Curve", new String[] {"Curves"}, "Curves to use for hilbert curve, more = bigger path", 5, 1, 5, 1);
    public final Value<Boolean> Render = new Value<Boolean>("Visualizer", new String[] {"Render"}, "Renders the path", true);
    public final Value<Boolean> Loop = new Value<Boolean>("Loop", new String[] {"Loop"}, "Loops after a finish", false);
    public final Value<Boolean> ToggleLog = new Value<Boolean>("ToggleStashLogger", new String[] {"ToggleLog"}, "Automatically toggles on StashLogger if not already enabled", true);
    
    public StashFinderModule()
    {
        super("StashFinder", new String[]
        { "BaseFinder" }, "Automatically pilots you towards generated waypoints", "NONE", -1, ModuleType.WORLD);
    }
    
    private ArrayList<BlockPos> WaypointPath = new ArrayList<BlockPos>();
    private ICamera camera = new Frustum();
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();

        int order = Curve.getValue();
        
        int n = (1 << order);
        List<Point> points = getPointsForCurve(n);
        
        WaypointPath.clear();
        
        points.forEach(p ->
            WaypointPath.add(new BlockPos((int)mc.player.posX + p.x * 16 * 8, 165, (int)mc.player.posZ + p.y * 16 * 8)));
        
        SendMessage("Turn on AutoWalk and StashLogger to begin!");
        
        if (ToggleLog.getValue())
        {
            final Module mod = ModuleManager.Get().GetMod(StashLoggerModule.class);
            
            if (!mod.isEnabled())
                mod.toggle();
        }
    }
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (!WaypointPath.isEmpty())
        {
            BlockPos first = WaypointPath.get(0);
            
            final double rotations[] =  EntityUtil.calculateLookAt(
                    first.getX() + 0.5,
                    first.getY() - 0.5,
                    first.getZ() + 0.5,
                    mc.player);
            
            //mc.player.rotationPitch = (float)rotations[1];
            mc.player.rotationYaw = (float)rotations[0];
            
            if (getDistance2D(first) < 10)
            {
                WaypointPath.remove(first);
                //SendMessage(String.format("Removed the point at %s remaining size: %s", first.toString(), WaypointPath.size()));
            }
        }
        else if (Loop.getValue())
        {
            int order = Curve.getValue();
            
            int n = (1 << order);
            List<Point> points = getPointsForCurve(n);
            
            WaypointPath.clear();
            
            points.forEach(p ->
                WaypointPath.add(new BlockPos((int)mc.player.posX + p.x * 16 * 8, 165, (int)mc.player.posZ + p.y * 16 * 8)));
        }
    });
    
    private double getDistance2D(BlockPos pos)
    {
        double posX = Math.abs(mc.player.posX - pos.getX());
        double posZ = Math.abs(mc.player.posZ - pos.getZ());
        
        return posX + posZ;
    }
    
    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;
        
        new ArrayList<BlockPos>(WaypointPath).forEach(pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX,
                    pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ,
                    pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
    
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
                
                RenderUtil.drawBoundingBox(bb, 1.0f, 0x90990099);
                RenderUtil.drawFilledBox(bb, 0x90990099);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    });
}
