package me.ionar.salhack.events.render;

import me.zero.alpine.event.type.Cancellable;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public class EventRenderLayers extends Cancellable
{
    private final EntityLivingBase entityLivingBase;

    private final LayerRenderer layerRenderer;
    private float HeadPitch;

    public EventRenderLayers(EntityLivingBase entityLivingBase, LayerRenderer layerRenderer, float headPitch)
    {

        this.entityLivingBase = entityLivingBase;
        this.layerRenderer = layerRenderer;
        HeadPitch = headPitch;
    }

    public EntityLivingBase getEntityLivingBase()
    {

        return entityLivingBase;
    }

    public LayerRenderer getLayerRenderer()
    {
        return layerRenderer;
    }
    
    public float GetHeadPitch()
    {
        return HeadPitch;
    }
    
    public void SetHeadPitch(float p_Pitch)
    {
        HeadPitch = p_Pitch;
    }
}
