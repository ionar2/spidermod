package me.ionar.salhack.module.render;

import java.sql.Types;
import java.text.DecimalFormat;

import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.GLUProjection;
import me.ionar.salhack.util.render.RenderUtil;
import me.ionar.salhack.waypoints.Waypoint;
import me.ionar.salhack.waypoints.WaypointManager;
import me.ionar.salhack.waypoints.WaypointManager.PlayerData;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;

public class WaypointsModule extends Module
{
    public final Value<Boolean> Normal = new Value<Boolean>("Normal", new String[] {"Normal"}, "Displays normal waypoints", true);
    public final Value<Boolean> LogoutSpots = new Value<Boolean>("LogoutSpots", new String[] {"LogoutSpots"}, "Displays players LogoutSpots", true);
    public final Value<Boolean> DeathPoints = new Value<Boolean>("DeathPoints", new String[] {"DeathPoints"}, "Displays players DeathPoints", true);
    public final Value<Boolean> CoordTPExploit = new Value<Boolean>("CoordTPExploit", new String[] {"CoordTPExploit"}, "Displays waypoints created by CoordTPExploit", true);
    public final Value<Boolean> Tracers = new Value<Boolean>("Tracers", new String[] {"Tracers"}, "Points tracers to each waypoint", false);
    public static final Value<Integer> RemoveDistance = new Value<Integer>("RemoveDistance", new String[]{"RD", "RemoveRange"}, "Minimum distance in blocks the player must be away from the spot for it to be removed.", 200, 1, 2000, 1);

    public WaypointsModule()
    {
        super("Waypoints", new String[] {"Waypoint"}, "Displays waypoints in your render", "NONE", -1, ModuleType.RENDER);
    }
    
    private DecimalFormat _formatter = new DecimalFormat("#.#");
    
    @EventHandler
    private Listener<EventRenderGameOverlay> OnRenderGameOverlay = new Listener<>(event ->
    {
        final String currentAddress = Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "singleplayer";
        
        // iterate all the waypoints available
        for (Waypoint point : WaypointManager.Get().GetWaypoints())
        {
            // verify server address
            if (!point.getAddress().equals(currentAddress))
                continue;
            
            // we only want to render things that are visible with our options
            if (point.getType() == Waypoint.Type.Normal && !Normal.getValue())
                continue;
            if (point.getType() == Waypoint.Type.Logout && !LogoutSpots.getValue())
                continue;
            if (point.getType() == Waypoint.Type.Death && !DeathPoints.getValue())
                continue;
            if (point.getType() == Waypoint.Type.CoordTPExploit && !CoordTPExploit.getValue())
                continue;
            
            if (point.getDimension() == 1 && mc.player.dimension != 1) // if this is an end coord and we are not in end, continue
                continue;
            
            // however we want to only show end coords in the end.
            if (mc.player.dimension == 1 && point.getDimension() != 1)
                continue;

            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();
            
            if (point.getDimension() == -1 && mc.player.dimension == 0) // nether coord
            {
                // multiply by 8 if we are in overworld
                x *= 8;
                z *= 8;
            }
            else if (point.getDimension() == 0 && mc.player.dimension == -1)
            {
                // convert overworld coord to nether
                x /= 8;
                z /= 8;
            }
            
            // create a projection towards this position
            GLUProjection.Projection projection = GLUProjection.getInstance().project(
                    x - mc.getRenderManager().viewerPosX,
                    y - mc.getRenderManager().viewerPosY,
                    z - mc.getRenderManager().viewerPosZ,
                    GLUProjection.ClampMode.NONE,
                    false);
            
            // verify the projection is not null and is an inside location
            if (projection != null && projection.getType() == GLUProjection.Projection.Type.INSIDE)
            {
                final double distance = mc.player.getDistance(x, y, z);
                
                final String displayName = point.getDisplayName() + " " + _formatter.format(distance) + "m";
                
                RenderUtil.drawStringWithShadow(displayName, (float)projection.getX()-RenderUtil.getStringWidth(displayName), (float)projection.getY()-RenderUtil.getStringHeight(displayName), -1);
            }
            
            if (Tracers.getValue())
            {
                // create another projection
                projection = GLUProjection.getInstance().project(
                        x - mc.getRenderManager().viewerPosX,
                        y - mc.getRenderManager().viewerPosY,
                        z - mc.getRenderManager().viewerPosZ,
                        GLUProjection.ClampMode.NONE,
                        true);

                if (projection != null)
                {
                    // draw the line
                    RenderUtil.drawLine((float) projection.getX(), (float) projection.getY(), event.getScaledResolution().getScaledWidth() / 2, event.getScaledResolution().getScaledHeight() / 2, .5f, -1);
                }
            }
        }
    });
    
    @EventHandler
    private Listener<RenderEvent> OnRender = new Listener<>(event ->
    {
        if (mc.player == null || mc.getRenderManager() == null || mc.getRenderManager().renderViewEntity == null)
            return;
        
        if (LogoutSpots.getValue())
        {
            for (String uuid : WaypointManager.Get().getLogoutCache().keySet())
            {
                final PlayerData data = WaypointManager.Get().getLogoutCache().get(uuid);
    
                if (WaypointManager.Get().isOutOfRange(data))
                {
                    WaypointManager.Get().RemoveLogoutCache(uuid);
                    continue;
                }
    
                data.ghost.prevLimbSwingAmount = 0;
                data.ghost.limbSwing = 0;
                data.ghost.limbSwingAmount = 0;
                data.ghost.hurtTime = 0;
    
                GlStateManager.pushMatrix();
                GlStateManager.enableLighting();
                GlStateManager.enableBlend();
                GlStateManager.enableDepth();
                GlStateManager.color(1, 1, 1, 1);
                mc.getRenderManager().renderEntity(data.ghost, data.position.x - mc.getRenderManager().renderPosX, data.position.y - mc.getRenderManager().renderPosY, data.position.z - mc.getRenderManager().renderPosZ, data.ghost.rotationYaw, mc.getRenderPartialTicks(), false);
                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });
}
