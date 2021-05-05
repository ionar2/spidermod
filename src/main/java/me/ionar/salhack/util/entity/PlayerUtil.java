package me.ionar.salhack.util.entity;

import java.text.DecimalFormat;

import me.ionar.salhack.util.Hole.HoleTypes;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtil
{
    private static Minecraft mc = Minecraft.getMinecraft();

    public static int GetItemSlot(Item input)
    {
        if (mc.player == null)
            return 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            
            if (s.isEmpty())
                continue;
            
            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }

    public static int GetRecursiveItemSlot(Item input)
    {
        if (mc.player == null)
            return 0;

        for (int i = mc.player.inventoryContainer.getInventory().size() - 1; i > 0; --i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            
            if (s.isEmpty())
                continue;
            
            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }
    
    public static int GetItemSlotNotHotbar(Item input)
    {
        if (mc.player == null)
            return 0;
        
        for (int i = 9; i < 36; i++)
        {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input)
            {
                return i;
            }
        }
        return -1;
    }
    
    public static int GetItemCount(Item input)
    {
        if (mc.player == null)
            return 0;
        
        int items = 0;

        for (int i = 0; i < 45; i++)
        {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == input)
            {
                items += stack.getCount();
            }
        }

        return items;
    }

    public static boolean CanSeeBlock(BlockPos p_Pos)
    {
        if (mc.player == null)
            return false;
        
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(p_Pos.getX(), p_Pos.getY(), p_Pos.getZ()), false, true, false) == null;
    }
    
    public static boolean isCurrentViewEntity()
    {
        return mc.getRenderViewEntity() == mc.player;
    }

    public static boolean IsEating()
    {
        return mc.player != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemFood && mc.player.isHandActive();
    }
    
    public static int GetItemInHotbar(Item p_Item)
    {
        for (int l_I = 0; l_I < 9; ++l_I)
        {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            
            if (l_Stack != ItemStack.EMPTY)
            {
                if (l_Stack.getItem() == p_Item)
                {
                    return l_I;
                }
            }
        }
        
        return -1;
    }

    public static BlockPos GetLocalPlayerPosFloored()
    {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static BlockPos EntityPosToFloorBlockPos(Entity e)
    {
        return new BlockPos(Math.floor(e.posX), Math.floor(e.posY), Math.floor(e.posZ));
    }
    
    public static float GetHealthWithAbsorption()
    {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }
    
    public static boolean IsPlayerInHole()
    {
        BlockPos blockPos = GetLocalPlayerPosFloored();
        
        IBlockState blockState = mc.world.getBlockState(blockPos);
        
        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]
        { blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west() };

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks)
        {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
                validHorizontalBlocks++;
        }

        if (validHorizontalBlocks < 4)
            return false;

        return true;
    }
    
    public static void PacketFacePitchAndYaw(float p_Pitch, float p_Yaw)
    {
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
            try { Thread.sleep(1); } catch (Exception e) {}
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
            float l_Pitch = p_Pitch;
            float l_Yaw = p_Yaw;
            
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
    }

    public static boolean IsPlayerTrapped()
    {
        BlockPos l_PlayerPos = GetLocalPlayerPosFloored();
        
        final BlockPos[] l_TrapPositions = {
                l_PlayerPos.down(),
                l_PlayerPos.up().up(),
                l_PlayerPos.north(),
                l_PlayerPos.south(),
                l_PlayerPos.east(),
                l_PlayerPos.west(),
                l_PlayerPos.north().up(),
                l_PlayerPos.south().up(),
                l_PlayerPos.east().up(),
                l_PlayerPos.west().up(),
                };
        
        for (BlockPos l_Pos : l_TrapPositions)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State.getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(l_Pos).getBlock() != Blocks.BEDROCK)
                return false;
        }

        return true;
    }

    public static boolean IsEntityTrapped(Entity e)
    {
        BlockPos l_PlayerPos = EntityPosToFloorBlockPos(e);
        
        final BlockPos[] l_TrapPositions = {
                l_PlayerPos.up().up(),
                l_PlayerPos.north(),
                l_PlayerPos.south(),
                l_PlayerPos.east(),
                l_PlayerPos.west(),
                l_PlayerPos.north().up(),
                l_PlayerPos.south().up(),
                l_PlayerPos.east().up(),
                l_PlayerPos.west().up(),
                };
        
        for (BlockPos l_Pos : l_TrapPositions)
        {
            IBlockState l_State = mc.world.getBlockState(l_Pos);
            
            if (l_State.getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(l_Pos).getBlock() != Blocks.BEDROCK)
                return false;
        }

        return true;
    }
    
    public enum FacingDirection
    {
        North,
        South,
        East,
        West,
    }

    public static FacingDirection GetFacing()
    {
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
            case 1:
                return FacingDirection.South;
            case 2:
            case 3:
                return FacingDirection.West;
            case 4:
            case 5:
                return FacingDirection.North;
            case 6:
            case 7:
                return FacingDirection.East;
        }
        return FacingDirection.North;
    }

    final static DecimalFormat Formatter = new DecimalFormat("#.#");
    
    public static float getSpeedInKM()
    {
        final double deltaX = mc.player.posX - mc.player.prevPosX;
        final double deltaZ = mc.player.posZ - mc.player.prevPosZ;
        
        float l_Distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double l_KMH = Math.floor(( l_Distance/1000.0f ) / ( 0.05f/3600.0f ));
        
        String l_Formatter = Formatter.format(l_KMH);
        
        if (!l_Formatter.contains("."))
            l_Formatter += ".0";
        
        return Float.valueOf(l_Formatter);
    }
}
