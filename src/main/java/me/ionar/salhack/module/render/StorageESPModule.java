package me.ionar.salhack.module.render;

import static me.ionar.salhack.util.render.ESPUtil.RenderOutline;

import java.util.ArrayList;
import java.util.List;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class StorageESPModule extends Module
{
    public final Value<Boolean> EnderChests = new Value<Boolean>("EnderChests", new String[] { "S" }, "Highlights EnderChests", true);
    public final Value<Boolean> Chests = new Value<Boolean>("Chests", new String[] { "S" }, "Highlights Chests", true);
    public final Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[] { "S" }, "Highlights Shulkers", true);

    public StorageESPModule()
    {
        super("StorageESP", new String[] {""}, "Highlights different kind of storages", "NONE", -1, ModuleType.RENDER);
    }

    public final List<StorageBlockPos> Storages = new ArrayList<>();
    private ICamera camera = new Frustum();

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        new Thread(() -> 
        {
            Storages.clear();
    
            mc.world.loadedTileEntityList.forEach(p_Tile ->
            {
                if (p_Tile instanceof TileEntityEnderChest && EnderChests.getValue())
                    Storages.add(new StorageBlockPos(p_Tile.getPos().getX(), p_Tile.getPos().getY(), p_Tile.getPos().getZ(), StorageType.Ender));
                else if (p_Tile instanceof TileEntityChest && Chests.getValue())
                    Storages.add(new StorageBlockPos(p_Tile.getPos().getX(), p_Tile.getPos().getY(), p_Tile.getPos().getZ(), StorageType.Chest));
                else if (p_Tile instanceof TileEntityShulkerBox && Shulkers.getValue())
                    Storages.add(new StorageBlockPos(p_Tile.getPos().getX(), p_Tile.getPos().getY(), p_Tile.getPos().getZ(), StorageType.Shulker));
            });
        }).start();
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        new ArrayList<StorageBlockPos>(Storages).forEach(p_Pos ->
        {
            final AxisAlignedBB bb = new AxisAlignedBB(p_Pos.getX() - mc.getRenderManager().viewerPosX, p_Pos.getY() - mc.getRenderManager().viewerPosY,
                    p_Pos.getZ() - mc.getRenderManager().viewerPosZ, p_Pos.getX() + 1 - mc.getRenderManager().viewerPosX, p_Pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    p_Pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ)))
            {
                GlStateManager.pushMatrix();
                switch (p_Pos.GetType())
                {
                    case Chest:
                        RenderOutline(p_Event, p_Pos, 0.94f, 1.0f, 0f, 0.6f);
                        break;
                    case Ender:
                        RenderOutline(p_Event, p_Pos, 0.65f, 0f, 0.93f, 0.6f);
                        break;
                    case Shulker:
                        RenderOutline(p_Event, p_Pos, 1.0f, 0.0f, 0.59f, 0.6f);
                        break;
                    default:
                        break;
                }

                GlStateManager.popMatrix();
            }
        });
    });
    
    public enum StorageType
    {
        Chest,
        Shulker,
        Ender,
    }
    
    public class StorageBlockPos extends BlockPos
    {
        public StorageType Type;

        public StorageBlockPos(int x, int y, int z, StorageType p_Type)
        {
            super(x, y, z);
            
            Type = p_Type;
        }
        
        public StorageType GetType()
        {
            return Type;
        }
    }
}
