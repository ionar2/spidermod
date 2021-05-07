package me.ionar.salhack.module.combat;

import java.util.Comparator;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.ItemUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class Auto32kModule extends Module
{
    private Value<Modes> Mode = new Value<Modes>("Mode", new String[] {"Modes"}, "The mode to use", Modes.Hopper);
    private Value<Boolean> Automatic = new Value<Boolean>("Automatic", new String[] {"Automatic"}, "Automatically finds a place in range to place at", true);
    private Value<Integer> Delay = new Value<Integer>("Delay", new String[] {"Delay"}, "Delay", 250, 0, 2000, 100);
    private Value<Boolean> ThrowReverted = new Value<Boolean>("ThrowReverted", new String[] {"ThrowRevered"}, "Automatically throws reverted 32ks", true);
    private Value<Boolean> Toggles = new Value<Boolean>("Toggles", new String[] {"Toggles"}, "Toggles off when out of the hopper", true);
    
    enum Modes
    {
        Hopper,
    //    Dispenser,
    }
    
    public Auto32kModule()
    {
        super("Auto32k", new String[] {"Auto32k"}, "Automatically bypasses the illegals plugin to let you use a 32k", "NONE", 0xFFFFFF, ModuleType.COMBAT);
    }
    
    private int ShulkerSlot = -1;
    private BlockPos HopperPosition = null;
    private boolean WasInHopper = false;
    private boolean WasInDispenser = false;
    private Timer Take32kTimer = new Timer();
    
    /// dispenser 32k
    private BlockPos DispenserPosition = null;
    
    @Override
    public String getMetaData()
    {
        if (ShulkerSlot == -1)
            return "No shulker";
        
        return null;
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
        
        Pair<Integer, ItemStack> l_Pair = GetShulkerSlotInHotbar();
        ShulkerSlot  = l_Pair.getFirst();
        
        if (l_Pair.getSecond() != ItemStack.EMPTY)
        {
            SendMessage(String.format("%s[Auto32k] Found shulker %s", ChatFormatting.LIGHT_PURPLE, l_Pair.getSecond().getDisplayName()));
        }
        
        HopperPosition = null;
        DispenserPosition = null;
        WasInHopper = false;
        WasInDispenser = false;
        
        if (Automatic.getValue())
        {
            int l_HopperSlot = GetHopperSlot();
            
            if (l_HopperSlot == -1 || ShulkerSlot == -1)
                return;

            HopperPosition = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), 4.0f, 4, false, true, 0).stream()
                    .filter(p_Pos -> IsValidBlockPos(p_Pos))
                    .min(Comparator.comparing(p_Pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, p_Pos)))
                    .orElse(null);
            
            if (HopperPosition == null)
                return;

            mc.player.inventory.currentItem = l_HopperSlot;
            mc.playerController.updateController();
            BlockInteractionHelper.place(HopperPosition, 5.0f, true, false);
        }
        else
        {
            final RayTraceResult l_Ray = mc.objectMouseOver;
            
            if (l_Ray == null)
                return;
            
            if (l_Ray.typeOfHit != RayTraceResult.Type.BLOCK)
                return;
            
            final BlockPos l_BlockPos = l_Ray.getBlockPos();
            
            int l_LastSlot = mc.player.inventory.currentItem;
            boolean l_NeedHopper = true;
            
            if (mc.world.getBlockState(l_BlockPos).getBlock() == Blocks.HOPPER && BlockInteractionHelper.IsLiquidOrAir(l_BlockPos.up()))
                l_NeedHopper = false;
            
            HopperPosition = l_NeedHopper ? l_BlockPos.up() : l_BlockPos;
        }
    }

    @EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(p_Event ->
    {
        if (p_Event.getPacket() instanceof SPacketConfirmTransaction)
        {
            SPacketConfirmTransaction l_Packet = (SPacketConfirmTransaction)p_Event.getPacket();
            
            if (WasInDispenser && DispenserPosition != null)
            {
                mc.player.closeScreen();
                
                int l_RedstoneBlock = GetSlotById(152);
                mc.player.inventory.currentItem = l_RedstoneBlock;
                mc.playerController.updateController();
                BlockInteractionHelper.place(DispenserPosition.east(), 5.0f, true, false);
                
                DispenserPosition = null;
                
                mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(HopperPosition, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));
            }
        }
        else if (p_Event.getPacket() instanceof SPacketOpenWindow)
        {
            SPacketOpenWindow l_Packet = (SPacketOpenWindow)p_Event.getPacket();
            
            if (DispenserPosition != null)
            {
                WasInDispenser = true;
                mc.playerController.windowClick(l_Packet.getWindowId(), ShulkerSlot+36, 0, ClickType.QUICK_MOVE, mc.player);
              //  SendMessage("Send the packet!");
            }
        }
        else if (p_Event.getPacket() instanceof SPacketWindowItems)
        {
            Take32kTimer.reset();
        }
    });

    @EventHandler
    private Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (WasInHopper == true && Toggles.getValue() && mc.currentScreen == null)
        {
            toggle();
            return;
        }
        
        if (HopperPosition != null && DispenserPosition == null)
        {
            if (!(mc.currentScreen instanceof GuiHopper))
            {
                if (!WasInHopper)
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(HopperPosition, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));
            }
            else
            {
                if (!Take32kTimer.passed(Delay.getValue()))
                    return;
                
                WasInHopper = true;

                if (!(mc.world.getBlockState(HopperPosition.up()).getBlock() instanceof BlockShulkerBox))
                {
                    mc.player.inventory.currentItem = ShulkerSlot;
                    mc.playerController.updateController();
                    BlockInteractionHelper.place(HopperPosition.up(), 5.0f, true, false);
                }
                
                if (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && ItemUtil.Is32k(mc.player.getHeldItemMainhand()))
                    return;
                
                /// This is where we find 32ks
                for (int l_I = 0; l_I < 4; ++l_I)
                {
                    ItemStack l_Stack = mc.player.openContainer.getSlot(l_I).getStack();
                    
                    if (l_Stack == ItemStack.EMPTY)
                        continue;
                    
                    if (l_Stack.getItem() instanceof ItemSword)
                    {
                        if (!ItemUtil.Is32k(l_Stack))
                            continue;
                        
                        int l_FreeHotbarSlot = GetFreeHotbarSlot();
                        
                        if (l_FreeHotbarSlot != -1)
                        {
                            mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, l_I, 0, ClickType.QUICK_MOVE,
                                    mc.player);
                         //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_FreeHotbarSlot+32, 0, ClickType.PICKUP,
                        //            mc.player);
                            break;
                        }
                    }
                }
                
                for (int l_I = 0; l_I < 9; ++l_I)
                {
                    ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
                    
                    if (l_Stack.isEmpty() || !(l_Stack.getItem() instanceof ItemSword))
                        continue;
                    
                    if (ItemUtil.Is32k(l_Stack))
                    {
                        mc.player.inventory.currentItem = l_I;
                        mc.playerController.updateController();
                        
                        SendMessage(String.format("Found 32k in slot %s", l_I));
                        
                        KillAuraModule l_KillAura = (KillAuraModule)ModuleManager.Get().GetMod(KillAuraModule.class);
                        
                        if (!l_KillAura.isEnabled())
                            l_KillAura.toggle();
                        
                        l_KillAura.HitDelay.setValue(false);
                    }
                    else if (ThrowReverted.getValue())
                        mc.playerController.windowClick(((GuiContainer)mc.currentScreen).inventorySlots.windowId, l_I+32, 999, ClickType.THROW,
                                    mc.player);
                }
            }
            return;
        }
        else if (DispenserPosition != null)
        {
            if (!(mc.currentScreen instanceof GuiDispenser))
            {
                if (!WasInDispenser)
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(DispenserPosition, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));
            }
            
            return;
        }
    });
    
    private Pair<Integer, ItemStack> GetShulkerSlotInHotbar()
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack != ItemStack.EMPTY)
            {
                if (l_Stack.getItem() instanceof ItemShulkerBox)
                {
                    return new Pair<Integer, ItemStack>(l_I, l_Stack);
                }
            }
        }
        
        return new Pair<Integer, ItemStack>(-1, ItemStack.EMPTY);
    }
    
    private int GetHopperSlot()
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack != ItemStack.EMPTY)
            {
                if (Item.getIdFromItem(l_Stack.getItem()) == 154) ///< Hopper
                {
                    return l_I;
                }
            }
        }
        
        return -1;
    }
    
    private int GetSlotById(int p_Id)
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack != ItemStack.EMPTY)
            {
                if (Item.getIdFromItem(l_Stack.getItem()) == p_Id) ///< Hopper
                {
                    return l_I;
                }
            }
        }
        
        return -1;
    }
    
    private int GetFreeHotbarSlot()
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack == ItemStack.EMPTY || l_Stack.getItem() == Items.AIR)
                return l_I;
        }
        
        return -1;
    }

    private boolean IsValidBlockPos(final BlockPos p_Pos)
    {
        IBlockState l_State = mc.world.getBlockState(p_Pos);

        if (l_State.getBlock() == Blocks.AIR && mc.world.getBlockState(p_Pos.up()).getBlock() == Blocks.AIR)
        {
            ValidResult l_Result = BlockInteractionHelper.valid(p_Pos);
            
            if (l_Result == ValidResult.Ok)
                return true;
        }

        return false;
    }
}
