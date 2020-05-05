package me.ionar.salhack.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Hole;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.ionar.salhack.util.Hole.HoleTypes;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class ESPModule extends Module
{
    public final Value<Boolean> Holes = new Value<Boolean>("Holes", new String[]
    { "H" }, "Shows holes for crystal pvp", true);
    public final Value<Integer> radius = new Value<Integer>("HoleRadius", new String[]
    { "Radius", "Range", "Distance" }, "Radius in blocks to scan for holes.", 8, 0, 32, 1);
    public final Value<Boolean> Void = new Value<Boolean>("Void", new String[]
    { "V" }, "Shows holes that are void esp", true);
    public final Value<Boolean> Storages = new Value<Boolean>("Storages", new String[]
    { "S" }, "Highlights storages", true);
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]
    { "Players" }, "Highlights players", true);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]
    { "Animals" }, "Highlights Animals", false);
    public final Value<Boolean> Mobs = new Value<Boolean>("Mobs", new String[]
    { "Mobs" }, "Highlights Mobs", false);

    public final Value<Float> ObsidianRed = new Value<Float>("ObsidianRed", new String[] {"oRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianGreen = new Value<Float>("ObsidianGreen", new String[] {"oGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianBlue = new Value<Float>("ObsidianBlue", new String[] {"oBlue"}, "Blue for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> ObsidianAlpha = new Value<Float>("ObsidianAlpha", new String[] {"oAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);

    public final Value<Float> BedrockRed = new Value<Float>("BedrockRed", new String[] {"bRed"}, "Red for rendering", 0f, 0f, 1.0f, 0.1f);
    public final Value<Float> BedrockGreen = new Value<Float>("BedrockGreen", new String[] {"bGreen"}, "Green for rendering", 1f, 0f, 1.0f, 0.1f);
    public final Value<Float> BedrockBlue = new Value<Float>("BedrockBlue", new String[] {"bBlue"}, "Blue for rendering", 0.8f, 0f, 1.0f, 0.1f);
    public final Value<Float> BedrockAlpha = new Value<Float>("BedrockAlpha", new String[] {"bAlpha"}, "Alpha for rendering", 0.5f, 0f, 1.0f, 0.1f);
    
    public final Value<HoleModes> HoleMode = new Value<HoleModes>("HoleModed", new String[] {"HM"}, "Mode for rendering holes", HoleModes.FlatOutline);
    
    private enum HoleModes
    {
    	FlatOutline,
    	Flat,
    	Outline,
    	Full,
    }
    
    public ESPModule()
    {
        super("ESP", new String[]
        { "HoleESP" }, "Highlights entities or blocks", "NONE", 0x75DB24, ModuleType.RENDER);
    }

    public final List<Hole> holes = new ArrayList<>();
    public final List<BlockPos> VoidBlocks = new ArrayList<>();

    private ICamera camera = new Frustum();

    public final List<Hole> GetHoles()
    {
        return holes;
    }

    @EventHandler
    private Listener<EventPlayerUpdate> PacketEvent = new Listener<>(p_Event ->
    {
        this.holes.clear();
        VoidBlocks.clear();

        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (int x = playerPos.getX() - radius.getValue(); x < playerPos.getX() + radius.getValue(); x++)
        {
            for (int z = playerPos.getZ() - radius.getValue(); z < playerPos.getZ() + radius.getValue(); z++)
            {
                for (int y = playerPos.getY() + 4; y > playerPos.getY() - 4; y--)
                {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final IBlockState blockState = mc.world.getBlockState(blockPos);

                    HoleTypes l_Type = isBlockValid(blockState, blockPos);

                    if (l_Type != HoleTypes.None)
                    {
                        final IBlockState downBlockState = mc.world.getBlockState(blockPos.down());
                        if (downBlockState.getBlock() == Blocks.AIR)
                        {
                            final BlockPos downPos = blockPos.down();

                            l_Type = isBlockValid(downBlockState, blockPos);

                            if (l_Type != HoleTypes.None)
                            {
                                this.holes.add(new Hole(downPos.getX(), downPos.getY(), downPos.getZ(), downPos, l_Type, true));
                            }
                        }
                        else
                        {
                            this.holes.add(new Hole(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos, l_Type));
                        }
                    }

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
        
        if (Holes.getValue())
        {
            for (Hole hole : this.holes)
            {
                switch (hole.GetHoleType())
                {
                    case Bedrock:
                    case Obsidian:
                        break;
                    default:
                        continue;
                }

                final AxisAlignedBB bb = new AxisAlignedBB(hole.getX() - mc.getRenderManager().viewerPosX, hole.getY() - mc.getRenderManager().viewerPosY,
                        hole.getZ() - mc.getRenderManager().viewerPosZ, hole.getX() + 1 - mc.getRenderManager().viewerPosX, hole.getY() + (hole.isTall() ? 2 : 1) - mc.getRenderManager().viewerPosY,
                        hole.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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

                    switch (hole.GetHoleType())
                    {
                        case Bedrock:
                        	Render(bb, BedrockRed.getValue(), BedrockGreen.getValue(), BedrockBlue.getValue(), BedrockAlpha.getValue());
                            break;
                        case Obsidian:
                        	Render(bb, ObsidianRed.getValue(), ObsidianGreen.getValue(), ObsidianBlue.getValue(), ObsidianAlpha.getValue());
                            break;
                        default:
                            break;
                    }

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        }

        if (Void.getValue())
        {
            for (BlockPos l_Pos : VoidBlocks)
            {
                final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX, l_Pos.getY() - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() - mc.getRenderManager().viewerPosZ, l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, l_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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
            }
        }

        mc.world.loadedTileEntityList.forEach(p_Tile ->
        {
            if (p_Tile instanceof TileEntityChest && Storages.getValue())
            {
                BlockPos l_Pos = p_Tile.getPos();

                final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX, l_Pos.getY() - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() - mc.getRenderManager().viewerPosZ, l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, l_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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

                    RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 1.0f, 0, 1, 1.0f);

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }

            if (p_Tile instanceof TileEntityEnderChest && Storages.getValue())
            {
                BlockPos l_Pos = p_Tile.getPos();

                final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX, l_Pos.getY() - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() - mc.getRenderManager().viewerPosZ, l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, l_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }

            if (p_Tile instanceof TileEntityShulkerBox && Storages.getValue())
            {
                BlockPos l_Pos = p_Tile.getPos();

                final AxisAlignedBB bb = new AxisAlignedBB(l_Pos.getX() - mc.getRenderManager().viewerPosX, l_Pos.getY() - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() - mc.getRenderManager().viewerPosZ, l_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, l_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        l_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

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

                    RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 1.0f, 1, 1, 0.50f);
                    RenderGlobal.renderFilledBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 1.0f, 1, 1, 0.22f);

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        });

        boolean isThirdPersonFrontal = mc.getRenderManager().options.thirdPersonView == 2;
        float viewerYaw = mc.getRenderManager().playerViewY;

        mc.world.loadedEntityList.stream().filter(EntityUtil::isLiving).filter(entity -> mc.player != entity).map(entity -> (EntityLivingBase) entity)
                .filter(entityLivingBase -> !entityLivingBase.isDead)
                .filter(entity -> (Players.getValue() && entity instanceof EntityPlayer) || (EntityUtil.isPassive(entity) ? Animals.getValue() : Mobs.getValue())).forEach(entity ->
                {
                    GL11.glPushMatrix();
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glLineWidth(1.0F);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                    GL11.glDepthMask(false);
                    
                    Color c = new Color(0xFF00FF);
                    
                    GL11.glColor4d(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 0.15F);
                    RenderUtil.drawColorBox(new AxisAlignedBB(
                            entity.getEntityBoundingBox().minX - 0.05 - entity.posX
                                    + (entity.posX - mc.getRenderManager().renderPosX),
                            entity.getEntityBoundingBox().minY - entity.posY + (entity.posY - mc.getRenderManager().renderPosY),
                            entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ
                                    + (entity.posZ - mc.getRenderManager().renderPosZ),
                            entity.getEntityBoundingBox().maxX + 0.05 - entity.posX
                                    + (entity.posX - mc.getRenderManager().renderPosX),
                            entity.getEntityBoundingBox().maxY + 0.1 - entity.posY
                                    + (entity.posY - mc.getRenderManager().renderPosY),
                            entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ
                                    + (entity.posZ - mc.getRenderManager().renderPosZ)),
                            0F, 0F, 0F, 0F);
                    GL11.glColor4d(0, 0, 0, 0.5);
                    GL11.glLineWidth(2.0F);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                    GL11.glDepthMask(true);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                    GL11.glColor4f(1f, 1f, 1f, 1f);
                });
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();
        GlStateManager.enableCull();
        GlStateManager.glLineWidth(1);
        GL11.glColor3f(1, 1, 1);
    });

    private boolean IsVoidHole(BlockPos blockPos, IBlockState blockState)
    {
        if (blockPos.getY() > 4 || blockPos.getY() <= 0)
            return false;

        BlockPos l_Pos = blockPos;

        for (int l_I = blockPos.getY(); l_I >= 0; --l_I)
        {
            if (mc.world.getBlockState(l_Pos).getBlock() != Blocks.AIR)
                return false;

            l_Pos = l_Pos.down();
        }

        return true;
    }

    private HoleTypes isBlockValid(IBlockState blockState, BlockPos blockPos)
    {
        if (blockState.getBlock() != Blocks.AIR)
            return HoleTypes.None;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return HoleTypes.None;

        if (mc.world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) // ensure the area is
                                                                             // tall enough for
                                                                             // the player
            return HoleTypes.None;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return HoleTypes.None;

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
            return HoleTypes.None;

        if (l_Bedrock)
            return HoleTypes.Bedrock;
        if (l_Obsidian)
            return HoleTypes.Obsidian;

        return HoleTypes.Normal;
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
