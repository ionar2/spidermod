package com.ionar.salhack.module.dupe;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import com.ionar.salhack.events.network.EventNetworkPacketEvent;
import com.ionar.salhack.events.player.EventPlayerUpdate;
import com.ionar.salhack.main.SalHack;
import com.ionar.salhack.managers.ModuleManager;
import com.ionar.salhack.managers.TickRateManager;
import com.ionar.salhack.module.Module;
import com.ionar.salhack.module.Value;
import com.ionar.salhack.module.exploit.EntityDesyncModule;
import com.ionar.salhack.module.exploit.PacketCancellerModule;
import com.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public final class AutoDuperWWEModule extends Module {
  public final Value<Boolean> TPSScaling = new Value("TpsScaling", new String[] { "TPSScaling" }, "Use TPS scaling on base timers", Boolean.valueOf(true));
  
  public final Value<Integer> PacketToggleDelay = new Value("PacketToggleDelay", new String[] { "PacketToggleDelay" }, "Delay for packet toggling", Integer.valueOf(12000), Integer.valueOf(0), Integer.valueOf(30000), Integer.valueOf(1000));
  
  public final Value<Integer> DupeDelay = new Value("DupeDelay", new String[] { "DupeDelay" }, "Time for DupeDelay (TP back to starting point)", Integer.valueOf(24000), Integer.valueOf(0), Integer.valueOf(30000), Integer.valueOf(1000));
  
  public final Value<Integer> RemountDelay = new Value("RemountDelay", new String[] { "RemountDelay" }, "Time for remounting after throwing items", Integer.valueOf(1000), Integer.valueOf(0), Integer.valueOf(10000), Integer.valueOf(1000));
  
  public final Value<Integer> RestartTimer = new Value("RestartTimer", new String[] { "RestartTimer" }, "Time for restarting after remounting", Integer.valueOf(1000), Integer.valueOf(0), Integer.valueOf(10000), Integer.valueOf(1000));
  
  public final Value<Boolean> BypassMode = new Value("BypassMode", new String[] { "BypassMode" }, "BypassMode for 2b2t on a patch day", Boolean.valueOf(false));
  
  public final Value<Boolean> UseEntityDesync = new Value("EntityDesync", new String[] { "EntityDesync" }, "use EntityDesync", Boolean.valueOf(true));
  
  public final Value<Integer> EntityDesyncDelay = new Value("EntityDesyncDelay", new String[] { "EntityDesyncDelay" }, "edsync delay", Integer.valueOf(300), Integer.valueOf(0), Integer.valueOf(10000), Integer.valueOf(1000));
  
  public final Value<Integer> DupeRemountDelay = new Value("DupeRemountDelay", new String[] { "DupeRemountDelay" }, "DupeRemountDelay", Integer.valueOf(300), Integer.valueOf(0), Integer.valueOf(10000), Integer.valueOf(1000));
  
  public final Value<Integer> InventoryDelay = new Value("InventoryDelay", new String[] { "InventoryDelay" }, "InventoryDelay", Integer.valueOf(1000), Integer.valueOf(0), Integer.valueOf(10000), Integer.valueOf(1000));
  
  public final Value<Boolean> LockOriginalToStart = new Value("LockOriginalToStart", new String[] { "Lock" }, "Lock", Boolean.valueOf(false));
  
  public final Value<Boolean> IgnorePosUpdate = new Value("IgnorePosUpdate", new String[] { "IgnorePosUpdate" }, "IgnorePosUpdate", Boolean.valueOf(false));
  
  public final Value<Integer> BypassRemount = new Value("BypassRemount", new String[] { "" }, "BypassRemount", Integer.valueOf(18000), Integer.valueOf(0), Integer.valueOf(18000), Integer.valueOf(1000));
  
  private Vec3d StartPos;
  
  private float Pitch;
  
  private float Yaw;
  
  private DupeFreecam Freecam;
  
  private PacketCancellerModule PacketCanceller;
  
  private EntityDesyncModule EntityDesync;
  
  private Timer timer;
  
  private Entity riding;
  
  private BlockPos button;
  
  private int ShulkersDuped;
  
  private Vec3d StartingPosition;
  
  private boolean RestartDupeNoInv;
  
  private boolean SetIgnoreStartClip;
  
  private int StreakCounter;
  
  private Timer packetTimer;
  
  @EventHandler
  private Listener<EventPlayerUpdate> OnPlayerUpdate;
  
  @EventHandler
  private Listener<EventNetworkPacketEvent> PacketEvent;
  
  public AutoDuperWWEModule() {
    super("DupeBot", new String[] { "Duper", "Autodonkeydupe" }, "Automaticly performs the wwe donkey dupe", "NONE", -1, Module.ModuleType.DUPE);
    this.StartPos = Vec3d.ZERO;
    this.ShulkersDuped = 0;
    this.StartingPosition = Vec3d.ZERO;
    this.RestartDupeNoInv = false;
    this.SetIgnoreStartClip = false;
    this.StreakCounter = 0;
    this.packetTimer = new Timer();
    this.OnPlayerUpdate = new Listener(p_Event -> {
          float seconds = (float)(System.currentTimeMillis() - this.packetTimer.getTime()) / 1000.0F % 60.0F;
          if (!isEnabled())
            return; 
          if (this.StartPos == Vec3d.ZERO)
            return; 
          if (!this.SetIgnoreStartClip && !((Boolean)this.IgnorePosUpdate.getValue()).booleanValue())
            this.mc.player.setPosition(this.StartPos.x, this.StartPos.y, this.StartPos.z); 
          this.mc.player.rotationPitch = this.Pitch;
          this.mc.player.rotationYaw = this.Yaw;
        }new java.util.function.Predicate[0]);
    this.PacketEvent = new Listener(p_Event -> {
          if (p_Event.getPacket() != null)
            this.packetTimer.reset(); 
          if (p_Event.getPacket() instanceof SPacketWindowItems) {
            if (!this.mc.player.isRiding() || !(this.mc.player.getRidingEntity() instanceof AbstractChestHorse))
              return; 
            AbstractChestHorse l_Donkey = GetNearDonkey();
            this.RestartDupeNoInv = false;
            if (l_Donkey == null) {
              SalHack.SendMessage("Could not find the donkey near you");
              this.mc.player.closeScreen();
              HandleDupe();
              return;
            } 
            SalHack.SendMessage("Dumping items from " + this.mc.player.getRidingEntity().getName());
            SPacketWindowItems l_Packet = (SPacketWindowItems)p_Event.getPacket();
            int l_I = 0;
            for (ItemStack l_Stack : l_Packet.getItemStacks()) {
              if (l_I > 1 && l_I < 17) {
                this.mc.playerController.windowClick(l_Packet.getWindowId(), l_I, 1, ClickType.THROW, (EntityPlayer)this.mc.player);
                if (l_Stack.getItem() instanceof net.minecraft.item.ItemShulkerBox)
                  this.ShulkersDuped++; 
              } 
              l_I++;
            } 
            SalHack.SendMessage(ChatFormatting.GREEN + "Done dumping items from " + this.mc.player.getRidingEntity().getName() + "!");
            this.StreakCounter++;
            this.timer.schedule(new TimerTask() {
                  public void run() {
                    AutoDuperWWEModule.this.mc.player.closeScreen();
                    AutoDuperWWEModule.this.mc.displayGuiScreen(null);
                    AutoDuperWWEModule.this.Remount();
                  }
                },  ((Integer)this.RemountDelay.getValue()).intValue());
          } 
        }new java.util.function.Predicate[0]);
  }
  
  public String getMetaData() {
    return "" + this.ShulkersDuped + ChatFormatting.GOLD + " Streak: " + this.StreakCounter;
  }
  
  public void ToggleOffMods() {
    if (this.Freecam != null && this.Freecam.isEnabled())
      this.Freecam.toggle(); 
    if (this.PacketCanceller != null && this.PacketCanceller.isEnabled())
      this.PacketCanceller.toggle(); 
    if (this.EntityDesync != null && this.EntityDesync.isEnabled())
      this.EntityDesync.toggle(); 
  }
  
  public void onEnable() {
    super.onEnable();
    if (this.mc.player == null) {
      toggle();
      return;
    } 
    if (this.mc.player.getRidingEntity() == null) {
      SalHack.SendMessage(ChatFormatting.RED + "You are not riding an entity!");
      toggle();
      return;
    } 
    this.StartingPosition = new Vec3d((this.mc.player.getRidingEntity()).posX, (this.mc.player.getRidingEntity()).posY, (this.mc.player.getRidingEntity()).posZ);
    this.StartPos = new Vec3d(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ);
    this.Pitch = this.mc.player.rotationPitch;
    this.Yaw = this.mc.player.rotationYaw;
    this.Freecam = (DupeFreecam)ModuleManager.Get().GetMod(DupeFreecam.class);
    this.PacketCanceller = (PacketCancellerModule)ModuleManager.Get().GetMod(PacketCancellerModule.class);
    this.EntityDesync = (EntityDesyncModule)ModuleManager.Get().GetMod(EntityDesyncModule.class);
    ToggleOffMods();
    this.button = null;
    this.SetIgnoreStartClip = false;
    SalHack.SendMessage("Last Streak counter was " + this.StreakCounter);
    this.StreakCounter = 0;
    HandleDupe();
  }
  
  public int CalculateNewTime(int p_BaseTime, float p_Tps) {
    if (((Boolean)this.TPSScaling.getValue()).booleanValue())
      return (int)(p_BaseTime * 20.0F / p_Tps); 
    return p_BaseTime;
  }
  
  public void HandleDupe() {
    SalHack.SendMessage(ChatFormatting.LIGHT_PURPLE + "Starting dupe!");
    if (this.timer != null)
      this.timer.cancel(); 
    this.timer = new Timer();
    if (((Boolean)this.LockOriginalToStart.getValue()).booleanValue())
      this.mc.player.getRidingEntity().setPosition(this.StartingPosition.x, this.StartingPosition.y, this.StartingPosition.z); 
    RayTraceResult ray = this.mc.objectMouseOver;
    if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK && this.button == null) {
      BlockPos pos = ray.getBlockPos();
      IBlockState iblockstate = this.mc.world.getBlockState(pos);
      if (iblockstate.getMaterial() != Material.AIR && this.mc.world.getWorldBorder().contains(pos))
        if (iblockstate.getBlock() == Blocks.WOODEN_BUTTON || iblockstate.getBlock() == Blocks.STONE_BUTTON)
          this.button = pos;  
    } 
    if (this.button == null) {
      SalHack.SendMessage("Button is null!");
      return;
    } 
    if (this.StartPos != Vec3d.ZERO)
      this.mc.player.setPosition(this.StartPos.x, this.StartPos.y, this.StartPos.z); 
    this.mc.playerController.processRightClickBlock(this.mc.player, this.mc.world, this.button, EnumFacing.UP, new Vec3d(0.0D, 0.0D, 0.0D), EnumHand.MAIN_HAND);
    SalHack.SendMessage("Rightclicked!");
    final float l_Tps = TickRateManager.Get().getTickRate();
    SalHack.SendMessage("Tps: " + l_Tps);
    this.riding = this.mc.player.getRidingEntity();
    this.timer.schedule(new TimerTask() {
          public void run() {
            AutoDuperWWEModule.this.Freecam.toggle();
            AutoDuperWWEModule.this.PacketCanceller.toggle();
            SalHack.SendMessage("Toggled the hax!");
          }
        },  100L);
    this.timer.schedule(new TimerTask() {
          public void run() {
            AutoDuperWWEModule.this.PacketCanceller.toggle();
            SalHack.SendMessage("Moving the donkey!");
            AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                  public void run() {
                    AutoDuperWWEModule.this.PacketCanceller.toggle();
                    SalHack.SendMessage("Not moving the donkey!");
                  }
                },  AutoDuperWWEModule.this.CalculateNewTime(1000, l_Tps));
          }
        }CalculateNewTime(((Integer)this.PacketToggleDelay.getValue()).intValue(), l_Tps));
    this.timer.schedule(new TimerTask() {
          public void run() {
            AutoDuperWWEModule.this.Freecam.toggle();
            if (((Boolean)AutoDuperWWEModule.this.BypassMode.getValue()).booleanValue()) {
              AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                    public void run() {
                      if (!((Boolean)AutoDuperWWEModule.this.BypassMode.getValue()).booleanValue() && ((Boolean)AutoDuperWWEModule.this.UseEntityDesync.getValue()).booleanValue()) {
                        AutoDuperWWEModule.this.PacketCanceller.toggle();
                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                              public void run() {
                                AutoDuperWWEModule.this.EntityDesync.toggle();
                                SalHack.SendMessage("EntityDesync - ON");
                                AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                      public void run() {
                                        AutoDuperWWEModule.this.EntityDesync.toggle();
                                        SalHack.SendMessage("EntityDesync - OFF");
                                      }
                                    },  AutoDuperWWEModule.this.CalculateNewTime(100, l_Tps));
                              }
                            }AutoDuperWWEModule.this.CalculateNewTime(((Integer)AutoDuperWWEModule.this.EntityDesyncDelay.getValue()).intValue(), l_Tps));
                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                              public void run() {
                                if (AutoDuperWWEModule.this.Freecam.isEnabled())
                                  AutoDuperWWEModule.this.Freecam.toggle(); 
                                if (AutoDuperWWEModule.this.PacketCanceller.isEnabled())
                                  AutoDuperWWEModule.this.PacketCanceller.toggle(); 
                                AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                      public void run() {
                                        AutoDuperWWEModule.this.RestartDupeNoInv = true;
                                        AutoDuperWWEModule.this.mc.player.sendHorseInventory();
                                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                              public void run() {
                                                if (AutoDuperWWEModule.this.RestartDupeNoInv) {
                                                  SalHack.SendMessage("Detected Ghost Donkey, retrying");
                                                  AutoDuperWWEModule.this.riding = null;
                                                  AutoDuperWWEModule.this.Remount();
                                                  return;
                                                } 
                                              }
                                            },  2000L);
                                      }
                                    }((Integer)AutoDuperWWEModule.this.InventoryDelay.getValue()).intValue());
                              }
                            }AutoDuperWWEModule.this.CalculateNewTime(((Integer)AutoDuperWWEModule.this.DupeRemountDelay.getValue()).intValue() + ((Integer)AutoDuperWWEModule.this.EntityDesyncDelay.getValue()).intValue() + 1000, l_Tps));
                      } else if (((Boolean)AutoDuperWWEModule.this.BypassMode.getValue()).booleanValue()) {
                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                              public void run() {
                                AutoDuperWWEModule.this.PacketCanceller.toggle();
                              }
                            },  500L);
                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                              public void run() {
                                AutoDuperWWEModule.this.PacketCanceller.toggle();
                              }
                            },  600L);
                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                              public void run() {
                                SalHack.SendMessage("Bypass done");
                                if (AutoDuperWWEModule.this.PacketCanceller.isEnabled())
                                  AutoDuperWWEModule.this.PacketCanceller.toggle(); 
                                AutoDuperWWEModule.this.SetIgnoreStartClip = false;
                                if (((Boolean)AutoDuperWWEModule.this.UseEntityDesync.getValue()).booleanValue())
                                  AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                        public void run() {
                                          AutoDuperWWEModule.this.EntityDesync.toggle();
                                          SalHack.SendMessage("EntityDesync - ON");
                                          AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                                public void run() {
                                                  AutoDuperWWEModule.this.EntityDesync.toggle();
                                                  SalHack.SendMessage("EntityDesync - OFF");
                                                }
                                              },  AutoDuperWWEModule.this.CalculateNewTime(100, l_Tps));
                                        }
                                      }AutoDuperWWEModule.this.CalculateNewTime(((Integer)AutoDuperWWEModule.this.EntityDesyncDelay.getValue()).intValue(), l_Tps)); 
                                AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                      public void run() {
                                        if (AutoDuperWWEModule.this.StartPos != Vec3d.ZERO)
                                          AutoDuperWWEModule.this.mc.player.setPosition(AutoDuperWWEModule.this.StartPos.x, AutoDuperWWEModule.this.StartPos.y, AutoDuperWWEModule.this.StartPos.z); 
                                        if (AutoDuperWWEModule.this.Freecam.isEnabled())
                                          AutoDuperWWEModule.this.Freecam.toggle(); 
                                        if (AutoDuperWWEModule.this.PacketCanceller.isEnabled())
                                          AutoDuperWWEModule.this.PacketCanceller.toggle(); 
                                        AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                              public void run() {
                                                AutoDuperWWEModule.this.RestartDupeNoInv = true;
                                                AutoDuperWWEModule.this.mc.player.sendHorseInventory();
                                                SalHack.SendMessage("Sending inventory.");
                                                AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                                                      public void run() {
                                                        if (AutoDuperWWEModule.this.RestartDupeNoInv) {
                                                          SalHack.SendMessage("Detected Ghost Donkey, retrying");
                                                          AutoDuperWWEModule.this.riding = null;
                                                          AutoDuperWWEModule.this.Remount();
                                                          return;
                                                        } 
                                                      }
                                                    },  2000L);
                                              }
                                            }((Integer)AutoDuperWWEModule.this.InventoryDelay.getValue()).intValue());
                                      }
                                    }AutoDuperWWEModule.this.CalculateNewTime(((Integer)AutoDuperWWEModule.this.DupeRemountDelay.getValue()).intValue() + ((Integer)AutoDuperWWEModule.this.EntityDesyncDelay.getValue()).intValue(), l_Tps));
                              }
                            }((Integer)AutoDuperWWEModule.this.BypassRemount.getValue()).intValue());
                      } 
                    }
                  }((Integer)AutoDuperWWEModule.this.DupeRemountDelay.getValue()).intValue());
            } else {
              AutoDuperWWEModule.this.PacketCanceller.toggle();
              if (AutoDuperWWEModule.this.StartPos != Vec3d.ZERO)
                AutoDuperWWEModule.this.mc.player.setPosition(AutoDuperWWEModule.this.StartPos.x, AutoDuperWWEModule.this.StartPos.y, AutoDuperWWEModule.this.StartPos.z); 
              if (AutoDuperWWEModule.this.Freecam.isEnabled())
                AutoDuperWWEModule.this.Freecam.toggle(); 
              if (AutoDuperWWEModule.this.PacketCanceller.isEnabled())
                AutoDuperWWEModule.this.PacketCanceller.toggle(); 
              AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                    public void run() {
                      AutoDuperWWEModule.this.RestartDupeNoInv = true;
                      AutoDuperWWEModule.this.mc.player.sendHorseInventory();
                      SalHack.SendMessage("Sending inventory.");
                      AutoDuperWWEModule.this.timer.schedule(new TimerTask() {
                            public void run() {
                              if (AutoDuperWWEModule.this.RestartDupeNoInv) {
                                SalHack.SendMessage("Detected Ghost Donkey, retrying");
                                AutoDuperWWEModule.this.riding = null;
                                AutoDuperWWEModule.this.Remount();
                                return;
                              } 
                            }
                          },  2000L);
                    }
                  }((Integer)AutoDuperWWEModule.this.InventoryDelay.getValue()).intValue());
            } 
          }
        }CalculateNewTime(((Integer)this.DupeDelay.getValue()).intValue(), l_Tps));
  }
  
  public void onDisable() {
    if (this.timer != null)
      this.timer.cancel(); 
    this.timer = null;
    this.StartPos = Vec3d.ZERO;
    ToggleOffMods();
  }
  
  public AbstractChestHorse GetNearDonkey() {
    int l_EntityId = (this.riding != null) ? this.riding.getEntityId() : 0;
    AbstractChestHorse l_Donkey = this.mc.world.loadedEntityList.stream().filter(entity -> (entity instanceof AbstractChestHorse && entity != this.riding && this.mc.player.getDistance(entity) < 10.0F && entity.getEntityId() != l_EntityId)).map(entity -> (AbstractChestHorse)entity).min(Comparator.comparing(c -> Float.valueOf(this.mc.player.getDistance((Entity)c)))).orElse(null);
    return l_Donkey;
  }
  
  public void Remount() {
    final AbstractChestHorse l_Donkey = GetNearDonkey();
    if (l_Donkey != null) {
      SalHack.SendMessage(ChatFormatting.GREEN + "Processing remount on " + l_Donkey.getName());
      this.riding = null;
      this.mc.player.connection.sendPacket((Packet)new CPacketInput(this.mc.player.moveStrafing, this.mc.player.moveForward, this.mc.player.movementInput.jump, true));
      this.timer.schedule(new TimerTask() {
            public void run() {
              AutoDuperWWEModule.this.mc.playerController.interactWithEntity((EntityPlayer)AutoDuperWWEModule.this.mc.player, (Entity)l_Donkey, EnumHand.MAIN_HAND);
            }
          }111L);
      this.timer.schedule(new TimerTask() {
            public void run() {
              SalHack.SendMessage("Restarting dupe!");
              AutoDuperWWEModule.this.HandleDupe();
            }
          },  (((Integer)this.RestartTimer.getValue()).intValue() + 111));
    } 
  }
}
