package me.ionar.salhack.module.render;

import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

// Tracer mod by Hoodlands :P thanks to ionar and jumbo for holding my hand
public class TracersModule extends Module
{
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] { "Players" }, "Traces players", true);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[] { "Friends" }, "Traces friends", true);
    public final Value<Boolean> Invisibles = new Value<Boolean>("Invisibles", new String[] { "Invisibles" }, "Traces invisibles", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[] { "Monsters" }, "Traces monsters", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] { "Animals" }, "Traces animals", false);
    public final Value<Boolean> Vehicles = new Value<Boolean>("Vehicles", new String[] { "Vehicles" }, "Traces Vehicles", false);
    public final Value<Boolean> Items = new Value<Boolean>("Items", new String[] { "Items" }, "Traces items", true);
    public final Value<Boolean> Others = new Value<Boolean>("Others", new String[] { "Others" }, "Traces others", false);

    public TracersModule()
    {
        super("Tracers", new String[] {"Tracers"}, "Draws tracer to a given entity", "NONE", -1, ModuleType.RENDER);
    }

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        for (Entity entity : mc.world.loadedEntityList)
        {
            if (shouldRenderTracer(entity))
            {
                final Vec3d pos = MathUtil.interpolateEntity(entity, p_Event.getPartialTicks()).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);

                if (pos != null)
                {
                   final boolean bobbing = mc.gameSettings.viewBobbing;
                   mc.gameSettings.viewBobbing = false;
                   mc.entityRenderer.setupCameraTransform(p_Event.getPartialTicks(), 0);
                   final Vec3d forward = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));
                   RenderUtil.drawLine3D((float) forward.x, (float) forward.y + mc.player.getEyeHeight(), (float) forward.z, (float) pos.x, (float) pos.y, (float) pos.z, 0.5f, getColor(entity));
                   mc.gameSettings.viewBobbing = bobbing;
                   mc.entityRenderer.setupCameraTransform(p_Event.getPartialTicks(), 0);
                }
            }
        }
    });

    public boolean shouldRenderTracer (Entity e)
    {
        if(e == Minecraft.getMinecraft().player)
            return false;
        if(e instanceof EntityPlayer)
            return Players.getValue();
        if((EntityUtil.isHostileMob(e) || EntityUtil.isNeutralMob(e)))
            return Monsters.getValue();
        if(EntityUtil.isPassive(e))
            return Animals.getValue();
        if((e instanceof EntityBoat || e instanceof EntityMinecart))
            return Vehicles.getValue();
        if(e instanceof EntityItem)
            return Items.getValue();
        return Others.getValue();
    }

    private int getColor (Entity e)
    {
        if (e instanceof EntityPlayer)
        {
            if (FriendManager.Get().IsFriend(e))
                return 0xFF00FFEE;
            return 0xFF00FF00;
        }
        if (e.isInvisible())
            return 0xFF000000;
        if ((EntityUtil.isHostileMob(e) || EntityUtil.isNeutralMob(e)))
            return 0xFFFF0000;
        if (EntityUtil.isPassive(e))
            return 0xFFFF8D00;
        if ((e instanceof EntityBoat || e instanceof EntityMinecart))
            return 0xFFFFFF00;
        if (e instanceof EntityItem)
            return 0xFFAA00FF;
        return 0xFFFFFFFF;
    }
}
