package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.render.ESPModule;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.Hole;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;

public class HoleFillerModule extends Module
{
    public final Value<Boolean> TogglesOff = new Value<Boolean>("TogglesOff", new String[]
    { "TogglesOff" }, "Toggles Off after filling all the holes around you", true);
    public final Value<Integer> MaxHoles = new Value<Integer>("MaxHoles", new String[] {"MaxHoles"}, "Maximum number of holes to fill", 5, 1, 20, 1);
    public final Value<Float> Radius = new Value<Float>("Radius", new String[] {"Range"}, "Range to search for holes", 5.0f, 1.0f, 10.0f, 1.0f);
    public final Value<Boolean> Render = new Value<Boolean>("Visualize", new String[]
            { "Visualize" }, "Visualizes the holes that we are attempting to fill", true);

    public final Value<Float> ObsidianRed = new Value<Float>("ObsidianRed", new String[] {"oRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianGreen = new Value<Float>("ObsidianGreen", new String[] {"oGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianBlue = new Value<Float>("ObsidianBlue", new String[] {"oBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianAlpha = new Value<Float>("ObsidianAlpha", new String[] {"oAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    public final Value<HoleModes> HoleMode = new Value<HoleModes>("HoleModed", new String[] {"HM"}, "Mode for rendering holes", HoleModes.Full);

    private enum HoleModes
    {
        FlatOutline,
        Flat,
        Outline,
        Full,
    }

    public HoleFillerModule()
    {
        super("HoleFiller", new String[]
        { "HoleFill" }, "Automatically fills up to x holes around you when enabled, if togglesoff is not enabled, it will continue to fill holes.",
                "NONE", -1, ModuleType.COMBAT);
    }

    private ICamera camera = new Frustum();
    private ArrayList<BlockPos> HolesToFill = new ArrayList<BlockPos>();
    
    @Override
    public void toggleNoSave()
    {
        
    }

    @Override
    public String getMetaData()
    {
        return String.valueOf(HolesToFill.size());
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        FindNewHoles();
    }
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;

        if (HolesToFill.isEmpty())
        {
            if (TogglesOff.getValue())
            {
                SendMessage("We are finished hole filling. toggling");
                toggle();
                return;
            }
            else
                FindNewHoles();
        }

        BlockPos l_PosToFill = null;
        
        for (BlockPos l_Pos : new ArrayList<BlockPos>(HolesToFill))
        {
            if (l_Pos == null)
                continue;

            ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
            
            if (l_Result != ValidResult.Ok)
            {
                /// Remove for next tick.
                HolesToFill.remove(l_Pos);
                continue;
            }
            
            l_PosToFill = l_Pos;
            break;
        }

        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        
        if (l_PosToFill != null && slot != -1)
        {
            int lastSlot;
            lastSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
            
            p_Event.cancel();
            float[] rotations = BlockInteractionHelper
                    .getLegitRotations(new Vec3d(l_PosToFill.getX(), l_PosToFill.getY(), l_PosToFill.getZ()));
            PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
            if (BlockInteractionHelper.place(l_PosToFill, Radius.getValue(), false, false) == BlockInteractionHelper.PlaceResult.Placed)
            {
                /// Remove for next tick.
                HolesToFill.remove(l_PosToFill);
            }
            Finish(lastSlot);
        }
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;

        for (BlockPos l_Pos : new ArrayList<BlockPos>(HolesToFill))
        {
            if (l_Pos == null)
                continue;

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
        }
    });

    public boolean IsProcessing()
    {
        return !HolesToFill.isEmpty();
    }

    private void Finish(int p_LastSlot)
    {
        if (!slotEqualsBlock(p_LastSlot, Blocks.OBSIDIAN))
        {
            mc.player.inventory.currentItem = p_LastSlot;
        }
        mc.playerController.updateController();
    }

    public boolean hasStack(Block type)
    {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private int findStackHotbar(Block type)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    private Hole.HoleTypes isBlockValid(IBlockState blockState, BlockPos blockPos)
    {
        if (blockState.getBlock() != Blocks.AIR)
            return Hole.HoleTypes.None;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return Hole.HoleTypes.None;

        if (mc.world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) // ensure the area is
            // tall enough for
            // the player
            return Hole.HoleTypes.None;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return Hole.HoleTypes.None;

        final BlockPos[] touchingBlocks = new BlockPos[]
                { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west() };

        boolean l_Bedrock = true;
        boolean l_Obsidian = true;

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
            {
                validHorizontalBlocks++;

                if (touchingState.getBlock() != Blocks.BEDROCK && l_Bedrock)
                    l_Bedrock = false;

                if (!l_Bedrock)
                {
                    if (touchingState.getBlock() != Blocks.OBSIDIAN && touchingState.getBlock() != Blocks.BEDROCK)
                        l_Obsidian = false;
                }
            }
        }

        if (validHorizontalBlocks < 4)
            return Hole.HoleTypes.None;

        if (l_Bedrock)
            return Hole.HoleTypes.Bedrock;
        if (l_Obsidian)
            return Hole.HoleTypes.Obsidian;

        return Hole.HoleTypes.Normal;
    }

    public void FindNewHoles()
    {
        HolesToFill.clear();

        float l_Radius = Radius.getValue();

        int l_Holes = 0;

        for (BlockPos l_Pos : BlockInteractionHelper.getSphere( PlayerUtil.GetLocalPlayerPosFloored(), l_Radius, (int) l_Radius,false, true, 0))
        {
            Hole.HoleTypes l_Type = isBlockValid(mc.world.getBlockState(l_Pos), l_Pos);

            switch (l_Type)
            {
                case None:
                    break;
                case Normal:
                case Obsidian:
                case Bedrock:
                    HolesToFill.add(l_Pos);
                    if (++l_Holes >= MaxHoles.getValue())
                        break;
                    break;
            }
        }

        VerifyHoles();
    }

    private void VerifyHoles()
    {
        /// Here we verify the holes to fill. (Copy arraylist so se can remove from original arraylist)
        for (BlockPos l_Pos : new ArrayList<BlockPos>(HolesToFill))
        {
            ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);

            if (l_Result != ValidResult.Ok)
                HolesToFill.remove(l_Pos);
        }
    }

    private void Render(final AxisAlignedBB bb, float p_Red, float p_Green, float p_Blue, float p_Alpha)
    {
        switch (HoleMode.getValue())
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
