package me.ionar.salhack.module.combat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.MathUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoTrapFeet extends Module
{
    public final Value<Float> Distance = new Value<Float>("Distance", new String[] {"Dist"}, "Distance to start searching for targets", 5.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> rotate = new Value<Boolean>("rotate", new String[]
    { "rotate" }, "Rotate", true);
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[] {"BPT"}, "Blocks per tick", 4, 1, 10, 1);
    public final Value<Boolean> Toggles = new Value<Boolean>("Toggles", new String[]
    { "Toggles" }, "Toggles off after a trap", false);
    
    public AutoTrapFeet()
    {
        super("AutoTrapFeet", new String[]
        { "AutoTrapFeet" }, "AutoTrapFeet", "NONE", 0x24DB78, ModuleType.COMBAT);
    }
    
    EntityPlayer Target = null;

    @Override
    public void toggleNoSave()
    {
        
    }
    
    @Override
    public void onEnable()
    {
        super.onEnable();
        
        if (mc.world == null || mc.player == null)
        {
            toggle();
            return;
        }
        
        float l_LastDist = 100.0f;
        
        for (EntityPlayer l_Player : mc.world.playerEntities)
        {
            if (l_Player == null || l_Player == mc.player)
                continue;
            
            if (FriendManager.Get().IsFriend(l_Player))
                continue;
            
            float l_Dist = l_Player.getDistance(mc.player);
            
            if (l_Dist > Distance.getValue())
                continue;
            
            if (l_LastDist > l_Dist)
            {
                Target = l_Player;
                l_LastDist = l_Dist;
            }
        }
        
       // Target = mc.world.playerEntities.stream()
       //         .filter(p_Entity -> Target != mc.player && Target.getName() != mc.player.getName())
       //         .min(Comparator.comparing(p_Entity -> mc.player.getDistance(p_Entity)))
       //         .orElse(null);
        
        if (Target != null)
        {
            SalHack.SendMessage("[AutoTrapFeet]: Found target " + Target.getName());
        }
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        if (Target == null)
        {
            return;
        }
        
        if (IsCurrentTargetTrapped())
        {
            if (Toggles.getValue())
            {
                toggle();
                SalHack.SendMessage(ChatFormatting.LIGHT_PURPLE + "[AutoTrapFeet]: Current target is trapped. Toggling");
            }
            return;
        }
        
        if (!HasObsidian())
            return;
        
        DecimalFormat l_Format = new DecimalFormat("#.###");

        final Vec3d pos = new Vec3d(Double.valueOf(l_Format.format(Target.posX)), Target.posY, Double.valueOf(l_Format.format(Target.posZ)));
        final float playerSpeed = (float) MathUtil.getDistance(pos, Target.posX, Target.posY, Target.posZ);

        final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);
        
        BlockPos northAbove = interpPos.north().up();
        BlockPos southAbove = interpPos.south().up();
        BlockPos eastAbove = interpPos.east().up();
        BlockPos westAbove = interpPos.west().up();
        
        BlockPos topBlock = interpPos.up().up();
        
        final BlockPos[] l_Array = { northAbove, southAbove, eastAbove, westAbove, topBlock };
        
        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1)
        {
            if ((mc.player.onGround && playerSpeed <= 0.005f))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
                
                int l_BlocksPerTick = BlocksPerTick.getValue();
                
                for (BlockPos l_Pos : l_Array)
                {
                    ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
                    
                    /// if already a block there, ignore it, it's satisfied
                    if (l_Result == ValidResult.AlreadyBlockThere)
                        continue;
                    
                    if (l_Result == ValidResult.NoNeighbors)
                    {
                        final BlockPos[] l_Test = { l_Pos.north(), l_Pos.south(), l_Pos.east(), l_Pos.west(), l_Pos.up(), l_Pos.down() };
                        
                        PlaceResult l_PlaceResult = PlaceResult.CantPlace;
                        
                        for (BlockPos l_Pos2 : l_Test)
                        {
                            ValidResult l_Result2 = BlockInteractionHelper.valid(l_Pos2);
                            
                            if (l_Result2 == ValidResult.NoNeighbors || l_Result2 == ValidResult.NoEntityCollision)
                                continue;
                            
                            l_PlaceResult = BlockInteractionHelper.place (l_Pos2, Distance.getValue(), rotate.getValue(), false);
                            break;
                        }
                        
                        if (l_PlaceResult != PlaceResult.CantPlace)
                            break;
                    }
                    
                    PlaceResult l_ResultPlace = BlockInteractionHelper.place (l_Pos, Distance.getValue(), rotate.getValue(), false);

                    if (l_ResultPlace == PlaceResult.Placed)
                    {
                        if (--l_BlocksPerTick <= 0)
                            break;
                    }
                    /*else if (l_ResultPlace == PlaceResult.CantPlace)
                    {
                        final BlockPos[] l_Test = { l_Pos.north(), l_Pos.south(), l_Pos.east(), l_Pos.west(), l_Pos.up(), l_Pos.down() };
                        
                        for (BlockPos l_Pos2 : l_Test)
                        {
                            final BlockPos[] l_Test2 = { l_Pos2.north(), l_Pos2.south(), l_Pos2.east(), l_Pos2.west(), l_Pos2.up(), l_Pos2.down() };
                            for (BlockPos l_Pos3 : l_Test2)
                            {
                                ValidResult l_Result2 = BlockInteractionHelper.valid(l_Pos3);
                                
                                if (l_Result2 != ValidResult.Ok)
                                {
                                    final BlockPos[] l_Test3 = { l_Pos2.north(), l_Pos2.south(), l_Pos2.east(), l_Pos2.west(), l_Pos2.up(), l_Pos2.down() };
                                    for (BlockPos l_Pos4 : l_Test3)
                                    {
                                        ValidResult l_Result3 = BlockInteractionHelper.valid(l_Pos4);
                                        
                                        if (l_Result3 != ValidResult.Ok)
                                        {
                                            
                                            continue;
                                        }
                                        
                                        BlockInteractionHelper.place (l_Pos4, Distance.getValue(), rotate.getValue());
                                    }
                                    continue;
                                }
                                
                                BlockInteractionHelper.place (l_Pos3, Distance.getValue(), rotate.getValue());
                            }
                        }
                        
                    }*/
                    else
                    {
                    //    SalHack.SendMessage(String.format("Can't place at %s because of %s", l_Pos.toString(), l_ResultPlace.toString()));
                    }
                }

                if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN))
                {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();
            }
        }
    });

    public boolean IsCurrentTargetTrapped()
    {
        if (Target == null)
            return true;

        DecimalFormat l_Format = new DecimalFormat("#.###");

        final Vec3d l_PlayerPos = new Vec3d(Double.valueOf(l_Format.format(Target.posX)), Target.posY, Double.valueOf(l_Format.format(Target.posZ)));

        final BlockPos l_InterPos = new BlockPos(l_PlayerPos.x, l_PlayerPos.y, l_PlayerPos.z);
        
        BlockPos l_North = l_InterPos.north().up();
        BlockPos l_South = l_InterPos.south().up();
        BlockPos l_East = l_InterPos.east().up();
        BlockPos l_West = l_InterPos.west().up();
        
        BlockPos l_Top = l_InterPos.up().up();
        
        final BlockPos[] l_Array = { l_North, l_South, l_East, l_West, l_Top };
        
        for (BlockPos l_Pos : l_Array)
        {
            if (BlockInteractionHelper.valid(l_Pos) != ValidResult.AlreadyBlockThere)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasStack(Block type)
    {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    public boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    public int findStackHotbar(Block type)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type)
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public boolean HasObsidian()
    {
        return findStackHotbar(Blocks.OBSIDIAN) != -1;
    }
}
