package me.ionar.salhack.module.world;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.player.EventPlayerMove;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import static java.lang.Double.isNaN;

public class ScaffoldModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "" }, "Tower lets you go up fast when holding space and placing blocks, normal will disable that", Modes.Tower);
    public final Value<Boolean> StopMotion = new Value<Boolean>("StopMotion", new String[] {""}, "Stops you from moving if the block isn't placed yet", true);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]
    { "Delay" }, "Delay of the place", 0f, 0.0f, 1.0f, 0.1f);

    public enum Modes
    {
        Tower,
        Normal,
    }

    public ScaffoldModule()
    {
        super("Scaffold", new String[]
        { "Scaffold" }, "Places blocks under you", "NONE", 0x36DB24, ModuleType.WORLD);
    }
    
    private Timer _timer = new Timer();
    private Timer _towerPauseTimer = new Timer();
    private Timer _towerTimer = new Timer();
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> onMotionUpdate = new Listener<>(event ->
    {
        if (event.isCancelled())
            return;
        
        if (event.getEra() != Era.PRE)
            return;
        
        if (!_timer.passed(Delay.getValue() * 1000))
            return;
        
        // verify we have a block in our hand
        ItemStack stack = mc.player.getHeldItemMainhand();
        
        int prevSlot = -1;
        
        if (!verifyStack(stack))
        {
            for (int i = 0; i < 9; ++i)
            {
                stack = mc.player.inventory.getStackInSlot(i);
                
                if (verifyStack(stack))
                {
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }
        }
        
        if (!verifyStack(stack))
            return;
        
        _timer.reset();
        
        BlockPos toPlaceAt = null;
        
        BlockPos feetBlock = PlayerUtil.GetLocalPlayerPosFloored().down();
        
        boolean placeAtFeet = isValidPlaceBlockState(feetBlock);
        
        // verify we are on tower mode, feet block is valid to be placed at, and 
        if (Mode.getValue() == Modes.Tower && placeAtFeet && mc.player.movementInput.jump && _towerTimer.passed(250) && !mc.player.isElytraFlying())
        {
            // todo: this can be moved to only do it on an SPacketPlayerPosLook?
            if (_towerPauseTimer.passed(1500))
            {
                _towerPauseTimer.reset();
                mc.player.motionY = -0.28f;
            }
            else
            {
                final float towerMotion = 0.41999998688f;
                
                mc.player.setVelocity(0, towerMotion, 0);
                
            }
        }
        
        if (placeAtFeet)
            toPlaceAt = feetBlock;
        else // find a supporting position for feet block
        {
            ValidResult result = BlockInteractionHelper.valid(feetBlock);
            
            // find a supporting block
            if (result != ValidResult.Ok && result != ValidResult.AlreadyBlockThere)
            {
                BlockPos[] array = { feetBlock.north(), feetBlock.south(), feetBlock.east(), feetBlock.west() };
                
                BlockPos toSelect = null;
                double lastDistance = 420.0;
                
                for (BlockPos pos : array)
                {
                    if (!isValidPlaceBlockState(pos))
                        continue;
                    
                    double dist = pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
                    if (lastDistance > dist)
                    {
                        lastDistance = dist;
                        toSelect = pos;
                    }
                }
                
                // if we found a position, that's our selection
                if (toSelect != null)
                    toPlaceAt = toSelect;
            }
        
        }
        
        if (toPlaceAt != null)
        {
            // PositionRotation
            // CPacketPlayerTryUseItemOnBlock
            // CPacketAnimation

            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
            
            for (final EnumFacing side : EnumFacing.values())
            {
                final BlockPos neighbor = toPlaceAt.offset(side);
                final EnumFacing side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
                {
                    final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f)
                    {
                        float[] rotations = getFacingRotations(toPlaceAt.getX(), toPlaceAt.getY(), toPlaceAt.getZ(), side);
                        
                        event.cancel();
                        PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                        break;
                    }
                }
            }
            
            if (BlockInteractionHelper.place(toPlaceAt, 5.0f, false, false) == PlaceResult.Placed)
            {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
        else
            _towerPauseTimer.reset();
        
        // set back our previous slot
        if (prevSlot != -1)
        {
            mc.player.inventory.currentItem = prevSlot;
            mc.playerController.updateController();
        }
    });

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketPlayerPosLook)
        {
            // reset this if we flagged the anticheat
            _towerTimer.reset();
        }
    });

    @EventHandler
    private Listener<EventPlayerMove> OnPlayerMove = new Listener<>(p_Event ->
    {
        if (!StopMotion.getValue())
            return;
        
        double x = p_Event.X;
        double y = p_Event.Y;
        double z = p_Event.Z;
        
        if (mc.player.onGround && !mc.player.noClip)
        {
            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, -1.0f, 0.0D);)
            {
                if (x < increment && x >= -increment)
                {
                    x = 0.0D;
                }
                else if (x > 0.0D)
                {
                    x -= increment;
                }
                else
                {
                    x += increment;
                }
            }
            for (; z != 0.0D && isOffsetBBEmpty(0.0D, -1.0f, z);)
            {
                if (z < increment && z >= -increment)
                {
                    z = 0.0D;
                }
                else if (z > 0.0D)
                {
                    z -= increment;
                }
                else
                {
                    z += increment;
                }
            }
            for (; x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, -1.0f, z);)
            {
                if (x < increment && x >= -increment)
                {
                    x = 0.0D;
                }
                else if (x > 0.0D)
                {
                    x -= increment;
                }
                else
                {
                    x += increment;
                }
                if (z < increment && z >= -increment)
                {
                    z = 0.0D;
                }
                else if (z > 0.0D)
                {
                    z -= increment;
                }
                else
                {
                    z += increment;
                }
            }
        }
        
        p_Event.X = x;
        p_Event.Y = y;
        p_Event.Z = z;
        p_Event.cancel();
    });

    private boolean isOffsetBBEmpty(double x, double y, double z)
    {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, y, z)).isEmpty();
    }
    
    private boolean isValidPlaceBlockState(BlockPos pos)
    {
        return BlockInteractionHelper.valid(pos) == ValidResult.Ok;
    }

    private boolean verifyStack(ItemStack stack)
    {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock;
    }

    // todo move these to blockinteractionhelper
    
    private float[] getFacingRotations(int x, int y, int z, EnumFacing facing)
    {
        return getFacingRotations(x, y, z, facing, 1);
    }

    private float[] getFacingRotations(int x, int y, int z, EnumFacing facing, double width)
    {
        return getRotationsForPosition(x + 0.5 + facing.getDirectionVec().getX() * width / 2.0, y + 0.5 + facing.getDirectionVec().getY() * width / 2.0, z + 0.5 + facing.getDirectionVec().getZ() * width / 2.0);
    }

    private float[] getRotationsForPosition(double x, double y, double z)
    {
        return getRotationsForPosition(x, y, z, mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
    }

    private float[] getRotationsForPosition(double x, double y, double z, double sourceX, double sourceY, double sourceZ)
    {
        double deltaX = x - sourceX;
        double deltaY = y - sourceY;
        double deltaZ = z - sourceZ;

        double yawToEntity;

        if (deltaZ < 0 && deltaX < 0) { // quadrant 3
            yawToEntity = 90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // forward
        } else if (deltaZ < 0 && deltaX > 0) { // quadrant 4
            yawToEntity = -90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // back
        } else { // quadrants one or two
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ
                * deltaZ);

        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));

        yawToEntity = wrapAngleTo180((float) yawToEntity);
        pitchToEntity = wrapAngleTo180((float) pitchToEntity);

        yawToEntity = isNaN(yawToEntity) ? 0 : yawToEntity;
        pitchToEntity = isNaN(pitchToEntity) ? 0 : pitchToEntity;

        return new float[] { (float) yawToEntity, (float) pitchToEntity };
    }

    private float wrapAngleTo180(float angle)
    {
        angle %= 360.0F;

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }
        while (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

}
