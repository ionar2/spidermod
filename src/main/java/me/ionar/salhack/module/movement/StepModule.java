package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.player.EventPlayerUpdateMoveState;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;

public class StepModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"M"}, "The mode used for step on different types of servers", Modes.Normal);
    public final Value<Boolean> EntityStep = new Value<Boolean>("EntityStep", new String[] {"ES"}, "Modifies your riding entity to max step height", false);
    public final Value<Float> Height = new Value<Float>("Height", new String[] {"H"}, "Modifier of height", 1.0f, 0.0f, 10.0f, 1.0f);
    
    public enum Modes
    {
        Normal, AAC
    }

    public StepModule()
    {
        super("Step", new String[] {"Spider", "NCPStep", "Stairstep"}, "Allows you to walk up blocks like stairs", "NONE", -1, ModuleType.MOVEMENT);
    }

    private double previousX, previousY, previousZ;
    private double offsetX, offsetY, offsetZ;
    private double frozenX, frozenZ;
    private byte cancelStage;
    private float _prevEntityStep;

    @Override
    public void onEnable()
    {
        super.onEnable();
        cancelStage = 0;
        
        if (mc.player != null && mc.player.isRiding())
            _prevEntityStep = mc.player.getRidingEntity().stepHeight;
    }
    
    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.player.stepHeight = 0.5F;

        if (mc.player.isRiding())
            mc.player.getRidingEntity().stepHeight = _prevEntityStep;
    }
    
    @Override
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }

    @EventHandler
    private Listener<EventPlayerUpdateMoveState> onInputUpdate = new Listener<>(event ->
    {
        if (cancelStage != 0 && Mode.getValue() == Modes.AAC)
            mc.player.movementInput.jump = false;
        
        if (EntityStep.getValue() && mc.player.isRiding())
        {
            mc.player.getRidingEntity().stepHeight = 256f;
        }
    });
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnMotionUpdate = new Listener<>(p_Event ->
    {
        switch (Mode.getValue())
        {
            case AAC:
                if (p_Event.getEra() == Era.PRE)
                {
                    offsetX = 0;
                    offsetY = 0;
                    offsetZ = 0;
    
                    mc.player.stepHeight = mc.player.onGround && mc.player.collidedHorizontally && cancelStage == 0 && mc.player.posY % 1 == 0 ? 1.1F : 0.5F;
    
                    if (cancelStage == -1)
                    {
                        cancelStage = 0;
                        return;
                    }
    
                    double yDist = mc.player.posY - previousY;
                    double hDistSq = (mc.player.posX - previousX) * (mc.player.posX - previousX) + (mc.player.posZ - previousZ) * (mc.player.posZ - previousZ);
    
                    if (yDist > 0.5 && yDist < 1.05 && hDistSq < 1 && cancelStage == 0)
                    {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(previousX, previousY + 0.42, previousZ, false));
                        offsetX = previousX - mc.player.posX;
                        offsetY = 0.755 - yDist;
                        offsetZ = previousZ - mc.player.posZ;
    
                        frozenX = previousX;
                        frozenZ = previousZ;
                        mc.player.stepHeight = 1.05F;
                        cancelStage = 1;
                    }
    
    
                    switch (cancelStage)
                    {
                        case 1:
                            cancelStage = 2;
                            mc.player.setEntityBoundingBox(mc.player.getEntityBoundingBox().offset(frozenX - mc.player.posX, 0, frozenZ - mc.player.posZ));
                            break;
                        case 2:
                            p_Event.cancel();
                            cancelStage = -1;
                            break;
                    }
    
                    previousX = mc.player.posX;
                    previousY = mc.player.posY;
                    previousZ = mc.player.posZ;
    
                    if (offsetX != 0 || offsetY != 0 || offsetZ != 0)
                    {
                        mc.player.posX += offsetX;
                        mc.player.setEntityBoundingBox(mc.player.getEntityBoundingBox().offset(0, offsetY, 0));
                        mc.player.posZ += offsetZ;
                    }
                }
                else
                {
                    if (offsetX != 0 || offsetY != 0 || offsetZ != 0)
                    {
                        mc.player.posX -= offsetX;
                        mc.player.setEntityBoundingBox(mc.player.getEntityBoundingBox().offset(0, -offsetY, 0));
                        mc.player.posZ -= offsetZ;
                    }
                }
                break;
            case Normal:
                if (p_Event.getEra() != Era.PRE)
                    return;
                
                if (mc.player.collidedHorizontally && mc.player.onGround && mc.player.fallDistance == 0.0f && !mc.player.isInWeb && !mc.player.isOnLadder() && !mc.player.movementInput.jump)
                {
                    AxisAlignedBB box = mc.player.getEntityBoundingBox().offset(0.0, 0.05, 0.0).grow(0.05);
                    if (!mc.world.getCollisionBoxes(mc.player, box.offset(0.0, 1.0, 0.0)).isEmpty())
                        return;
                    
                    double stepHeight = -1.0;
                    for (final AxisAlignedBB bb : mc.world.getCollisionBoxes(mc.player, box))
                    {
                        if (bb.maxY > stepHeight)
                            stepHeight = bb.maxY;
                    }
                    
                    stepHeight -= mc.player.posY;
                    
                    if (stepHeight < 0.0 || stepHeight > 1.0)
                        return;

                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, mc.player.onGround));
                    mc.player.setPosition(mc.player.posX, mc.player.posY+1, mc.player.posZ);
                }
                break;
            default:
                break;
        
        }
    });
}
