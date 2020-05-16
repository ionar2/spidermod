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
import java.util.List;
import java.util.Timer;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;

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
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
            
            IBlockState l_State = mc.world.getBlockState(ray.getBlockPos());
            
            if (l_State == null)
                return;
            
            if (l_State.getBlock() != Blocks.CHEST && !(l_State.getBlock() instanceof BlockShulkerBox))
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

                if (i >= TotalSlots)
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
        
        ArrayList<ItemStack> l_Items = PosItems.get(l_Pos);
        
        if (l_Items == null)
            return;
        
        final float[] bounds = this.convertBounds(l_Pos, p_Event.getPartialTicks(),
                p_Event.getScaledResolution().getScaledWidth(),
                p_Event.getScaledResolution().getScaledHeight());

        if (bounds != null)
        {
            int l_I = 0;
            int l_Y = -20;
            int x = 0;
            
            for (ItemStack stack : l_Items)
            {
                if (stack != null)
                {
                    //final Item item = stack.getItem();
                    //if (item != Items.AIR)
                    {
                        GlStateManager.pushMatrix();
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.translate(
                                bounds[0] + (bounds[2] - bounds[0]) / 2 + x,
                                l_Y + bounds[1] + (bounds[3] - bounds[1]) - mc.fontRenderer.FONT_HEIGHT - 19,
                                0);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
                        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.disableBlend();
                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.popMatrix();
                        x += 16;
                    }
                }
                
                if (++l_I % 9 == 0)
                {
                    x = 0;
                    l_Y += 15;
                }
            }
        }
        
    });

    private float[] convertBounds(BlockPos e, float partialTicks, int width, int height)
    {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        final Vec3d pos = new Vec3d(e.getX(), e.getY(), e.getZ());

        if (pos == null)
        {
            return null;
        }

        AxisAlignedBB bb = new AxisAlignedBB(
                e.getX() - mc.getRenderManager().viewerPosX,
                e.getY() - mc.getRenderManager().viewerPosY,
                e.getZ() - mc.getRenderManager().viewerPosZ,
                e.getX() + 1 - mc.getRenderManager().viewerPosX,
                e.getY() + 1 - mc.getRenderManager().viewerPosY,
                e.getZ() + 1 - mc.getRenderManager().viewerPosZ);

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        camera.setPosition(mc.getRenderViewEntity().posX,
                mc.getRenderViewEntity().posY,
                mc.getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(bb))
        {
            /// @todo: fix this
    //        return null;
        }

        final Vec3d corners[] =
        { new Vec3d(bb.minX - bb.maxX + 1 / 2, 0, bb.minZ - bb.maxZ + 1 / 2),
                new Vec3d(bb.maxX - bb.minX - 1 / 2, 0, bb.minZ - bb.maxZ + 1 / 2),
                new Vec3d(bb.minX - bb.maxX + 1 / 2, 0, bb.maxZ - bb.minZ - 1 / 2),
                new Vec3d(bb.maxX - bb.minX - 1 / 2, 0, bb.maxZ - bb.minZ - 1 / 2),

                new Vec3d(bb.minX - bb.maxX + 1 / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + 1 / 2),
                new Vec3d(bb.maxX - bb.minX - 1 / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + 1 / 2),
                new Vec3d(bb.minX - bb.maxX + 1 / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - 1 / 2),
                new Vec3d(bb.maxX - bb.minX - 1 / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - 1 / 2) };

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
