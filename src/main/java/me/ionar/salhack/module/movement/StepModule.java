package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
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
        Normal,
    }

    public StepModule()
    {
        super("Step", new String[] {"Spider", "NCPStep", "Stairstep"}, "Allows you to walk up blocks like stairs", "NONE", -1, ModuleType.MOVEMENT);
    }
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnMotionUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        if (EntityStep.getValue() && mc.player.isRiding())
        {
            mc.player.getRidingEntity().stepHeight = 256f;
        }
        
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
            
            switch (Mode.getValue())
            {
                case Normal:
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, mc.player.onGround));
                    mc.player.setPosition(mc.player.posX, mc.player.posY+1, mc.player.posZ);
                    break;
                default:
                    break;
            }
        }
    });
}
