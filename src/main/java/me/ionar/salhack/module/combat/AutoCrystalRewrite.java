package me.ionar.salhack.module.combat;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.FriendManager;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
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
    public static final Value<PlaceModes> PlaceMode = new Value<PlaceModes>("PlaceMode", new String[] {"BM"}, "Mode of placing to use", PlaceModes.Most);
    public static final Value<Float> PlaceRadius = new Value<Float>("PlaceRadius", new String[] {""}, "Radius for placing", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> BreakRadius = new Value<Float>("BreakRadius", new String[] {""}, "Radius for BreakRadius", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> WallsRange = new Value<Float>("WallsRange", new String[] {""}, "Max distance through walls", 3.5f, 0.0f, 5.0f, 0.5f);
    public static final Value<Boolean> MultiPlace = new Value<Boolean>("MultiPlace", new String[] {"MultiPlaces"}, "Tries to multiplace", false);
    public static final Value<Integer> Ticks = new Value<Integer>("Ticks", new String[] {"IgnoreTicks"}, "The number of ticks to ignore on client update", 2, 0, 20, 1);
    
    public static final Value<Float> MinDMG = new Value<Float>("MinDMG", new String[] {""}, "Minimum damage to do to your opponent", 4.0f, 0.0f, 20.0f, 1f);
    public static final Value<Float> MaxSelfDMG = new Value<Float>("MaxSelfDMG", new String[] {""}, "Max self dmg for breaking crystals that will deal tons of dmg", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> FacePlace = new Value<Float>("FacePlace", new String[] {""}, "Required target health for faceplacing", 8.0f, 0.0f, 20.0f, 0.5f);
    public static final Value<Boolean> AutoSwitch = new Value<Boolean>("AutoSwitch", new String[] {""}, "Automatically switches to crystals in your hotbar", true);
    public static final Value<Boolean> PauseIfHittingBlock = new Value<Boolean>("PauseIfHittingBlock", new String[] {""}, "Pauses when your hitting a block with a pickaxe", false);
    public static final Value<Boolean> PauseWhileEating = new Value<Boolean>("PauseWhileEating", new String[] {"PauseWhileEating"}, "Pause while eating", false);
    public static final Value<Boolean> NoSuicide = new Value<Boolean>("NoSuicide", new String[] {"NS"}, "Doesn't commit suicide/pop if you are going to take fatal damage from self placed crystal", true);
    public static final Value<Boolean> AntiWeakness = new Value<Boolean>("AntiWeakness", new String[] {"AW"}, "Switches to a sword to try and break crystals", true);
    
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
    
    public enum PlaceModes
    {
        Most,
        Lethal,
    }
    
    public AutoCrystalRewrite()
    {
        super("AutoCrystalRewrite", new String[] {"AutoCrystal2"}, "Automatically places and destroys crystals", "NONE", -1, ModuleType.COMBAT);
    }
    
    private AutoCrystalRewrite Mod = null;
    public static Timer _removeVisualTimer = new Timer();
    private Timer _rotationResetTimer = new Timer();
    private ConcurrentLinkedQueue<BlockPos> _placedCrystals = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<BlockPos, Float> _placedCrystalsDamage = new ConcurrentHashMap<>();
    private ICamera camera = new Frustum();
    private double[] _rotations = null;
    private ConcurrentHashMap<EntityEnderCrystal, Integer> _attackedEnderCrystals = new ConcurrentHashMap<>();
    private final Minecraft mc = Minecraft.getMinecraft();
    private String _lastTarget = null;
    private int _remainingTicks;
    private BlockPos _lastPlaceLocation = BlockPos.ORIGIN;
    
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
    
    // don't allow this to load enabled on startup
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
        _placedCrystalsDamage.clear();
        
        // also reset ticks on enable, we need as much speed as we can get.
        _remainingTicks = 0;
        
        // reset this, we will get a new one
        _lastPlaceLocation = BlockPos.ORIGIN;
    }
    
    @Override
    public String getMetaData()
    {
        // display our target name
        return _lastTarget;
    }

    @EventHandler
    private Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof EntityEnderCrystal)
        {
            // we don't need null things in this list.
            _attackedEnderCrystals.remove((EntityEnderCrystal)event.GetEntity());
        }
    });

    private boolean ValidateCrystal(EntityEnderCrystal e)
    {
        if (e == null || e.isDead)
            return false;
        
        if (_attackedEnderCrystals.containsKey(e) && _attackedEnderCrystals.get(e) > 5)
            return false;
        
        if (e.getDistance(mc.player) > (!mc.player.canEntityBeSeen(e) ? WallsRange.getValue() : BreakRadius.getValue()))
            return false;
        
        switch (BreakMode.getValue())
        {
            case OnlyOwn:
                return e.getDistance(e.posX, e.posY, e.posZ) <= 3;
            case Smart:
                float selfDamage = CrystalUtils.calculateDamage(mc.world, e.posX, e.posY, e.posZ, mc.player, 0);
                
                if (selfDamage > MaxSelfDMG.getValue())
                    return false;
                
                if (NoSuicide.getValue() && selfDamage >= mc.player.getHealth()+mc.player.getAbsorptionAmount())
                    return false;

                // iterate through all players, and crystal positions to find the best position for most damage
                for (EntityPlayer player : mc.world.playerEntities)
                {
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (player == mc.player || FriendManager.Get().IsFriend(player) || mc.player.isDead || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                        continue;
                    
                    // store this as a variable for faceplace per player
                    float minDamage = MinDMG.getValue();
                    
                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (player.getHealth() + player.getAbsorptionAmount() <= FacePlace.getValue())
                        minDamage = 1f;
                    
                    float calculatedDamage = CrystalUtils.calculateDamage(mc.world,  e.posX, e.posY, e.posZ, player, 0);
                    
                    if (calculatedDamage > minDamage)
                        return true;
                }
                return false;
            case Always:
            default:
                break;
        }
        
        return true;
    }
    
    /*
     * Returns nearest crystal to an entity, if the crystal is not null or dead
     * @entity - entity to get smallest distance from
     */
    public EntityEnderCrystal GetNearestCrystalTo(Entity entity)
    {
        return mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityEnderCrystal && ValidateCrystal((EntityEnderCrystal)e)).map(e -> (EntityEnderCrystal)e).min(Comparator.comparing(e -> entity.getDistance(e))).orElse(null);
    }
    
    public void AddAttackedCrystal(EntityEnderCrystal crystal)
    {
        if (_attackedEnderCrystals.containsKey(crystal))
        {
            int value = _attackedEnderCrystals.get(crystal);
            _attackedEnderCrystals.put(crystal, value + 1);
        }
        else
            _attackedEnderCrystals.put(crystal, 1);
    }
    
    private boolean VerifyCrystalBlocks(BlockPos pos)
    {
        // check distance
        if (mc.player.getDistanceSq(pos) > PlaceRadius.getValue()*PlaceRadius.getValue())
            return false;
        
        // check walls range
        if (WallsRange.getValue() > 0)
        {
            if (!PlayerUtil.CanSeeBlock(pos))
                if (pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ) > WallsRange.getValue())
                    return false;
        }
        
        // check self damage
        float selfDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player, 0);
        
        // make sure self damage is not greater than maxselfdamage
        if (selfDamage > MaxSelfDMG.getValue())
            return false;

        // no suicide, verify self damage won't kill us
        if (NoSuicide.getValue() && selfDamage >= mc.player.getHealth()+mc.player.getAbsorptionAmount())
            return false;
        
        // it's an ok position.
        return true;
    }
    
    @EventHandler
    private Listener<EventClientTick> OnClientTick = new Listener<>(event ->
    {
        // this is our 1 second timer to remove our attackedEnderCrystals list, and remove the first placedCrystal for the visualizer.
        if (_removeVisualTimer.passed(1000))
        {
            _removeVisualTimer.reset();
            
            if (!_placedCrystals.isEmpty())
            {
                BlockPos removed = _placedCrystals.remove();
                
                if (removed != null)
                    _placedCrystalsDamage.remove(removed);
            }
            
            _attackedEnderCrystals.clear();
        }

        if (NeedPause())
        {
            _remainingTicks = 0;
            return;
        }

        // override
        if (PlaceMode.getValue() == PlaceModes.Lethal && _lastPlaceLocation != BlockPos.ORIGIN)
        {
            float damage = 0f;
            
            EntityPlayer trappedTarget = null;
            
            // verify that this location will exceed lethal damage for atleast one enemy.
            // iterate through all players, and crystal positions to find the best position for most damage
            for (EntityPlayer player : mc.world.playerEntities)
            {
                // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                if (player == mc.player || FriendManager.Get().IsFriend(player) || mc.player.isDead || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                    continue;
                
                // store this as a variable for faceplace per player
                float minDamage = MinDMG.getValue();
                
                // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                if (player.getHealth() + player.getAbsorptionAmount() <= FacePlace.getValue())
                    minDamage = 1f;
                
                float calculatedDamage = CrystalUtils.calculateDamage(mc.world, _lastPlaceLocation.getX() + 0.5, _lastPlaceLocation.getY() + 1.0, _lastPlaceLocation.getZ() + 0.5, player, 0);
                
                if (calculatedDamage >= minDamage && calculatedDamage > damage)
                {
                    damage = calculatedDamage;
                    trappedTarget = player;
                }
            }
            
            if (damage == 0f || trappedTarget == null)
            {
                // set this back to null
                _lastPlaceLocation = BlockPos.ORIGIN;
            }
        }
        
        
        if (_remainingTicks > 0)
        {
            --_remainingTicks;
        }
        
        boolean skipUpdateBlocks = _lastPlaceLocation != BlockPos.ORIGIN && PlaceMode.getValue() == PlaceModes.Lethal;

        // create a list of available place locations
        ArrayList<BlockPos> placeLocations = new ArrayList<BlockPos>();
        EntityPlayer playerTarget = null;
        
        // if we don't need to skip update, get crystal blocks
        if (!skipUpdateBlocks && _remainingTicks <= 0)
        {
            _remainingTicks = Ticks.getValue();
            
            // this is the most expensive code, we need to get valid crystal blocks. -> todo verify stream to see if it's slower than normal looping.
            final List<BlockPos> cachedCrystalBlocks = CrystalUtils.findCrystalBlocks(mc.player, AutoCrystalRewrite.PlaceRadius.getValue()).stream().filter(pos -> VerifyCrystalBlocks(pos)).collect(Collectors.toList());
            
            // this is where we will iterate through all players (for most damage) and cachedCrystalBlocks
            if (!cachedCrystalBlocks.isEmpty())
            {
                float damage = 0f;
                String target = null;
                
                // iterate through all players, and crystal positions to find the best position for most damage
                for (EntityPlayer player : mc.world.playerEntities)
                {
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (player == mc.player || FriendManager.Get().IsFriend(player) || mc.player.isDead || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                        continue;
                    
                    // store this as a variable for faceplace per player
                    float minDamage = MinDMG.getValue();
                    
                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (player.getHealth() + player.getAbsorptionAmount() <= FacePlace.getValue())
                        minDamage = 1f;
                    
                    // iterate through all valid crystal blocks for this player, and calculate the damages.
                    for (BlockPos pos : cachedCrystalBlocks)
                    {
                        float calculatedDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player, 0);
                        
                        if (calculatedDamage >= minDamage && calculatedDamage > damage)
                        {
                            damage = calculatedDamage;
                            if (!placeLocations.contains(pos))
                                placeLocations.add(pos);
                            target = player.getName();
                            playerTarget = player;
                        }
                    }
                }
                
                // playerTarget can nullptr during client tick
                if (playerTarget != null)
                {
                    // the player could have died during this code run, wait till next tick for doing more calculations.
                    if (playerTarget.isDead || playerTarget.getHealth() <= 0.0f)
                        return;
                    
                    // ensure we have place locations
                    if (!placeLocations.isEmpty())
                    {
                        // store this as a variable for faceplace per player
                        float minDamage = MinDMG.getValue();
                        
                        // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                        if (playerTarget.getHealth() + playerTarget.getAbsorptionAmount() <= FacePlace.getValue())
                            minDamage = 1f;
                        
                        final float finalMinDamage = minDamage;
                        final EntityPlayer finalTarget = playerTarget;
                        
                        // iterate this again, we need to remove some values that are useless, since we iterated all players
                        placeLocations.removeIf(pos -> CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, finalTarget, 0) < finalMinDamage);
                        
                        // at this point, the place locations list is in asc order, we need to reverse it to get to desc
                        Collections.reverse(placeLocations);
                    
                        // store our last target name.
                        _lastTarget = target;
                    }
                }
            }
        }
        
        // at this point, we are going to destroy/place crystals.
        
        // Get nearest crystal to the player, we will need to null check this on the timer.
        EntityEnderCrystal crystal = GetNearestCrystalTo(mc.player);
        
        // get a valid crystal in range, and check if it's in break radius
        boolean isValidCrystal = crystal != null ? mc.player.getDistance(crystal) < BreakRadius.getValue() : false;
        
        // no where to place or break
        if (!isValidCrystal && placeLocations.isEmpty() && !skipUpdateBlocks)
        {
            _remainingTicks = 0;
            return;
        }
        
        if (isValidCrystal && (skipUpdateBlocks ? true : _remainingTicks == Ticks.getValue())) // we are checking null here because we don't want to waste time not destroying crystals right away
        {
            if (AntiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS))
            {
                if (mc.player.getHeldItemMainhand() == ItemStack.EMPTY || (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)))
                {
                    for (int i = 0; i < 9; ++i)
                    {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
                        
                        if (stack.isEmpty())
                            continue;
                        
                        if (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword)
                        {
                            mc.player.inventory.currentItem = i;
                            mc.playerController.updateController();
                            break;
                        }
                    }
                }
            }
            
            // get facing rotations to the crystal
            _rotations = EntityUtil.calculateLookAt(crystal.posX + 0.5, crystal.posY - 0.5, crystal.posZ + 0.5, mc.player);
            _rotationResetTimer.reset();
            
            // swing arm and attack the entity
            mc.playerController.attackEntity(mc.player, crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            AddAttackedCrystal(crystal);
            
            // if we are not multiplacing return here, we have something to do for this tick.
            if (!MultiPlace.getValue())
                return;
        }
        
        // verify the placeTimer is ready, selectedPosition is not 0,0,0 and the event isn't already cancelled
        if (!placeLocations.isEmpty() || skipUpdateBlocks)
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
            
            // no need to process the code below if we are not using off hand crystal or main hand crystal
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                return;
            
            BlockPos selectedPos = null;
            
            // iterate through available place locations
            if (!skipUpdateBlocks)
            {
                for (BlockPos pos : placeLocations)
                {
                    // verify we can still place crystals at this location, if we can't we try next location
                    if (CrystalUtils.canPlaceCrystal(pos))
                    {
                        selectedPos = pos;
                        break;
                    }
                }
            }
            else
                selectedPos = _lastPlaceLocation;
            
            // nothing found... this is bad, wait for next tick to correct it
            if (selectedPos == null)
            {
                _remainingTicks = 0;
                return;
            }
            
            // get facing rotations to the position, store them for the motion tick to handle it
            _rotations = EntityUtil.calculateLookAt(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5, mc.player);
            _rotationResetTimer.reset();
    
            // create a raytrace between player's position and the selected block position
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5));
    
            // this will allow for bypassing placing through walls afaik
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
            
            if (playerTarget != null)
            {
                float calculatedDamage = CrystalUtils.calculateDamage(mc.world, selectedPos.getX() + 0.5, selectedPos.getY() + 1.0, selectedPos.getZ() + 0.5, playerTarget, 0);
                
                _placedCrystalsDamage.put(selectedPos, calculatedDamage);
            }
            
            if (_lastPlaceLocation != BlockPos.ORIGIN && _lastPlaceLocation == selectedPos)
            {
                // reset ticks, we don't need to do more rotations for this position, so we can crystal faster.
                if (PlaceMode.getValue() == PlaceModes.Lethal)
                    _remainingTicks = 0;
            }
            else // set this to our last place location
                _lastPlaceLocation = selectedPos;
        }
    });
    
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

        // rotations are valid, cancel this update and use our custom rotations instead.
        if (_rotations != null)
        {
            event.cancel();
            PlayerUtil.PacketFacePitchAndYaw((float)_rotations[1], (float)_rotations[0]);
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

                if (_placedCrystalsDamage.containsKey(pos))
                {
                    GlStateManager.pushMatrix();
                    RenderUtil.glBillboardDistanceScaled((float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f, mc.player, 1);
                    final float damage = _placedCrystalsDamage.get(pos);
                    final String damageText = (Math.floor(damage) == damage ? (int) damage : String.format("%.1f", damage)) + "";
                    GlStateManager.disableDepth();
                    GlStateManager.translate(-(RenderUtil.getStringWidth(damageText) / 2.0d), 0, 0);
                    RenderUtil.drawStringWithShadow(damageText, 0, 0, -1);
                    GlStateManager.popMatrix();
                }
            }
        });
    });
    
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
