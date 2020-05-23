package me.ionar.salhack.managers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.combat.AutoCrystalRewrite;
import me.ionar.salhack.util.CrystalUtils;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import static me.ionar.salhack.module.combat.AutoCrystalRewrite.*;

public class EntityManager implements Listenable
{
    private List<EntityEnderCrystal> _enderCrystals = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<EntityEnderCrystal, Integer> _attackedEnderCrystals = new ConcurrentHashMap<>();
    private List<BlockPos> _cachedCrystalPositions = new CopyOnWriteArrayList<>();
    private final Minecraft mc = Minecraft.getMinecraft();
    private List<BlockPos> _placeLocations = new CopyOnWriteArrayList<>();
    private String _lastTarget = null;
    
    public EntityManager()
    {
        SalHackMod.EVENT_BUS.subscribe(this);
    }
    
    @EventHandler
    private Listener<EventEntityAdded> OnEntityAdded = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof EntityEnderCrystal)
            _enderCrystals.add((EntityEnderCrystal)event.GetEntity());
    });

    @EventHandler
    private Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof EntityEnderCrystal)
        {
            _enderCrystals.remove((EntityEnderCrystal)event.GetEntity());
            _attackedEnderCrystals.remove((EntityEnderCrystal)event.GetEntity());
        }
    });
    
    private boolean ValidateCrystal(EntityEnderCrystal e)
    {
        if (e == null || e.isDead)
            return false;
        
        if (_attackedEnderCrystals.containsKey(e) && _attackedEnderCrystals.get(e) > 3)
            return false;
        
        if (e.getDistance(mc.player) > (!mc.player.canEntityBeSeen(e) ? WallsRange.getValue() : BreakRadius.getValue()))
            return false;
        
        return true;
    }
    
    /*
     * Returns nearest crystal to an entity, if the crystal is not null or dead
     * @entity - entity to get smallest distance from
     */
    public EntityEnderCrystal GetNearestCrystalTo(Entity entity)
    {
        return _enderCrystals.stream().filter(e -> ValidateCrystal(e)).min(Comparator.comparing(e -> entity.getDistance(e))).orElse(null);
    }
    
    public final List<BlockPos> GetCachedCrystalPositions()
    {
        return _cachedCrystalPositions;
    }

    public void Update()
    {
        if (Wrapper.GetMC().world == null)
        {
            _enderCrystals.clear();
            return;
        }
            
        _enderCrystals.removeAll(Collections.singleton(null));
        
        if (Wrapper.GetMC().player != null)
            _cachedCrystalPositions = CrystalUtils.findCrystalBlocks(Wrapper.GetMC().player, AutoCrystalRewrite.PlaceRadius.getValue());
        
      //  if (!IsEnabled())
      //      return;
        
        if (_removeVisualTimer.passed(1000))
        {
            _removeVisualTimer.reset();
            
            if (!_placedCrystals.isEmpty())
                _placedCrystals.remove(0);
            
            _attackedEnderCrystals.clear();
        }
        
        //if (_placeTimer.passed(PlaceDelay.getValue()))
        {
            // get all crystal blocks in cache within 169 squared position to us
            final List<BlockPos> cachedCrystalBlocks = _cachedCrystalPositions.stream().filter(pos -> mc.player.getDistanceSq(pos) < PlaceRadius.getValue()*PlaceRadius.getValue()).collect(Collectors.toList());
            
            if (!cachedCrystalBlocks.isEmpty())
            {
                float damage = 0f;
                String target = null;
                EntityPlayer playerTarget = null;
                
                // iterate through all players, and crystal positions to find the best position for most damage
                for (EntityPlayer player : mc.world.playerEntities)
                {
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (player == mc.player || FriendManager.Get().IsFriend(player) || mc.player.isDead || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                        continue;
                    
                    // store this as a variable for faceplace per player
                    float minDamage = MinDMG.getValue();
                    
                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= FacePlace.getValue())
                        minDamage = 1f;
                    
                    for (BlockPos pos : cachedCrystalBlocks)
                    {
                        float selfDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player, 0);
                        
                        if (selfDamage > MaxSelfDMG.getValue())
                            continue;
    
                        if (WallsRange.getValue() > 0)
                        {
                            if (!PlayerUtil.CanSeeBlock(pos))
                                if (pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ) > WallsRange.getValue())
                                    continue;
                        }
                        
                        float calculatedDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player, 0);
                        
                        if (calculatedDamage >= minDamage && calculatedDamage > damage)
                        {
                            damage = calculatedDamage;
                            if (!_placeLocations.contains(pos))
                                _placeLocations.add(pos);
                            target = player.getName();
                            playerTarget = player;
                        }
                    }
                }
                
                if (playerTarget != null)
                {
                    if (!_placeLocations.isEmpty())
                    {
                        // at this point, the place locations list is in asc order, we need to reverse it to get to desc
                        Collections.reverse(_placeLocations);
                    
                        _lastTarget = target;
                    }
                }
            }
        }
    }

    public final List<BlockPos> GetPlaceLocations()
    {
        return _placeLocations;
    }
    
    public void ResetPlaceLocation()
    {
        _placeLocations.clear();
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
    
    public final String GetLastTarget()
    {
        return _lastTarget;
    }
    
    public static EntityManager Get()
    {
        return SalHack.GetEntityManager();
    }
}
