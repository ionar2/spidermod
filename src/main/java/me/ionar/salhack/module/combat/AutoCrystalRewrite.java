package me.ionar.salhack.module.combat;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.EntityManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.misc.AutoMendArmorModule;
import me.ionar.salhack.util.CrystalUtils;
import me.ionar.salhack.util.Timer; 
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class AutoCrystalRewrite extends Module
{
    public static final Value<BreakModes> BreakMode = new Value<BreakModes>("BreakMode", new String[] {"BM"}, "Mode of breaking to use", BreakModes.Always);
    public static final Value<Float> PlaceRadius = new Value<Float>("PlaceRadius", new String[] {""}, "Radius for placing", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> BreakRadius = new Value<Float>("BreakRadius", new String[] {""}, "Radius for BreakRadius", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> WallsRange = new Value<Float>("WallsRange", new String[] {""}, "Max distance through walls", 3.5f, 0.0f, 5.0f, 0.5f);
    
    public static final Value<Float> MinDMG = new Value<Float>("MinDMG", new String[] {""}, "Minimum damage to do to your opponent", 4.0f, 0.0f, 20.0f, 1f);
    public static final Value<Float> MaxSelfDMG = new Value<Float>("MaxSelfDMG", new String[] {""}, "Max self dmg for breaking crystals that will deal tons of dmg", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> FacePlace = new Value<Float>("FacePlace", new String[] {""}, "Required target health for faceplacing", 8.0f, 0.0f, 20.0f, 0.5f);
    public static final Value<Boolean> AutoSwitch = new Value<Boolean>("AutoSwitch", new String[] {""}, "Automatically switches to crystals in your hotbar", true);
    public static final Value<Boolean> PauseIfHittingBlock = new Value<Boolean>("PauseIfHittingBlock", new String[] {""}, "Pauses when your hitting a block with a pickaxe", false);
    public static final Value<Boolean> PauseWhileEating = new Value<Boolean>("PauseWhileEating", new String[] {"PauseWhileEating"}, "Pause while eating", false);
    public static final Value<Boolean> NoSuicide = new Value<Boolean>("NoSuicide", new String[] {"NS"}, "Doesn't commit suicide/pop if you are going to take fatal damage from self placed crystal", true);
     
    public static final Value<Boolean> Render = new Value<Boolean>("Render", new String[] {"Render"}, "Allows for rendering of block placements", true);
    public static final Value<Integer> Red = new Value<Integer>("Red", new String[] {"Red"}, "Red for rendering", 0x33, 0, 255, 5);
    public static final Value<Integer> Green = new Value<Integer>("Green", new String[] {"Green"}, "Green for rendering", 0xFF, 0, 255, 5);
    public static final Value<Integer> Blue = new Value<Integer>("Blue", new String[] {"Blue"}, "Blue for rendering", 0xF3, 0, 255, 5);
    public static final Value<Integer> Alpha = new Value<Integer>("Alpha", new String[] {"Alpha"}, "Alpha for rendering", 0x99, 0, 255, 5);
    
    public enum BreakModes
    {
        Always,
        Smart,
        OnlyOwn
    }
    
    public AutoCrystalRewrite()
    {
        super("AutoCrystalRewrite", new String[] {"AutoCrystal2"}, "Automatically places and destroys crystals", "NONE", -1, ModuleType.COMBAT);
    }
    
    private static AutoCrystalRewrite Mod = null;
    public static Timer _removeVisualTimer = new Timer();
    private Timer _rotationResetTimer = new Timer();
    public static List<BlockPos> _placedCrystals = new CopyOnWriteArrayList<>();
    private ICamera camera = new Frustum();
    private double[] _rotations = null;
    
    // Modules used for pausing
    private SurroundModule _surround = null;
    private AutoTrapFeet _autoTrapFeet = null;
    private AutoMendArmorModule _autoMend = null;
    private SelfTrapModule _selfTrap = null;
    private HoleFillerModule _holeFiller = null;
    private AutoCityModule _autoCity = null;
    
    @Override
    public void init()
    {
        Mod = this;
        
        // initalize the mods as needed
        _surround = (SurroundModule)ModuleManager.Get().GetMod(SurroundModule.class);
        _autoTrapFeet = (AutoTrapFeet)ModuleManager.Get().GetMod(AutoTrapFeet.class);
        _autoMend = (AutoMendArmorModule)ModuleManager.Get().GetMod(AutoMendArmorModule.class);
        _selfTrap = (SelfTrapModule)ModuleManager.Get().GetMod(SelfTrapModule.class);
        _holeFiller = (HoleFillerModule)ModuleManager.Get().GetMod(HoleFillerModule.class);
        _autoCity = (AutoCityModule)ModuleManager.Get().GetMod(AutoCityModule.class);
    }
    
    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        // clear placed crystals, we don't want to display them later on
        _placedCrystals.clear();
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
    }
    
    @Override
    public String getMetaData()
    {
        // display our target name
        return EntityManager.Get().GetLastTarget();
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        // we only want to run this event on pre motion, but don't reset rotations here
        if (event.getEra() != Era.PRE)
            return;
        
        if (event.isCancelled())
        {
            _rotations = null;
            return;
        }
        
        // if the previous event isn't cancelled, or if we don't need to pause.
        if (NeedPause())
        {
            _rotations = null;
            return;
        }
        
        // in order to not flag NCP, we don't want to reset our pitch after we have nothing to do, so do it every second. more legit
        if (_rotationResetTimer.passed(1000))
        {
            _rotations = null;
        }
        
        // Get nearest crystal to the player, we will need to null check this on the timer.
        EntityEnderCrystal crystal = EntityManager.Get().GetNearestCrystalTo(mc.player);
        
        // get a valid crystal in range, and check if it's in break radius
        boolean isValidCrystal = crystal != null ? mc.player.getDistance(crystal) < BreakRadius.getValue() : false;
        List<BlockPos> selectedPositions = EntityManager.Get().GetPlaceLocations();
        
        if (!isValidCrystal && selectedPositions.isEmpty() && _rotations != null)
        {
            event.cancel();
            PlayerUtil.PacketFacePitchAndYaw((float)_rotations[1], (float)_rotations[0]);
            return;
        }

        if (isValidCrystal) // we are checking null here because we don't want to waste time not destroying crystals right away
        {
            // get facing rotations to the crystal
            final double rotations[] =  EntityUtil.calculateLookAt(crystal.posX + 0.5, crystal.posY - 0.5, crystal.posZ + 0.5, mc.player);
            
            // send custom packets, the event is already cancelled by the mod itself
            event.cancel();
            PlayerUtil.PacketFacePitchAndYaw((float)rotations[1], (float)rotations[0]);
            
            _rotations = rotations;
            _rotationResetTimer.reset();
            
            // swing arm and attack the entity
            mc.playerController.attackEntity(mc.player, crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            EntityManager.Get().AddAttackedCrystal(crystal);
        }
        else if (isValidCrystal) // don't place more crystals, there is some to destroy at the next time.
        {
            if (_rotations != null)
            {
                event.cancel();
                PlayerUtil.PacketFacePitchAndYaw((float)_rotations[1], (float)_rotations[0]);
                return;
            }
        }
        
        // verify the placeTimer is ready, selectedPosition is not 0,0,0 and the event isn't already cancelled
        if (!selectedPositions.isEmpty() && !event.isCancelled())
        {
            // auto switch
            if (AutoSwitch.getValue())
            {
                if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                {
                    if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL)
                    {
                        for (int i = 0; i < 9; ++i)
                        {
                            ItemStack stack = mc.player.inventory.getStackInSlot(i);
                            
                            if (!stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL)
                            {
                                mc.player.inventory.currentItem = i;
                                mc.playerController.updateController();
                                break;
                            }
                        }
                    }
                }
            }
            
            // no need to process the code below if we are not using offhand crystal or main hand crystal
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                return;
            
            BlockPos selectedPos = null;
            
            for (BlockPos pos : selectedPositions)
            {
                if (CrystalUtils.canPlaceCrystal(pos))
                {
                    selectedPos = pos;
                    break;
                }
                //else
                    //SendMessage("invalid position, moving to next: list size is " + selectedPositions.size());
            }
            
            if (selectedPos == null)
            {
                if (_rotations != null)
                {
                    event.cancel();
                    PlayerUtil.PacketFacePitchAndYaw((float)_rotations[1], (float)_rotations[0]);
                    return;
                }
                return;
            }
            
            // get facing rotations to the position
            final double rotations[] =  EntityUtil.calculateLookAt(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5, mc.player);
    
            // cancel and event send custom packets, the event is already cancelled by the mod itself
            event.cancel();
            PlayerUtil.PacketFacePitchAndYaw((float)rotations[1], (float)rotations[0]);
            
            _rotations = rotations;
            _rotationResetTimer.reset();
    
            // create a raytrace between player's position and the selected block position
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5));
    
            // this will allow for bypassing placing through walls
            EnumFacing facing;
    
            if (result == null || result.sideHit == null)
                facing = EnumFacing.UP;
            else
                facing = result.sideHit;
            
            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(selectedPos, facing,
                    mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
            
            // if placedcrystals already contains this position, remove it because we need to have it at the back of the list
            if (_placedCrystals.contains(selectedPos))
                _placedCrystals.remove(selectedPos);
            
            // adds the selectedPos to the back of the placed crystals list
            _placedCrystals.add(selectedPos);
            
            // reset the placed location, we just placed there
            EntityManager.Get().ResetPlaceLocation();
        }
    });
    
    @EventHandler
    private Listener<EventNetworkPacketEvent> OnPacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketSoundEffect)
        {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            if (mc.world == null)
                return;
            
            // we need to remove crystals on this packet, because the server sends packets too slow to remove them
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE)
            {
                // loadedEntityList is not thread safe, create a copy and iterate it
                new ArrayList<Entity>(mc.world.loadedEntityList).forEach(e ->
                {
                    // if it's an endercrystal, within 6 distance, set it to be dead
                    if (e instanceof EntityEnderCrystal)
                        if (e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0)
                            e.setDead();
                    
                    // remove all crystals within 6 blocks from the placed crystals list
                    _placedCrystals.removeIf(p_Pos -> p_Pos.getDistance((int)packet.getX(), (int)packet.getY(), (int)packet.getZ()) <= 6.0);
                });
            }
        }
    });
    

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;
        
        _placedCrystals.forEach(pos ->
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
    
                int color = (Alpha.getValue() << 24) | (Red.getValue() << 16) | (Green.getValue() << 8) | Blue.getValue(); 
                
                RenderUtil.drawBoundingBox(bb, 1.0f, color);
                RenderUtil.drawFilledBox(bb, color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        });
    });
    
    public static boolean IsEnabled()
    {
        return Mod != null && Mod.isEnabled();
    }
    
    public boolean NeedPause()
    {
        /// We need to pause if we have surround enabled, and don't have obsidian
        if (_surround.isEnabled() && !_surround.IsSurrounded(mc.player) && _surround.HasObsidian())
        {
            if (!_surround.ActivateOnlyOnShift.getValue())
                return true;

            if (!mc.gameSettings.keyBindSneak.isKeyDown())
                return true;
        }
        
        if (_autoTrapFeet.isEnabled() && !_autoTrapFeet.IsCurrentTargetTrapped() && _autoTrapFeet.HasObsidian())
            return true;
        
        if (_autoMend.isEnabled())
            return true;
        
        if (_selfTrap.isEnabled() && !_selfTrap.IsSelfTrapped() && _surround.HasObsidian())
            return true;
        
        if (_holeFiller.isEnabled() && _holeFiller.IsProcessing())
            return true;

        if (PauseIfHittingBlock.getValue() && mc.playerController.isHittingBlock && mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)
            return true;
        
        if (_autoCity.isEnabled())
            return true;
        
        return false;
    }
}
