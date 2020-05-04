package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.MathUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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

public class SurroundModule extends Module
{
    public final Value<Boolean> disable = new Value<Boolean>("Toggles", new String[]
    { "Toggles", "Disables" }, "Will toggle off after a place", false);
    public final Value<Boolean> ToggleOffGround = new Value<Boolean>("ToggleOffGround", new String[]
    { "Toggles", "Disables" }, "Will toggle off after a place", false);
    public final Value<CenterModes> CenterMode = new Value<CenterModes>("Center", new String[]
    { "Center" }, "Moves you to center of block", CenterModes.NCP);
    
    public final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]
    { "rotate" }, "Rotate", true);
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[] {"BPT"}, "Blocks per tick", 4, 1, 10, 1);
    
    public enum CenterModes
    {
        Teleport,
        NCP,
        None,
    }

    public SurroundModule()
    {
        super("Surround", new String[]
        { "NoCrystal" }, "Automatically surrounds you with obsidian in the four cardinal direrctions", "NONE", 0x5324DB, ModuleType.COMBAT);
    }
    
    private Vec3d Center = Vec3d.ZERO;
    
    @Override
    public String getMetaData()
    {
        return CenterMode.getValue().toString();
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

        Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);
        
        if (CenterMode.getValue() != CenterModes.None)
        {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }
        
        if (CenterMode.getValue() == CenterModes.Teleport)
        {
          //  
            
        //    double l_MotionX = l_Center.x-mc.player.posX;
        //    double l_MotionZ = l_Center.z-mc.player.posZ;
            
       //     SalHack.INSTANCE.logChat(String.format("%s %s %s %s", l_MotionX, l_MotionZ, mc.player.getPositionVector(), l_Center.toString()));
            
     //       mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + l_MotionX/2, l_Center.y, mc.player.posZ + l_MotionZ/2, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
            mc.player.setPosition(Center.x, Center.y, Center.z);
            
           // mc.player.moveToBlockPosAndAngles(pos, rotationYawIn, rotationPitchIn);
        }
    }

    @Override
    public void toggleNoSave()
    {
        
    }

    @EventHandler
    private Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (p_Event.getEra() != Era.PRE)
            return;
        
        /// NCP Centering
        if (Center != Vec3d.ZERO && CenterMode.getValue() == CenterModes.NCP)
        {
            double l_XDiff = Math.abs(Center.x - mc.player.posX);
            double l_ZDiff = Math.abs(Center.z - mc.player.posZ);
            
            if (l_XDiff <= 0.1 && l_ZDiff <= 0.1)
            {
                Center = Vec3d.ZERO;
            }
            else
            {
                double l_MotionX = Center.x-mc.player.posX;
                double l_MotionZ = Center.z-mc.player.posZ;
                
                mc.player.motionX = l_MotionX/2;
                mc.player.motionZ = l_MotionZ/2;
            }
        }
        
        if (!mc.player.onGround && !mc.player.prevOnGround)
        {
            if (ToggleOffGround.getValue())
            {
                toggle();
                SalHack.SendMessage("[Surround]: You are off ground! toggling!");
                return;
            }
        }

        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

        final BlockPos north = interpPos.north();
        final BlockPos south = interpPos.south();
        final BlockPos east = interpPos.east();
        final BlockPos west = interpPos.west();
        
        BlockPos[] l_Array = {north, south, east, west};
        
        /// We don't need to do anything if we are not surrounded
        if (IsSurrounded(mc.player))
            return;
        
        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1)
        {
            if ((mc.player.onGround))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                int l_BlocksPerTick = BlocksPerTick.getValue();

                for (BlockPos l_Pos : l_Array)
                {
                    ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);
                    
                    if (l_Result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(l_Pos).getMaterial().isReplaceable())
                        continue;
                    
                    if (l_Result == ValidResult.NoNeighbors)
                    {
                        final BlockPos[] l_Test = {  l_Pos.down(), l_Pos.north(), l_Pos.south(), l_Pos.east(), l_Pos.west(), l_Pos.up(), };

                        for (BlockPos l_Pos2 : l_Test)
                        {
                            ValidResult l_Result2 = BlockInteractionHelper.valid(l_Pos2);

                            if (l_Result2 == ValidResult.NoNeighbors || l_Result2 == ValidResult.NoEntityCollision)
                                continue;
                            
                            BlockInteractionHelper.place (l_Pos2, 5.0f, false, false);
                            p_Event.cancel();
                            float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(l_Pos2.getX(), l_Pos2.getY(), l_Pos2.getZ()));
                            ProcessBlizzFacing(rotations[0], rotations[1]);
                            break;
                        }
                        
                        continue;
                    }
                    
                    BlockInteractionHelper.place (l_Pos, 5.0f, false, false);

                    p_Event.cancel();

                    float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(l_Pos.getX(), l_Pos.getY(), l_Pos.getZ()));
                    ProcessBlizzFacing(rotations[0], rotations[1]);
                    if (--l_BlocksPerTick <= 0)
                        break;
                }

                if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN))
                {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();

                if (this.disable.getValue())
                {
                    this.toggle();
                }
            }
        }
    });
    
    private void ProcessBlizzFacing(float p_Yaw, float p_Pitch)
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

    public boolean IsSurrounded(EntityPlayer p_Who)
    {
        final Vec3d l_PlayerPos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos l_InterpPos = new BlockPos(l_PlayerPos.x, l_PlayerPos.y, l_PlayerPos.z);

        final BlockPos l_North = l_InterpPos.north();
        final BlockPos l_South = l_InterpPos.south();
        final BlockPos l_East = l_InterpPos.east();
        final BlockPos l_West = l_InterpPos.west();
        
        BlockPos[] l_Array = {l_North, l_South, l_East, l_West};
        
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

    public Vec3d GetCenter(double posX, double posY, double posZ)
    {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D ;
        
        return new Vec3d(x, y, z);
    }

    public boolean HasObsidian()
    {
        return findStackHotbar(Blocks.OBSIDIAN) != -1;
    }
}
