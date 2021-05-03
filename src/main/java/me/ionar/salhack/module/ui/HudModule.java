package me.ionar.salhack.module.ui;

import me.ionar.salhack.events.render.EventRenderGameOverlay;
import me.ionar.salhack.events.render.EventRenderGetFOVModifier;
import me.ionar.salhack.events.render.EventRenderHand;
import me.ionar.salhack.managers.HudManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.glu.Project;

public final class HudModule extends Module
{
    public static final Value<Integer> ExtraTab = new Value<Integer>("ExtraTab", new String[]
    { "ET" }, "Max playerslots to show in the tab list", 80, 80, 1000, 10);
    public final Value<Boolean> CustomFOV = new Value<Boolean>("CustomFOV", new String[]
    { "CustomFOV" }, "Enables the option below", false);
    public final Value<Float> FOV = new Value<Float>("FOV", new String[]
    { "FOV" }, "Override the clientside FOV", mc.gameSettings.fovSetting, 0f, 170f, 10f);
    public final Value<Boolean> NoHurtCam = new Value<Boolean>("NoHurtCam", new String[]
    { "NoHurtCam" }, "Disables hurt camera effect", true);
    public final Value<Boolean> NoBob = new Value<Boolean>("NoBob", new String[]
    { "NoBob" }, "Disables bobbing effect", true);
    public final Value<Boolean> CustomFont = new Value<Boolean>("CustomFont", new String[] {"CF"}, "Displays the custom font", true);
    public final Value<Boolean> Rainbow = new Value<Boolean>("Rainbow", new String[] {"RGB"}, "Give HUD items rainbow effect.", true);

    public HudModule()
    {
        super("HUD", new String[]
        { "HUD" }, "Displays the HUD", "NONE", 0xD1DB24, ModuleType.UI);
    }

    @EventHandler
    private Listener<EventRenderGameOverlay> OnRenderGameOverlay = new Listener<>(p_Event ->
    {
        if (!mc.gameSettings.showDebugInfo)
            HudManager.Get().OnRender(p_Event.PartialTicks);
    });

    @EventHandler
    private Listener<EventRenderGetFOVModifier> OnGetFOVModifier = new Listener<>(p_Event ->
    {
        if (!CustomFOV.getValue())
            return;

        p_Event.cancel();
        p_Event.SetFOV(FOV.getValue());
    });

    @EventHandler
    private Listener<EventRenderHand> OnRenderHand = new Listener<>(p_Event ->
    {
        if (!CustomFOV.getValue())
            return;
        
        p_Event.cancel();

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        float f = 0.07F;

        if (mc.entityRenderer.mc.gameSettings.anaglyph)
        {
            GlStateManager.translate((float) (-(p_Event.Pass * 2 - 1)) * 0.07F, 0.0F, 0.0F);
        }

        Project.gluPerspective(70.0f, (float) mc.entityRenderer.mc.displayWidth / (float) mc.entityRenderer.mc.displayHeight, 0.05F, mc.entityRenderer.farPlaneDistance * 2.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();

        if (mc.entityRenderer.mc.gameSettings.anaglyph)
        {
            GlStateManager.translate((float) (p_Event.Pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        GlStateManager.pushMatrix();
        hurtCameraEffect(p_Event.PartialTicks);

        if (mc.entityRenderer.mc.gameSettings.viewBobbing)
        {
            applyBobbing(p_Event.PartialTicks);
        }

        boolean flag = mc.entityRenderer.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.entityRenderer.mc.getRenderViewEntity()).isPlayerSleeping();

        if (!net.minecraftforge.client.ForgeHooksClient.renderFirstPersonHand(mc.renderGlobal, p_Event.PartialTicks, p_Event.Pass))
            if (mc.entityRenderer.mc.gameSettings.thirdPersonView == 0 && !flag && !mc.entityRenderer.mc.gameSettings.hideGUI && !mc.entityRenderer.mc.playerController.isSpectator())
            {
                mc.entityRenderer.enableLightmap();
                mc.entityRenderer.itemRenderer.renderItemInFirstPerson(p_Event.PartialTicks);
                mc.entityRenderer.disableLightmap();
            }

        GlStateManager.popMatrix();

        if (mc.entityRenderer.mc.gameSettings.thirdPersonView == 0 && !flag)
        {
            mc.entityRenderer.itemRenderer.renderOverlays(p_Event.PartialTicks);
            hurtCameraEffect(p_Event.PartialTicks);
        }

        if (mc.entityRenderer.mc.gameSettings.viewBobbing)
        {
            applyBobbing(p_Event.PartialTicks);
        }
    });

    private void hurtCameraEffect(float partialTicks)
    {
        if (NoHurtCam.getValue())
            return;

        if (this.mc.getRenderViewEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase) this.mc.getRenderViewEntity();
            float f = (float) entitylivingbase.hurtTime - partialTicks;

            if (entitylivingbase.getHealth() <= 0.0F)
            {
                float f1 = (float) entitylivingbase.deathTime + partialTicks;
                GlStateManager.rotate(40.0F - 8000.0F / (f1 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f < 0.0F)
            {
                return;
            }

            f = f / (float) entitylivingbase.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * (float) Math.PI);
            float f2 = entitylivingbase.attackedAtYaw;
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-f * 14.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * Updates the bobbing render effect of the player.
     */
    private void applyBobbing(float partialTicks)
    {
        if (NoBob.getValue())
            return;

        if (this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
            float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
            float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            GlStateManager.translate(MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F, -Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2), 0.0F);
            GlStateManager.rotate(MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
        }
    }

}
