package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import me.ionar.salhack.module.movement.SpeedModule;

import me.zero.alpine.fork.listener.EventHandler;

import java.awt.*;
import java.awt.event.KeyEvent;

public class AnchorModule extends Module {

    /*
     * Author LeonLeonPotato over many, many days (I suck at java lol)
     */

    public AnchorModule() {
        super("Anchor", new String[]{"anchor"}, "stops all movement when over a hole", "NONE", 0xDB2468,
                ModuleType.MOVEMENT);
    }

    public final Value<Boolean> PitchMode = new Value<Boolean>("pitchMode", new String[]{"pitchMode"},
            "activates only when your pitch is below a value", true);
    public final Value<Float> pitch = new Value<Float>("pitch", new String[]{"pitch"}, "the pitch to activate at",
            75f, 0.0f, 90f, 1f);
    public final Value<Boolean> pull = new Value<Boolean>("pull", new String[]{"pull"},
            "pulls you into a hole, like reverseStep", false);
    public final Value<Float> pullSpeed = new Value<Float>("pullSpeed", new String[]{"pull"},
            "pulls you into a hole, like reverseStep", 2.5f, 0f, 5f, 0.5f);
    public final Value<centermodes> CenterMode = new Value<centermodes>("Center", new String[]{"Center"},
            "Moves you to center of block", centermodes.NCP);
    public final Value<Integer> height = new Value<Integer>("height", new String[]{"height"},
            "the height which you are at from a hole which anchor will activate", 10, 1, 10, 1);
    public final Value<Boolean> toggleInHole = new Value<Boolean>("ToggleInHole", new String[]{"toggleAfter"},
            "toggles after", false);
    public final Value<Boolean> toggleSpeed = new Value<Boolean>("ToggleSpeed", new String[]{"toggleSpeed"},
            "toggles speed", false);

    public enum centermodes {
        Teleport, NCP, None,
    }

    /*
     * I planned to make toggleStrafe including other clients strafes by typing the
     * keybind... yeah idk how to make it public enum strafes { single, multi }
     */
    @EventHandler
    private final Listener<EventClientTick> EventPlayerTick = new Listener<>(p_Event -> {
        Vec3d Center = Vec3d.ZERO;
        Vec3d SurroundCenter = Vec3d.ZERO;
        boolean Anchoring = false;
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        robot.keyPress(KeyEvent.VK_A);

        if (mc.player == null || mc.world == null) {
            return;
        }

        if (PitchMode.getValue() && mc.player.rotationPitch <= 90 - pitch.getValue()) {
            return;
        }

        if (mc.player.capabilities.isFlying || ModuleManager.Get().GetMod(FlightModule.class).isEnabled()
                || mc.player.isElytraFlying()) {
            return;
        }

        if (Center == Vec3d.ZERO) {
            Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);

            // check if we are above a surrounded hole
            for (int a = 0; a < height.getValue(); a++) {
                if (IsSurrounded(getPlayerPos().down(a))) {
                    for (int b = 0; b < a; b++) {
                        if (mc.world.getBlockState(getPlayerPos().down(b)).getBlock() != Blocks.AIR) {
                            return;
                        }
                    }
                    Anchoring = true;
                    SurroundCenter = Center;
                }
            }

            // Pull
            if (Anchoring && pull.getValue()) {
                mc.player.motionY = pullSpeed.getValue().doubleValue() * -1;
            }

            // NCP centering
            if (Anchoring && CenterMode.getValue() == centermodes.NCP) {
                double l_XDiff = Math.abs((SurroundCenter.x) - mc.player.posX);
                double l_ZDiff = Math.abs((SurroundCenter.z) - mc.player.posZ);

                if (l_XDiff <= 0.1 && l_ZDiff <= 0.1) {
                    Center = Vec3d.ZERO;
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                } else {
                    double l_MotionX = (SurroundCenter.x) - mc.player.posX;
                    double l_MotionZ = (SurroundCenter.z) - mc.player.posZ;

                    mc.player.motionX = l_MotionX / 2;
                    mc.player.motionZ = l_MotionZ / 2;
                }
            }

            // Teleport centering
            if (Anchoring && CenterMode.getValue() == centermodes.Teleport) {
                mc.player.connection.sendPacket(
                        new CPacketPlayer.Position(SurroundCenter.x, mc.player.posY, SurroundCenter.z, false));
                mc.player.setPosition(SurroundCenter.x, mc.player.posY, SurroundCenter.z);
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }

            if (Anchoring && CenterMode.getValue() == centermodes.None) {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }

            if (IsSurrounded(getPlayerPos()) && toggleInHole.getValue() && this.isEnabled()) {
                this.toggle();
            }

            if (toggleSpeed.getValue() && Anchoring && ModuleManager.Get().GetMod(SpeedModule.class).isEnabled()) {
                ModuleManager.Get().GetMod(SpeedModule.class).toggle();
            }
        }
    });

    //////////////////////////////////////////////////////////////// METHODS
    //////////////////////////////////////////////////////////////// ///////////////////////////////////////////////////////////////////////////////////

    public Vec3d GetCenter(double posX, double posY, double posZ) {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D;

        return new Vec3d(x, y, z);
    }

    public boolean IsSurrounded(BlockPos pos) {
        if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR
                && mc.world.getBlockState(pos.down()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(pos.west()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(pos.north()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(pos.east()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(pos.south()).getBlock() != Blocks.AIR) {
            return true;
        } else {
            return false;
        }
    }

    private BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }
}
