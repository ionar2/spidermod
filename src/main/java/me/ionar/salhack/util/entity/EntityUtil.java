package me.ionar.salhack.util.entity;

import java.awt.Point;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.util.MathUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityUtil
{

    public static boolean isPassive(Entity e)
    {
        if (e instanceof EntityWolf && ((EntityWolf) e).isAngry())
            return false;
        if (e instanceof EntityAnimal || e instanceof EntityAgeable || e instanceof EntityTameable
                || e instanceof EntityAmbientCreature || e instanceof EntitySquid)
            return true;
        if (e instanceof EntityIronGolem && ((EntityIronGolem) e).getRevengeTarget() == null)
            return true;
        return false;
    }

    public static boolean isLiving(Entity e)
    {
        return e instanceof EntityLivingBase;
    }

    public static boolean isFakeLocalPlayer(Entity entity)
    {
        return entity != null && entity.getEntityId() == -100 && Minecraft.getMinecraft().player != entity;
    }

    /**
     * Find the entities interpolated amount
     */
    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z)
    {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, 0 * y,
                (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, Vec3d vec)
    {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks)
    {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static boolean isMobAggressive(Entity entity)
    {
        if (entity instanceof EntityPigZombie)
        {
            // arms raised = aggressive, angry = either game or we have set the anger
            // cooldown
            if (((EntityPigZombie) entity).isArmsRaised() || ((EntityPigZombie) entity).isAngry())
            {
                return true;
            }
        }
        else if (entity instanceof EntityWolf)
        {
            return ((EntityWolf) entity).isAngry()
                    && !Minecraft.getMinecraft().player.equals(((EntityWolf) entity).getOwner());
        }
        else if (entity instanceof EntityEnderman)
        {
            return ((EntityEnderman) entity).isScreaming();
        }
        return isHostileMob(entity);
    }

    /**
     * If the mob by default wont attack the player, but will if the player attacks
     * it
     */
    public static boolean isNeutralMob(Entity entity)
    {
        return entity instanceof EntityPigZombie || entity instanceof EntityWolf || entity instanceof EntityEnderman;
    }

    /**
     * If the mob is friendly (not aggressive)
     */
    public static boolean isFriendlyMob(Entity entity)
    {
        return (entity.isCreatureType(EnumCreatureType.CREATURE, false) && !EntityUtil.isNeutralMob(entity))
                || (entity.isCreatureType(EnumCreatureType.AMBIENT, false)) || entity instanceof EntityVillager
                || entity instanceof EntityIronGolem || (isNeutralMob(entity) && !EntityUtil.isMobAggressive(entity));
    }

    /**
     * If the mob is hostile
     */
    public static boolean isHostileMob(Entity entity)
    {
        return (entity.isCreatureType(EnumCreatureType.MONSTER, false) && !EntityUtil.isNeutralMob(entity));
    }

    /**
     * Find the entities interpolated position
     */
    public static Vec3d getInterpolatedPos(Entity entity, float ticks)
    {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)
                .add(getInterpolatedAmount(entity, ticks));
    }

    public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks)
    {
        return getInterpolatedPos(entity, ticks).subtract(Minecraft.getMinecraft().getRenderManager().renderPosX,
                Minecraft.getMinecraft().getRenderManager().renderPosY,
                Minecraft.getMinecraft().getRenderManager().renderPosZ);
    }

    public static boolean isInWater(Entity entity)
    {
        if (entity == null)
            return false;

        double y = entity.posY + 0.01;

        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++)
            {
                BlockPos pos = new BlockPos(x, (int) y, z);

                if (Minecraft.getMinecraft().world.getBlockState(pos).getBlock() instanceof BlockLiquid)
                    return true;
            }

        return false;
    }

    public static boolean isDrivenByPlayer(Entity entityIn)
    {
        return Minecraft.getMinecraft().player != null && entityIn != null
                && entityIn.equals(Minecraft.getMinecraft().player.getRidingEntity());
    }

    public static boolean isAboveWater(Entity entity)
    {
        return isAboveWater(entity, false);
    }

    public static boolean isAboveWater(Entity entity, boolean packet)
    {
        if (entity == null)
            return false;

        double y = entity.posY - (packet ? 0.03 : (EntityUtil.isPlayer(entity) ? 0.2 : 0.5)); // increasing this seems
                                                                                              // to flag more in NCP but
                                                                                              // needs to be increased
                                                                                              // so the player lands on
                                                                                              // solid water

        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++)
            {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (Minecraft.getMinecraft().world.getBlockState(pos).getBlock() instanceof BlockLiquid)
                    return true;
            }

        return false;
    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me)
    {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        // to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]
        { yaw, pitch };
    }

    public static boolean isPlayer(Entity entity)
    {
        return entity instanceof EntityPlayer;
    }

    public static double getRelativeX(float yaw)
    {
        return (double) (MathHelper.sin(-yaw * 0.017453292F));
    }

    public static double getRelativeZ(float yaw)
    {
        return (double) (MathHelper.cos(yaw * 0.017453292F));
    }
    
    public static int GetPlayerMS(EntityPlayer p_Player)
    {
        if (p_Player.getUniqueID() == null)
            return 0;
        
        final NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(p_Player.getUniqueID());

        if (playerInfo == null)
            return 0;
        
        return playerInfo.getResponseTime();
    }

    public static Vec3d CalculateExpectedPosition(EntityPlayer p_Player)
    {
        Vec3d l_Position = new Vec3d(p_Player.posX, p_Player.posY, p_Player.posZ);
        
        if (p_Player.lastTickPosX != p_Player.posX && p_Player.lastTickPosY != p_Player.posY && p_Player.lastTickPosZ != p_Player.posZ)
            return l_Position;
        
        int l_PlayerMS = GetPlayerMS(p_Player);

        final double deltaX = p_Player.posX - p_Player.prevPosX;
        final double deltaZ = p_Player.posZ - p_Player.prevPosZ;
        final float tickRate = (Minecraft.getMinecraft().timer.tickLength / 1000.0f);
        
        float l_Distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        double l_Facing = MathUtil.calculateAngle(p_Player.posX, p_Player.posZ, p_Player.lastTickPosX, p_Player.lastTickPosZ) / 45;
        
        return new Vec3d(
                p_Player.posX + (Math.cos(l_Facing) * l_Distance),
                p_Player.posY,
                p_Player.posZ + (Math.sin(l_Facing) * l_Distance)
                );
    }
    
    public static double GetDistance(double p_X, double p_Y, double p_Z, double x, double y, double z)
    {
        double d0 = p_X - x;
        double d1 = p_Y - y;
        double d2 = p_Z - z;
        return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
    
    public static double GetDistanceOfEntityToBlock(Entity p_Entity, BlockPos p_Pos)
    {
        return GetDistance(p_Entity.posX, p_Entity.posY, p_Entity.posZ, p_Pos.getX(), p_Pos.getY(), p_Pos.getZ());
    }
}
