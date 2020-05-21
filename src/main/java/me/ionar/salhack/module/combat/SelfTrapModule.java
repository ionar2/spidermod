package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.MathUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SelfTrapModule extends Module
{
    public final Value<Boolean> HoleCheck = new Value<Boolean>("HoleCheck", new String[]
    { "HC" }, "Only functions if you're in a hole", true);
    public final Value<Boolean> disable = new Value<Boolean>("Toggles", new String[]
    { "Toggles", "Disables" }, "Will toggle off after a place", false);

    public SelfTrapModule()
    {
        super("SelfTrap", new String[]
        { "SelfTrapHole" }, "Automatically places an obsidian over your head, and optionally if you're in a hole",
                "NONE", 0x5324DB, ModuleType.COMBAT);
    }
    
    private BlockPos TrapPos = null;

    @Override
    public void toggleNoSave()
    {
        
    }
    
    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        if (HoleCheck.getValue() && !PlayerUtil.IsPlayerInHole())
            return;
        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        TrapPos = new BlockPos(pos.x, pos.y, pos.z).up().up();

        if (IsSelfTrapped())
        {
            if (disable.getValue())
            {
                toggle();
            }
            return;
        }
        
        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1)
        {
            if ((mc.player.onGround))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                ValidResult l_Result = BlockInteractionHelper.valid(TrapPos);

                if (l_Result == ValidResult.AlreadyBlockThere
                        && !mc.world.getBlockState(TrapPos).getMaterial().isReplaceable())
                {
                    Finish(lastSlot);
                    return;
                }

                if (l_Result == ValidResult.NoNeighbors)
                {
                    BlockPos[] l_Test =
                    { TrapPos.north(), TrapPos.south(), TrapPos.east(), TrapPos.west(), TrapPos.up(), TrapPos.down().west() };

                    for (BlockPos l_Pos2 : l_Test)
                    {
                        ValidResult l_Result2 = BlockInteractionHelper.valid(l_Pos2);

                        if (l_Result2 == ValidResult.NoNeighbors || l_Result2 == ValidResult.NoEntityCollision)
                            continue;

                        PlaceResult l_Result3 = BlockInteractionHelper.place(l_Pos2, 5.0f, false, false);
                        
                        if (l_Result3 == PlaceResult.Placed)
                        {
                            p_Event.cancel();
                            float[] rotations = BlockInteractionHelper
                                    .getLegitRotations(new Vec3d(l_Pos2.getX(), l_Pos2.getY(), l_Pos2.getZ()));
                            PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                            Finish(lastSlot);
                            return;
                        }
                    }

                    Finish(lastSlot);
                    return;
                }

                PlaceResult l_Result2 = BlockInteractionHelper.place(TrapPos, 5.0f, false, false);

                if (l_Result2 == PlaceResult.Placed)
                {
                    p_Event.cancel();

                    float[] rotations = BlockInteractionHelper
                            .getLegitRotations(new Vec3d(TrapPos.getX(), TrapPos.getY(), TrapPos.getZ()));
                    PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                }
                
                Finish(lastSlot);
            }
        }
    });
    
    private void Finish(int p_LastSlot)
    {
        if (!slotEqualsBlock(p_LastSlot, Blocks.OBSIDIAN))
        {
            mc.player.inventory.currentItem = p_LastSlot;
        }
        mc.playerController.updateController();
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

    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private int findStackHotbar(Block type)
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
    
    public boolean IsSelfTrapped()
    {
        if (TrapPos == null)
            return false;
        
        IBlockState l_State= mc.world.getBlockState(TrapPos);
        
        return l_State.getBlock() != Blocks.AIR && l_State.getBlock() != Blocks.WATER && l_State.getBlock() != Blocks.LAVA;
    }
}
