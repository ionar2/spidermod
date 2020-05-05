package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.player.EventPlayerJump;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.player.EventPlayerMove;
import me.ionar.salhack.events.player.EventPlayerTravel;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.world.TimerModule;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;

public class SpeedModule extends Module
{
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
    { "Mode" }, "The mode of speed to use", Modes.Strafe);
    public final Value<Boolean> UseTimer = new Value<Boolean>("UseTimer", new String[]
    { "UseTimer" }, "Uses timer to go faster", false);
    public final Value<Boolean> AutoSprint = new Value<Boolean>("AutoSprint", new String[]
    { "AutoSprint" }, "Automatically sprints for you", false);
    public final Value<Boolean> SpeedInWater = new Value<Boolean>("SpeedInWater", new String[] {"SpeedInWater"}, "Speeds in water", false);

    public enum Modes
    {
        Strafe,
        OnGround
    }

    public SpeedModule()
    {
        super("Speed", new String[]
        { "Strafe" }, "Speed strafe", "NONE", 0xDB2468, ModuleType.MOVEMENT);
    }
    
    private TimerModule Timer = null;

    @Override
    public String getMetaData()
    {
        return String.valueOf(Mode.getValue());
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        Timer = (TimerModule) ModuleManager.Get().GetMod(TimerModule.class);
    }

    public float GetRotationYawForCalc()
    {
        float rotationYaw = mc.player.rotationYaw;
        if (mc.player.moveForward < 0.0f)
        {
            rotationYaw += 180.0f;
        }
        float n = 1.0f;
        if (mc.player.moveForward < 0.0f)
        {
            n = -0.5f;
        }
        else if (mc.player.moveForward > 0.0f)
        {
            n = 0.5f;
        }
        if (mc.player.moveStrafing > 0.0f)
        {
            rotationYaw -= 90.0f * n;
        }
        if (mc.player.moveStrafing < 0.0f)
        {
            rotationYaw += 90.0f * n;
        }
        return rotationYaw * 0.017453292f;
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnMotionUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.POST)
            return;

        if (mc.player.isInWater() || mc.player.isInLava())
        {
            if (!SpeedInWater.getValue())
                return;
        }
        
        if (UseTimer.getValue())
            Timer.SetOverrideSpeed(1.088f);
        
        if (mc.player.moveForward > 0.0f || mc.player.moveStrafing != 0.0f)
        {
            if (AutoSprint.getValue())
                mc.player.setSprinting(true);

            final float yaw = GetRotationYawForCalc();

            if (mc.player.onGround)
            {
                mc.player.motionY = 0.405f;
                // mc.player.jump();

                mc.player.motionX -= MathHelper.sin(yaw) * 0.2f;
                mc.player.motionZ += MathHelper.cos(yaw) * 0.2f;
            }
            else
            {
                final double sqrt = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);
                mc.player.motionX = -Math.sin(yaw) * 1.0064f * sqrt;
                mc.player.motionZ = Math.cos(yaw) * 1.0064f * sqrt;
            }
        }
    });


    @EventHandler
    private Listener<EventPlayerJump> OnPlayerJump = new Listener<>(p_Event ->
    {
        if (mc.player.collidedHorizontally)
            return;

        float n = (float) p_Event.MotionX;
        float n2 = (float) p_Event.MotionY;

        final MovementInput movementInput = mc.player.movementInput;
        float moveForward = movementInput.moveForward;
        float moveStrafe = movementInput.moveStrafe;
        float rotationYaw = mc.player.rotationYaw;
        if (moveForward != 0.0)
        {
            if (moveStrafe > 0.0)
            {
                rotationYaw += ((moveForward > 0.0) ? -45 : 45);
            }
            else if (moveStrafe < 0.0)
            {
                rotationYaw += ((moveForward > 0.0) ? 45 : -45);
            }
            moveStrafe = 0.0f;
            if (moveForward > 0.0)
            {
                moveForward = 1.0f;
            }
            else if (moveForward < 0.0)
            {
                moveForward = -1.0f;
            }
        }
        if (moveStrafe > 0.0)
        {
            moveStrafe = 1.0f;
        }
        else if (moveStrafe < 0.0)
        {
            moveStrafe = -1.0f;
        }
        mc.player.motionX = n + (moveForward * 0.2 * Math.cos(Math.toRadians(rotationYaw + 90.0f)) + moveStrafe * 0.2 * Math.sin(Math.toRadians(rotationYaw + 90.0f)));
        mc.player.motionZ = n2 + (moveForward * 0.2 * Math.sin(Math.toRadians(rotationYaw + 90.0f)) - moveStrafe * 0.2 * Math.cos(Math.toRadians(rotationYaw + 90.0f)));
    });
    
    @EventHandler
    private Listener<EventPlayerMove> OnEventPlayerMove = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        if (!mc.player.collidedHorizontally)
            return;

        // movement data variables
        float playerSpeed = 0.2873f;
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationPitch = mc.player.rotationPitch;
        float rotationYaw = mc.player.rotationYaw;

        // check for speed potion
        if (mc.player.isPotionActive(MobEffects.SPEED))
        {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            playerSpeed *= (1.0f + 0.2f * (amplifier + 1));
        }

        // not movement input, stop all motion
        if (moveForward == 0.0f && moveStrafe == 0.0f)
        {
            p_Event.X = (0.0d);
            p_Event.Z = (0.0d);
        }
        else
        {
            if (moveForward != 0.0f)
            {
                if (moveStrafe > 0.0f)
                {
                    rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                }
                else if (moveStrafe < 0.0f)
                {
                    rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                }
                moveStrafe = 0.0f;
                if (moveForward > 0.0f)
                {
                    moveForward = 1.0f;
                }
                else if (moveForward < 0.0f)
                {
                    moveForward = -1.0f;
                }
            }
            p_Event.X = ((moveForward * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))) + (moveStrafe * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))));
            p_Event.Z = ((moveForward * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))) - (moveStrafe * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))));
        }
    });
    
    @Override
    public void onDisable()
    {
        super.onDisable();

        if (UseTimer.getValue())
            Timer.SetOverrideSpeed(1.0f);
    }
}
