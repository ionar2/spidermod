package me.ionar.salhack.module.render;

import static me.ionar.salhack.util.render.ESPUtil.RenderCSGO;

import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ValueListeners;
import me.ionar.salhack.util.entity.EntityUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

import static me.ionar.salhack.util.render.ESPUtil.*;

public class EntityESPModule extends Module
{
    public final Value<ESPMode> Mode = new Value<ESPMode>("Mode", new String[] {"ESPMode"}, "Mode of rendering to use for ESP", ESPMode.Shader);
    
    /// Entities
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[] { "Players" }, "Highlights players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[] { "Monsters" }, "Highlights Monsters", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[] { "Animals" }, "Highlights Animals", false);
    public final Value<Boolean> Vehicles = new Value<Boolean>("Vehicles", new String[] { "Vehicles" }, "Highlights Vehicles", false);
    public final Value<Boolean> Others = new Value<Boolean>("Others", new String[] { "Others" }, "Highlights Others", false);
    public final Value<Boolean> Items = new Value<Boolean>("Items", new String[] { "Items" }, "Highlights Items", false);
    public final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[] { "Tamed" }, "Highlights Tamed", false);

    private enum ESPMode
    {
        None,
        Outline,
        CSGO,
        Shader,
    }
    
    public EntityESPModule()
    {
        super("EntityESP", new String[] {""}, "Highlights different kind of storages", "NONE", -1, ModuleType.RENDER);

        Mode.Listener = new ValueListeners()
        {
            @Override
            public void OnValueChange(Value p_Val)
            {
                if (mc.world == null)
                    return;
                
                if (p_Val.getValue() == ESPMode.Outline)
                    SendMessage("Outline is not yet implemented!");
                
                UpdateShaders();
            }
        };
        
        ValueListeners l_Listener = new ValueListeners()
        {
            @Override
            public void OnValueChange(Value p_Val)
            {
                if (mc.world == null)
                    return;

                if (Mode.getValue() == ESPMode.Shader)
                    UpdateShaders();
            }
        };
        
        /// Update all of these when the value is changed
        Players.Listener  = l_Listener;
        Monsters.Listener = l_Listener;
        Animals.Listener  = l_Listener;
        Vehicles.Listener = l_Listener;
        Others.Listener   = l_Listener;
        Items.Listener    = l_Listener;
        Tamed.Listener    = l_Listener;
    }

    private ICamera camera = new Frustum();
    
    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        new Thread(() -> 
        {
            
        }).start();
    });

    @EventHandler
    private Listener<RenderEvent> OnRenderEvent = new Listener<>(p_Event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;
        
        GlStateManager.pushMatrix();
        switch (Mode.getValue())
        {
            case CSGO:
                RenderCSGO(camera, this, p_Event);
                break;
                /// Currently broken.
            /*case Outline:
                RenderOutline(camera, p_Event);
                break;*/
            default:
                break;
        }
        GlStateManager.popMatrix();
    });

    @EventHandler
    private Listener<EventEntityAdded> OnEntityAdded = new Listener<>(p_Event ->
    {
        if (Mode.getValue() != ESPMode.Shader)
            return;

        boolean l_SetGlowing = false;
        
        /// TODO: func this
        if (p_Event.GetEntity() instanceof EntityPlayer && Players.getValue())
            l_SetGlowing = true;
        
        if (EntityUtil.isFriendlyMob(p_Event.GetEntity()) && Animals.getValue())
            l_SetGlowing = true;
        
        if (EntityUtil.isHostileMob(p_Event.GetEntity()) && Monsters.getValue())
            l_SetGlowing = true;
        
        if (EntityUtil.IsVehicle(p_Event.GetEntity()) && Vehicles.getValue())
            l_SetGlowing = true;
        
        if (p_Event.GetEntity() instanceof EntityItem && Items.getValue())
            l_SetGlowing = true;
        
        if (p_Event.GetEntity() instanceof EntityEnderCrystal && Others.getValue())
            l_SetGlowing = true;
        
        
        p_Event.GetEntity().setGlowing(l_SetGlowing);
    });

    @EventHandler
    private Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(p_Event ->
    {
        p_Event.GetEntity().setGlowing(false);
    });
    
    private void UpdateShaders()
    {
        /// Try catch, because this can be accessed from another thread while entities are being added/removed.
        mc.world.loadedEntityList.forEach(p_Entity ->
        {
            try
            {
                if (p_Entity != null)
                {
                    boolean l_SetGlowing = false;
                    
                    if (Mode.getValue() == ESPMode.Shader)
                    {
                        /// TODO: func this
                        if (p_Entity instanceof EntityPlayer && Players.getValue())
                            l_SetGlowing = true;
                        
                        if (EntityUtil.isFriendlyMob(p_Entity) && Animals.getValue())
                            l_SetGlowing = true;
                        
                        if (EntityUtil.isHostileMob(p_Entity) && Monsters.getValue())
                            l_SetGlowing = true;
                        
                        if (EntityUtil.IsVehicle(p_Entity) && Vehicles.getValue())
                            l_SetGlowing = true;
                        
                        if (p_Entity instanceof EntityItem && Items.getValue())
                            l_SetGlowing = true;
                        
                        if (p_Entity instanceof EntityEnderCrystal && Others.getValue())
                            l_SetGlowing = true;
                    }
                    
                    p_Entity.setGlowing(l_SetGlowing);
                }
            }
            catch (Exception e)
            {
         //       SendMessage(e.toString());
            }
        });
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();

        mc.world.loadedEntityList.forEach(p_Entity ->
        {
            if (p_Entity != null)
            {
                p_Entity.setGlowing(false);
            }
        });
    }
}
