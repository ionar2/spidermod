package me.ionar.salhack.module.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.Queue;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

public final class AutoMendArmorModule extends Module
{
    public final Value<Float> Delay = new Value<Float>("Delay", new String[] {"D"}, "Delay for moving armor pieces", 1.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Float> Pct = new Value<Float>("Pct", new String[] {"P"}, "Amount of armor pct to heal at, so you don't waste extra experience potions", 90.0f, 0.0f, 100.0f, 10.0f);
    public final Value<Boolean> GhostHand = new Value<Boolean>("GhostHand", new String [] {"GH"}, "Uses ghost hand for exp", false);
    
    public AutoMendArmorModule()
    {
        super("AutoMendArmor", new String[]
        { "AMA" }, "Moves your armor to a free slot and mends them piece by piece. Recommended to use autoarmor incase you need to toggle this off while using it", "NONE", 0x24DBD4, ModuleType.MISC);
    }
    
    private LinkedList<MendState> SlotsToMoveTo = new LinkedList<MendState>();
    private Timer timer = new Timer();
    private Timer internalTimer = new Timer();
    private boolean ReadyToMend = false;
    private boolean AllDone = false;
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        ArrayList<ItemStack> ArmorsToMend = new ArrayList<ItemStack>();
        SlotsToMoveTo.clear();
        ReadyToMend = false;
        AllDone = false;
        
        int l_Slot = PlayerUtil.GetItemInHotbar(Items.EXPERIENCE_BOTTLE);
        
        if (l_Slot == -1)
        {
            SendMessage("You don't have any XP! Disabling!");
            toggle();
            return;
        }

        final Iterator<ItemStack> l_Armor = mc.player.getArmorInventoryList().iterator();
        
        int l_I = 0;
        boolean l_NeedMend = false;

        while (l_Armor.hasNext())
        {
            final ItemStack l_Item = l_Armor.next();
            if (l_Item != ItemStack.EMPTY && l_Item.getItem() != Items.AIR)
            {
                ArmorsToMend.add(l_Item);
                
                float l_Pct = GetArmorPct(l_Item);
                
                if (l_Pct < Pct.getValue())
                {
                    l_NeedMend = true;
                    SendMessage(ChatFormatting.LIGHT_PURPLE + "[" + ++l_I + "] Mending " + ChatFormatting.AQUA + l_Item.getDisplayName() + ChatFormatting.LIGHT_PURPLE + " it has " + l_Pct + "%.");
                }
            }
        }
        
        if (ArmorsToMend.isEmpty() || !l_NeedMend)
        {
            SendMessage(ChatFormatting.RED + "Nothing to mend!");
            toggle();
            return;
        }
        
        ArmorsToMend.sort(Comparator.comparing(ItemStack::getItemDamage).reversed());
        
        ArmorsToMend.forEach(p_Item ->
        {
            SendMessage(p_Item.getDisplayName() + " " + p_Item.getItemDamage());
        });
        
        l_I = 0;
        
        final Iterator<ItemStack> l_Itr = ArmorsToMend.iterator();
        
        boolean l_First = true;
        
        for (l_I = 0; l_I < mc.player.inventoryContainer.getInventory().size(); ++l_I)
        {
            if (l_I == 0 || l_I == 5 || l_I == 6 || l_I == 7 || l_I == 8)
                continue;
            
            ItemStack l_Stack = mc.player.inventoryContainer.getInventory().get(l_I);
            
            /// Slot must be empty or air
            if (!l_Stack.isEmpty() && l_Stack.getItem() != Items.AIR)
                continue;
            
            if (!l_Itr.hasNext())
                break;
            
            ItemStack l_ArmorS = l_Itr.next();
            
            SlotsToMoveTo.add(new MendState(l_First, l_I, GetSlotByItemStack(l_ArmorS), GetArmorPct(l_ArmorS) < Pct.getValue(), l_ArmorS.getDisplayName()));
            
            if (l_First)
                l_First = false;
            
           // SendMessage("Found free slot " + l_I + " for " + l_ArmorS.getDisplayName() + " stack here is " + l_Stack.getDisplayName());
        }
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        p_Event.cancel();
        
        boolean l_IsSprinting = mc.player.isSprinting();

        if (l_IsSprinting != mc.player.serverSprintState)
        {
            if (l_IsSprinting)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = l_IsSprinting;
        }

        boolean l_IsSneaking = mc.player.isSneaking();

        if (l_IsSneaking != mc.player.serverSneakState)
        {
            if (l_IsSneaking)
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            else
            {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = l_IsSneaking;
        }

        if (PlayerUtil.isCurrentViewEntity())
        {
            float l_Pitch = 90f;
            float l_Yaw = mc.player.rotationYaw;
            
            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double l_PosXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double l_PosYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double l_PosZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double l_YawDifference = (double)(l_Yaw - mc.player.lastReportedYaw);
            double l_RotationDifference = (double)(l_Pitch - mc.player.lastReportedPitch);
            ++mc.player.positionUpdateTicks;
            boolean l_MovedXYZ = l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean l_MovedRotation = l_YawDifference != 0.0D || l_RotationDifference != 0.0D;

            if (mc.player.isRiding())
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, l_Yaw, l_Pitch, mc.player.onGround));
                l_MovedXYZ = false;
            }
            else if (l_MovedXYZ && l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (l_MovedXYZ)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            }
            else if (l_MovedRotation)
            {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(l_Yaw, l_Pitch, mc.player.onGround));
            }
            else if (mc.player.prevOnGround != mc.player.onGround)
            {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (l_MovedXYZ)
            {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (l_MovedRotation)
            {
                mc.player.lastReportedYaw = l_Yaw;
                mc.player.lastReportedPitch = l_Pitch;
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
        
        if (timer.passed(Delay.getValue() * 100))
        {
            timer.reset();
            
            if (SlotsToMoveTo.isEmpty())
                return;
            
            boolean l_NeedBreak = false;
            
            for (MendState l_State : SlotsToMoveTo)
            {
                if (l_State.MovedToInv)
                    continue;
                
                l_State.MovedToInv = true;
                
             //   SendMessage("" + l_State.SlotMovedTo);

                if (l_State.Reequip)
                {
                    if (l_State.SlotMovedTo <= 4)
                    {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                    }
                    else
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.QUICK_MOVE, mc.player);
                 //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                }
                else
                {
                 //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.QUICK_MOVE, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_State.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                }
                
                l_NeedBreak = true;
                break;
            }
            
            if (!l_NeedBreak)
            {
                ReadyToMend = true;
                
                if (AllDone)
                {
                    SendMessage(ChatFormatting.AQUA + "Disabling.");
                    toggle();
                    return;
                }
            }
        }
        
        if (!internalTimer.passed(1000))
            return;
        
        if (ReadyToMend && !AllDone)
        {
            ItemStack l_CurrItem = mc.player.getHeldItemMainhand();

            int l_CurrSlot = -1;
            if (l_CurrItem.isEmpty() || l_CurrItem.getItem() != Items.EXPERIENCE_BOTTLE)
            {
                int l_Slot = PlayerUtil.GetItemInHotbar(Items.EXPERIENCE_BOTTLE);
                
                if (l_Slot != -1)
                {
                    l_CurrSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = l_Slot;
                    mc.playerController.updateController();
                }
                else
                {
                    SendMessage(ChatFormatting.RED + "No XP Found!");

                    SlotsToMoveTo.forEach(p_State ->
                    {
                        p_State.MovedToInv = false;
                        p_State.Reequip = true;
                    });
                    
                    SlotsToMoveTo.get(0).MovedToInv = true;
                    AllDone = true;
                    return;
                }
            }
            
            l_CurrItem = mc.player.getHeldItemMainhand();
            
            if (l_CurrItem.isEmpty() || l_CurrItem.getItem() != Items.EXPERIENCE_BOTTLE)
                return;
            
            final Iterator<ItemStack> l_Armor = mc.player.getArmorInventoryList().iterator();
            
            while (l_Armor.hasNext())
            {
                ItemStack l_Stack = l_Armor.next();

                if (l_Stack == ItemStack.EMPTY || l_Stack.getItem() == Items.AIR)
                    continue;

                float l_ArmorPct = GetArmorPct(l_Stack);
                
                if (l_ArmorPct >= Pct.getValue())
                {
                    if (!SlotsToMoveTo.isEmpty())
                    {
                        MendState l_State = SlotsToMoveTo.get(0);
                        
                        if (l_State.DoneMending)
                        {
                            SlotsToMoveTo.forEach(p_State ->
                            {
                                p_State.MovedToInv = false;
                                p_State.Reequip = true;
                            });
                            SendMessage(ChatFormatting.GREEN + "All done!");
                            l_State.MovedToInv = true;
                            AllDone = true;
                            return;
                        }
                        
                        l_State.DoneMending = true;
                        l_State.MovedToInv = false;
                        l_State.Reequip = false;
                        
                        SendMessage(String.format("%sDone Mending %s%s %sat the requirement of %s NewPct: %s", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, l_Stack.getDisplayName(), ChatFormatting.LIGHT_PURPLE, Pct.getValue().toString() + "%", l_ArmorPct+"%"));
                        ReadyToMend = false;
                        
                        SlotsToMoveTo.remove(0);
                        SlotsToMoveTo.add(l_State);
                        
                        MendState l_NewState = SlotsToMoveTo.get(0);

                        if (l_NewState.DoneMending || !l_NewState.NeedMend)
                        {
                            SlotsToMoveTo.forEach(p_State ->
                            {
                                p_State.MovedToInv = false;
                                p_State.Reequip = true;
                            });
                            l_State.MovedToInv = true;
                            SendMessage(ChatFormatting.GREEN + "All done!");
                            AllDone = true;
                            return;
                        }
                        else
                        {
                            SendMessage(ChatFormatting.LIGHT_PURPLE + "Mending next piece.. it's name is " + ChatFormatting.AQUA + l_NewState.ItemName);
                            
                            l_NewState.MovedToInv = false;
                            l_NewState.Reequip = true;
                        }
                    }
                    
                    return;
                }
                else
                {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    
                    if (l_CurrSlot != -1 && GhostHand.getValue())
                    {
                        mc.player.inventory.currentItem = l_CurrSlot;
                        mc.playerController.updateController();
                    }
                    
                    break;
                }
            }
        }
    });
    
    public int GetSlotByItemStack(ItemStack p_Stack)
    {
        if (p_Stack.getItem() instanceof ItemArmor)
        {
            ItemArmor l_Armor = (ItemArmor) p_Stack.getItem();
            
            switch (l_Armor.getEquipmentSlot())
            {
                case CHEST:
                    return 6;
                case FEET:
                    return 8;
                case HEAD:
                    return 5;
                case LEGS:
                    return 7;
                default:
                    break;
            }
        }
        
        return mc.player.inventory.armorInventory.indexOf(p_Stack) + 5;
    }
    
    private float GetArmorPct(ItemStack p_Stack)
    {
        return ((float)(p_Stack.getMaxDamage()-p_Stack.getItemDamage()) /  (float)p_Stack.getMaxDamage())*100.0f;
    }
    
    private class MendState
    {
        public MendState(boolean p_MovedToInv, int p_SlotMovedTo, int p_ArmorSlot, boolean p_NeedMend, String p_ItemName)
        {
            MovedToInv = p_MovedToInv;
            SlotMovedTo = p_SlotMovedTo;
            ArmorSlot = p_ArmorSlot;
            NeedMend = p_NeedMend;
            ItemName = p_ItemName;
        }
        public boolean MovedToInv = false;
        public int SlotMovedTo = -1;
        public boolean Reequip = false;
        public int ArmorSlot = -1;
        public boolean DoneMending = false;
        public boolean NeedMend = true;
        public String ItemName;
    }
}
