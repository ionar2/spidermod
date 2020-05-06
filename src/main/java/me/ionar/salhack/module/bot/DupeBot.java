package me.ionar.salhack.module.bot;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.exploit.EntityDesyncModule;
import me.ionar.salhack.module.exploit.ModifiedFreecam;
import me.ionar.salhack.module.exploit.PacketCancellerModule;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public final class DupeBot extends Module
{
    public final Value<Boolean> TPSScaling = new Value<Boolean>("TpsScaling", new String[] {"TPSScaling"}, "Use TPS scaling on base timers", true);
    public final Value<Integer> PacketToggleDelay = new Value<Integer>("PacketToggleDelay", new String[] {"PacketToggleDelay"}, "Delay for packet toggling", 12000, 0, 30000, 1000);
    public final Value<Integer> DupeDelay = new Value<Integer>("DupeDelay", new String[] {"DupeDelay"}, "Time for DupeDelay (TP back to starting point)", 24000, 0, 30000, 1000);
    public final Value<Integer> RemountDelay = new Value<Integer>("RemountDelay", new String[] {"RemountDelay"}, "Time for remounting after throwing items", 1000, 0, 10000, 1000);
    public final Value<Integer> RestartTimer = new Value<Integer>("RestartTimer", new String[] {"RestartTimer"}, "Time for restarting after remounting", 1000, 0, 10000, 1000);
    public final Value<Boolean> BypassMode = new Value<Boolean>("BypassMode", new String[] {"BypassMode"}, "BypassMode for 2b2t on a patch day", false);
    public final Value<Boolean> UseEntityDesync = new Value<Boolean>("EntityDesync", new String[] {"EntityDesync"}, "use EntityDesync", true);
    public final Value<Integer> EntityDesyncDelay = new Value<Integer>("EntityDesyncDelay", new String[] {"EntityDesyncDelay"}, "edsync delay", 300, 0, 10000, 1000);
    public final Value<Integer> DupeRemountDelay = new Value<Integer>("DupeRemountDelay", new String[] {"DupeRemountDelay"}, "DupeRemountDelay", 300, 0, 10000, 1000);
    public final Value<Integer> InventoryDelay = new Value<Integer>("InventoryDelay", new String[] {"InventoryDelay"}, "InventoryDelay", 1000, 0, 10000, 1000);
    public final Value<Boolean> LockOriginalToStart = new Value<Boolean>("LockOriginalToStart", new String[] {"Lock"}, "Lock", false);
    public final Value<Boolean> IgnorePosUpdate = new Value<Boolean>("IgnorePosUpdate", new String[] {"IgnorePosUpdate"}, "IgnorePosUpdate", false);
    public final Value<Integer> BypassRemount = new Value<Integer>("BypassRemount", new String[] {""}, "BypassRemount", 18000, 0, 18000, 1000);

    public DupeBot()
    {
        super("Dupe", new String[]
        { "DupeMod" }, "2b dupe mod (OUTDATED ON SOME SERVERS)", "NONE", -1, ModuleType.BOT);
    }

    private Vec3d StartPos = Vec3d.ZERO;
    private float Pitch;
    private float Yaw;

    /// Mods
    private ModifiedFreecam Freecam;
    private PacketCancellerModule PacketCanceller;
    private EntityDesyncModule EntityDesync;

    private Timer timer;
    private Entity riding;
    private BlockPos button;
    private int ShulkersDuped = 0;
    private Vec3d StartingPosition = Vec3d.ZERO;
    private boolean RestartDupeNoInv = false;
    private boolean SetIgnoreStartClip = false;
    private int StreakCounter = 0;

    private me.ionar.salhack.util.Timer packetTimer = new me.ionar.salhack.util.Timer();

    @Override
    public String getMetaData()
    {
        return "" + ShulkersDuped + ChatFormatting.GOLD + " Streak: " + StreakCounter;
    }

    public void ToggleOffMods()
    {
        if (Freecam != null && Freecam.isEnabled())
            Freecam.toggle();
        if (PacketCanceller != null && PacketCanceller.isEnabled())
            PacketCanceller.toggle();
        if (EntityDesync != null && EntityDesync.isEnabled())
            EntityDesync.toggle();
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player == null)
        {
            toggle();
            return;
        }

        if (mc.player.getRidingEntity() == null)
        {
            SalHack.SendMessage(ChatFormatting.RED + "You are not riding an entity!");
            toggle();
            return;
        }

        StartingPosition = new Vec3d(mc.player.getRidingEntity().posX, mc.player.getRidingEntity().posY, mc.player.getRidingEntity().posZ);
        /// Set Starting position
        StartPos = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);
        Pitch = mc.player.rotationPitch;
        Yaw = mc.player.rotationYaw;

        /// Initalize mod objects
        Freecam = (ModifiedFreecam) ModuleManager.Get().GetMod(ModifiedFreecam.class);
        PacketCanceller = (PacketCancellerModule) ModuleManager.Get().GetMod(PacketCancellerModule.class);
        EntityDesync = (EntityDesyncModule) ModuleManager.Get().GetMod(EntityDesyncModule.class);

        /// Toggle the mods off
        ToggleOffMods();

        button = null;
        SetIgnoreStartClip = false;

        SalHack.SendMessage("Last Streak counter was " + StreakCounter);

        StreakCounter = 0;
        HandleDupe();
    }

    public int CalculateNewTime(int p_BaseTime, float p_Tps)
    {
        if (TPSScaling.getValue())
            return (int) (p_BaseTime*(20/p_Tps));
        return p_BaseTime;
    }

    public void HandleDupe()
    {
        SalHack.SendMessage(ChatFormatting.LIGHT_PURPLE + "Starting dupe!");

        if (timer != null)
            timer.cancel();

        timer = new Timer();

        if (LockOriginalToStart.getValue())
        {
            mc.player.getRidingEntity().setPosition(StartingPosition.x, StartingPosition.y, StartingPosition.z);
        }

        final RayTraceResult ray = mc.objectMouseOver;
        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && button == null)
        {
            BlockPos pos = ray.getBlockPos();
            final IBlockState iblockstate = mc.world.getBlockState(pos);

            if (iblockstate.getMaterial() != Material.AIR && mc.world.getWorldBorder().contains(pos))
            {
                if (iblockstate.getBlock() == Blocks.WOODEN_BUTTON || iblockstate.getBlock() == Blocks.STONE_BUTTON)
                {
                    button = pos;
                }
            }
        }

        if (button == null)
        {
            SalHack.SendMessage("Button is null!");
            return;
        }

        if (StartPos != Vec3d.ZERO)
            mc.player.setPosition(StartPos.x, StartPos.y, StartPos.z);

        mc.playerController.processRightClickBlock(mc.player, mc.world, button, EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
        SalHack.SendMessage("Rightclicked!");

        float l_Tps = TickRateManager.Get().getTickRate();
        SalHack.SendMessage("Tps: " + l_Tps);

        riding = mc.player.getRidingEntity();

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Freecam.toggle();
                PacketCanceller.toggle();
                SalHack.SendMessage("Toggled the hax!");
            }
        }, 100);

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                PacketCanceller.toggle();
                SalHack.SendMessage("Moving the donkey!");

                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        PacketCanceller.toggle();
                        SalHack.SendMessage("Not moving the donkey!");
                    }
                }, CalculateNewTime(1000, l_Tps));
            }
        }, CalculateNewTime(PacketToggleDelay.getValue(), l_Tps));

        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Freecam.toggle();

                if (BypassMode.getValue())
                {
                    timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            if (!BypassMode.getValue() && UseEntityDesync.getValue())
                            {
                                PacketCanceller.toggle();

                                timer.schedule(new TimerTask()
                                {
                                    @Override
                                    public void run()
                                    {
                                        EntityDesync.toggle();
                                        SalHack.SendMessage("EntityDesync - ON");

                                        timer.schedule(new TimerTask()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                EntityDesync.toggle();
                                                SalHack.SendMessage("EntityDesync - OFF");
                                            }
                                        }, CalculateNewTime(100, l_Tps));
                                    }
                                }, CalculateNewTime(EntityDesyncDelay.getValue(), l_Tps));

                                timer.schedule(new TimerTask()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (Freecam.isEnabled())
                                            Freecam.toggle();
                                        if (PacketCanceller.isEnabled())
                                            PacketCanceller.toggle();

                                        timer.schedule(new TimerTask()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                RestartDupeNoInv = true;
                                                mc.player.sendHorseInventory();

                                                timer.schedule(new TimerTask()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        if (RestartDupeNoInv)
                                                        {
                                                            SalHack.SendMessage("Detected Ghost Donkey, retrying");
                                                            riding = null;
                                                            Remount();
                                                            return;
                                                        }
                                                    }
                                                }, 2000);
                                            }
                                        }, InventoryDelay.getValue());
                                    }
                                }, CalculateNewTime(DupeRemountDelay.getValue()+EntityDesyncDelay.getValue()+1000, l_Tps));
                            }
                            else if (BypassMode.getValue())
                            {
                                /*Packet l_Packet = new CPacketVehicleMove(riding);

                                PacketCanceller.AddIgnorePacket(l_Packet);

                                mc.getConnection().sendPacket(l_Packet);

                                SalHack.SendMessage("Forced a veheicle packet.");*/

                                /// OFF
                                timer.schedule(new TimerTask()
                                {
                                    @Override
                                    public void run()
                                    {
                                        PacketCanceller.toggle();
                                    }
                                }, 500);
                                /// ON
                                timer.schedule(new TimerTask()
                                {
                                    @Override
                                    public void run()
                                    {
                                        PacketCanceller.toggle();
                                    }
                                }, 600);

                                timer.schedule(new TimerTask()
                                {
                                    @Override
                                    public void run()
                                    {
                                        SalHack.SendMessage("Bypass done");

                                        /// Disable packet canceller
                                        if (PacketCanceller.isEnabled())
                                            PacketCanceller.toggle();

                                        SetIgnoreStartClip = false;

                                        if (UseEntityDesync.getValue())
                                        {
                                            timer.schedule(new TimerTask()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    EntityDesync.toggle();
                                                    SalHack.SendMessage("EntityDesync - ON");

                                                    timer.schedule(new TimerTask()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            EntityDesync.toggle();
                                                            SalHack.SendMessage("EntityDesync - OFF");
                                                        }
                                                    }, CalculateNewTime(100, l_Tps));
                                                }
                                            }, CalculateNewTime(EntityDesyncDelay.getValue(), l_Tps));
                                        }

                                        timer.schedule(new TimerTask()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                if (StartPos != Vec3d.ZERO)
                                                    mc.player.setPosition(StartPos.x, StartPos.y, StartPos.z);

                                                if (Freecam.isEnabled())
                                                    Freecam.toggle();
                                                if (PacketCanceller.isEnabled())
                                                    PacketCanceller.toggle();

                                                timer.schedule(new TimerTask()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        RestartDupeNoInv = true;
                                                        mc.player.sendHorseInventory();
                                                        SalHack.SendMessage("Sending inventory.");

                                                        timer.schedule(new TimerTask()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                if (RestartDupeNoInv)
                                                                {
                                                                    SalHack.SendMessage("Detected Ghost Donkey, retrying");
                                                                    riding = null;
                                                                    Remount();
                                                                    return;
                                                                }
                                                            }
                                                        }, 2000);
                                                    }
                                                }, InventoryDelay.getValue());
                                            }
                                        }, CalculateNewTime(DupeRemountDelay.getValue()+EntityDesyncDelay.getValue(), l_Tps));
                                    }
                                }, BypassRemount.getValue());
                            }
                        }
                    }, DupeRemountDelay.getValue());

                }
                else
                {
                    PacketCanceller.toggle();

                    if (StartPos != Vec3d.ZERO)
                        mc.player.setPosition(StartPos.x, StartPos.y, StartPos.z);

                    if (Freecam.isEnabled())
                        Freecam.toggle();
                    if (PacketCanceller.isEnabled())
                        PacketCanceller.toggle();

                    timer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            RestartDupeNoInv = true;
                            mc.player.sendHorseInventory();
                            SalHack.SendMessage("Sending inventory.");

                            timer.schedule(new TimerTask()
                            {
                                @Override
                                public void run()
                                {
                                    if (RestartDupeNoInv)
                                    {
                                        SalHack.SendMessage("Detected Ghost Donkey, retrying");
                                        riding = null;
                                        Remount();
                                        return;
                                    }
                                }
                            }, 2000);
                        }
                    }, InventoryDelay.getValue());
                }
            }
        }, CalculateNewTime(DupeDelay.getValue(), l_Tps));
    }

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        final float seconds = ((System.currentTimeMillis() - this.packetTimer.getTime()) / 1000.0f) % 60.0f;

        if (!isEnabled())
            return;

        if (StartPos == Vec3d.ZERO)
            return;

      //  if (mc.player.isRiding())
      //      mc.player.getRidingEntity().setPosition(StartPos.x, StartPos.y, StartPos.z);
      //  else
        if (!SetIgnoreStartClip)
            if (!IgnorePosUpdate.getValue())
                mc.player.setPosition(StartPos.x, StartPos.y, StartPos.z);

        mc.player.rotationPitch = Pitch;
        mc.player.rotationYaw = Yaw;
    });

    @Override
    public void onDisable()
    {
        if (timer != null)
            timer.cancel();
        timer = null;

        StartPos = Vec3d.ZERO;

        ToggleOffMods();
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() != null)
        {
            this.packetTimer.reset();
        }

        if (p_Event.getPacket() instanceof SPacketWindowItems)
        {
            if (!mc.player.isRiding() || (!(mc.player.getRidingEntity() instanceof AbstractChestHorse)))
                return;

            AbstractChestHorse l_Donkey = GetNearDonkey();

            RestartDupeNoInv = false;

            if (l_Donkey== null)
            {
                SalHack.SendMessage("Could not find the donkey near you");
                mc.player.closeScreen();
                HandleDupe();
                return;
            }

            SalHack.SendMessage("Dumping items from " + mc.player.getRidingEntity().getName());

            SPacketWindowItems l_Packet = (SPacketWindowItems)p_Event.getPacket();

            int l_I = 0;

            for (ItemStack l_Stack : l_Packet.getItemStacks())
            {
                if (l_I > 1 && l_I < 17)
                {
                    /// 1 because drop WHOLE stack
                    mc.playerController.windowClick(l_Packet.getWindowId(), l_I, 1, ClickType.THROW, mc.player);

                    if (l_Stack.getItem() instanceof ItemShulkerBox)
                    {
                        ++ShulkersDuped;
                        //SalHack.INSTANCE.getNotificationManager().addNotification("", String.format("Duped %s%s", ChatFormatting.LIGHT_PURPLE, l_Stack.getDisplayName()));
                    }
                }

                ++l_I;
            }

            SalHack.SendMessage(ChatFormatting.GREEN + "Done dumping items from " + mc.player.getRidingEntity().getName() + "!");
            ++StreakCounter;

            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    mc.player.closeScreen();
                    mc.displayGuiScreen(null);
                    Remount();
                }
            }, RemountDelay.getValue());
        }
    });

    public AbstractChestHorse GetNearDonkey()
    {
        int l_EntityId = riding != null ? riding.getEntityId() : 0;

        AbstractChestHorse l_Donkey =  mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof AbstractChestHorse && entity != riding && mc.player.getDistance(entity) < 10.0f && entity.getEntityId() != l_EntityId)
                .map(entity -> (AbstractChestHorse) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);

        return l_Donkey;
    }


    public void Remount()
    {
        AbstractChestHorse l_Donkey = GetNearDonkey();

        if (l_Donkey != null)
        {
            SalHack.SendMessage(ChatFormatting.GREEN + "Processing remount on " + l_Donkey.getName());

            riding = null;
            mc.player.connection.sendPacket(new CPacketInput(mc.player.moveStrafing, mc.player.moveForward, mc.player.movementInput.jump, true));

            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    mc.playerController.interactWithEntity(mc.player, l_Donkey, EnumHand.MAIN_HAND);
                }
            }, 111);

            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    SalHack.SendMessage("Restarting dupe!");
                    HandleDupe();
                }
            }, RestartTimer.getValue()+111);
        }
    }
}
