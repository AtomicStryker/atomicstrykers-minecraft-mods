package com.sirolf2009.necromancy.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityTear extends Entity implements IProjectile
{

    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile = Blocks.air;
    private int inData = 0;
    private boolean inGround = false;

    /** 1 if the player can pick up the arrow */
    public int canBePickedUp = 0;

    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake = 0;

    /** The owner of this arrow. */
    public EntityLivingBase shootingEntity;
    private int ticksInAir = 0;
    private int damage = 5;

    /** The amount of knockback an arrow applies when it hits a mob. */
    private int knockbackStrength;

    public EntityTear(World par1World)
    {
        super(par1World);
        renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
    }

    public EntityTear(World par1World, double par2, double par4, double par6)
    {
        super(par1World);
        renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
        this.setPosition(par2, par4, par6);
        yOffset = 0.0F;
    }

    public EntityTear(World par1World, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving, float par4, float par5)
    {
        super(par1World);
        renderDistanceWeight = 10.0D;
        shootingEntity = par2EntityLiving;

        if (par2EntityLiving instanceof EntityPlayer)
        {
            canBePickedUp = 1;
        }

        posY = par2EntityLiving.posY + par2EntityLiving.getEyeHeight() - 0.10000000149011612D;
        double d0 = par3EntityLiving.posX - par2EntityLiving.posX;
        double d1 = par3EntityLiving.boundingBox.minY + par3EntityLiving.height / 3.0F - posY;
        double d2 = par3EntityLiving.posZ - par2EntityLiving.posZ;
        double d3 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D)
        {
            float f2 = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
            float f3 = (float) -(Math.atan2(d1, d3) * 180.0D / Math.PI);
            double d4 = d0 / d3;
            double d5 = d2 / d3;
            this.setLocationAndAngles(par2EntityLiving.posX + d4, posY, par2EntityLiving.posZ + d5, f2, f3);
            yOffset = 0.0F;
            float f4 = (float) d3 * 0.2F;
            this.setThrowableHeading(d0, d1 + f4, d2, par4, par5);
        }
    }

    public EntityTear(World par1World, EntityLivingBase par2EntityLiving, float par3)
    {
        super(par1World);
        renderDistanceWeight = 10.0D;
        shootingEntity = par2EntityLiving;

        if (par2EntityLiving instanceof EntityPlayer)
        {
            canBePickedUp = 1;
        }

        this.setSize(0.5F, 0.5F);
        this.setLocationAndAngles(par2EntityLiving.posX, par2EntityLiving.posY + par2EntityLiving.getEyeHeight(), par2EntityLiving.posZ,
                par2EntityLiving.rotationYaw, par2EntityLiving.rotationPitch);
        posX -= MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        motionX = -MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI);
        motionZ = MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI);
        motionY = -MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI);
        this.setThrowableHeading(motionX, motionY, motionZ, par3 * 1.5F, 1.0F);
    }

    @Override
    protected void entityInit()
    {
        dataWatcher.addObject(16, Byte.valueOf((byte) 0));
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
     * direction.
     */
    @Override
    public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
    {
        float f2 = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= f2;
        par3 /= f2;
        par5 /= f2;
        par1 += rand.nextGaussian() * (rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * par8;
        par3 += rand.nextGaussian() * (rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * par8;
        par5 += rand.nextGaussian() * (rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        motionX = par1;
        motionY = par3;
        motionZ = par5;
        float f3 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
        prevRotationYaw = rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(par3, f3) * 180.0D / Math.PI);
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
    {
        this.setPosition(par1, par3, par5);
        this.setRotation(par7, par8);
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double par1, double par3, double par5)
    {
        motionX = par1;
        motionY = par3;
        motionZ = par5;

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
            prevRotationYaw = rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (Math.atan2(par3, f) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch;
            prevRotationYaw = rotationYaw;
            this.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, f) * 180.0D / Math.PI);
        }

        Block i = worldObj.getBlock(xTile, yTile, zTile);

        i.setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
        AxisAlignedBB axisalignedbb = i.getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);

        if (axisalignedbb != null && axisalignedbb.isVecInside(worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ)))
        {
            inGround = true;
        }

        if (arrowShake > 0)
        {
            --arrowShake;
        }

        if (inGround)
        {
            Block j = worldObj.getBlock(xTile, yTile, zTile);
            int k = worldObj.getBlockMetadata(xTile, yTile, zTile);

            if (j == inTile && k == inData)
            {
                setDead();
            }
            else
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInAir = 0;
            }
        }
        else
        {
            ++ticksInAir;
            Vec3 vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
            Vec3 vec31 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec3, vec31, false);
            vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
            vec31 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);

            if (movingobjectposition != null)
            {
                vec31 =
                        worldObj.getWorldVec3Pool().getVecFromPool(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord,
                                movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            @SuppressWarnings("rawtypes")
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            int l;
            float f1;

            for (l = 0; l < list.size(); ++l)
            {
                Entity entity1 = (Entity) list.get(l);

                if (entity1.canBeCollidedWith() && (entity1 != shootingEntity || ticksInAir >= 5))
                {
                    f1 = 0.3F;
                    AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand(f1, f1, f1);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null)
                    {
                        double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.entityHit;

                if (entityplayer.capabilities.disableDamage || shootingEntity instanceof EntityPlayer
                        && !((EntityPlayer) shootingEntity).canAttackPlayer(entityplayer))
                {
                    movingobjectposition = null;
                }
            }

            float f2;
            float f3;

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {

                    DamageSource damagesource = null;

                    damagesource = DamageSource.generic;
                    if (movingobjectposition.entityHit.attackEntityFrom(damagesource, damage))
                    {
                        if (movingobjectposition.entityHit instanceof EntityLiving)
                        {
                            EntityLiving entityliving = (EntityLiving) movingobjectposition.entityHit;

                            if (!worldObj.isRemote)
                            {
                                entityliving.setArrowCountInEntity(entityliving.getArrowCountInEntity() + 1);
                            }

                            if (knockbackStrength > 0)
                            {
                                f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);

                                if (f3 > 0.0F)
                                {
                                    movingobjectposition.entityHit.addVelocity(motionX * knockbackStrength * 0.6000000238418579D / f3, 0.1D, motionZ
                                            * knockbackStrength * 0.6000000238418579D / f3);
                                }
                            }

                            if (shootingEntity != null)
                            {
                                Enchantment.thorns.func_151367_b(shootingEntity, entityliving, rand.nextInt(6));
                            }

                            if (shootingEntity != null && movingobjectposition.entityHit != shootingEntity
                                    && movingobjectposition.entityHit instanceof EntityPlayer && shootingEntity instanceof EntityPlayerMP)
                            {
                                ((EntityPlayerMP) shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0));
                            }
                        }

                        this.playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));

                        if (!(movingobjectposition.entityHit instanceof EntityEnderman))
                        {
                            this.setDead();
                        }
                    }
                    else
                    {
                        movingobjectposition.entityHit.attackEntityFrom(damagesource, damage);
                    }
                }
                else
                {
                    xTile = movingobjectposition.blockX;
                    yTile = movingobjectposition.blockY;
                    zTile = movingobjectposition.blockZ;
                    inTile = worldObj.getBlock(xTile, yTile, zTile);
                    inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
                    motionX = (float) (movingobjectposition.hitVec.xCoord - posX);
                    motionY = (float) (movingobjectposition.hitVec.yCoord - posY);
                    motionZ = (float) (movingobjectposition.hitVec.zCoord - posZ);
                    f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    posX -= motionX / f2 * 0.05000000074505806D;
                    posY -= motionY / f2 * 0.05000000074505806D;
                    posZ -= motionZ / f2 * 0.05000000074505806D;
                    this.playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
                    inGround = true;
                    arrowShake = 7;
                    this.setIsCritical(false);

                    if (inTile != Blocks.air)
                    {
                        inTile.onEntityCollidedWithBlock(worldObj, xTile, yTile, zTile, this);
                    }
                }
            }

            if (this.getIsCritical())
            {
                for (l = 0; l < 4; ++l)
                {
                    worldObj.spawnParticle("crit", posX + motionX * l / 4.0D, posY + motionY * l / 4.0D, posZ + motionZ * l / 4.0D, -motionX,
                            -motionY + 0.2D, -motionZ);
                }
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            f2 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

            for (rotationPitch = (float) (Math.atan2(motionY, f2) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -=
                    360.0F)
            {
                ;
            }

            while (rotationPitch - prevRotationPitch >= 180.0F)
            {
                prevRotationPitch += 360.0F;
            }

            while (rotationYaw - prevRotationYaw < -180.0F)
            {
                prevRotationYaw -= 360.0F;
            }

            while (rotationYaw - prevRotationYaw >= 180.0F)
            {
                prevRotationYaw += 360.0F;
            }

            rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
            rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
            float f4 = 0.99F;
            f1 = 0.05F;

            if (this.isInWater())
            {
                for (int j1 = 0; j1 < 4; ++j1)
                {
                    f3 = 0.25F;
                    worldObj.spawnParticle("bubble", posX - motionX * f3, posY - motionY * f3, posZ - motionZ * f3, motionX, motionY, motionZ);
                }

                f4 = 0.8F;
            }

            motionX *= f4;
            motionY *= f4;
            motionZ *= f4;
            motionY -= f1;
            this.setPosition(posX, posY, posZ);
            this.func_145775_I(); // doBlockCollisions
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setShort("xTile", (short) xTile);
        par1NBTTagCompound.setShort("yTile", (short) yTile);
        par1NBTTagCompound.setShort("zTile", (short) zTile);
        par1NBTTagCompound.setByte("inData", (byte) inData);
        par1NBTTagCompound.setByte("shake", (byte) arrowShake);
        par1NBTTagCompound.setByte("inGround", (byte) (inGround ? 1 : 0));
        par1NBTTagCompound.setByte("pickup", (byte) canBePickedUp);
        par1NBTTagCompound.setInteger("damage", damage);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        xTile = par1NBTTagCompound.getShort("xTile");
        yTile = par1NBTTagCompound.getShort("yTile");
        zTile = par1NBTTagCompound.getShort("zTile");
        inData = par1NBTTagCompound.getByte("inData") & 255;
        arrowShake = par1NBTTagCompound.getByte("shake") & 255;
        inGround = par1NBTTagCompound.getByte("inGround") == 1;

        if (par1NBTTagCompound.hasKey("damage"))
        {
            damage = par1NBTTagCompound.getInteger("damage");
        }

        if (par1NBTTagCompound.hasKey("pickup"))
        {
            canBePickedUp = par1NBTTagCompound.getByte("pickup");
        }
        else if (par1NBTTagCompound.hasKey("player"))
        {
            canBePickedUp = par1NBTTagCompound.getBoolean("player") ? 1 : 0;
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    @Override
    public void onCollideWithPlayer(EntityPlayer par1EntityPlayer)
    {
        if (!worldObj.isRemote && inGround && arrowShake <= 0)
        {
            boolean flag = canBePickedUp == 1 || canBePickedUp == 2 && par1EntityPlayer.capabilities.isCreativeMode;

            if (canBePickedUp == 1 && !par1EntityPlayer.inventory.addItemStackToInventory(new ItemStack(Items.arrow, 1)))
            {
                flag = false;
            }

            if (flag)
            {
                this.playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                par1EntityPlayer.onItemPickup(this, 1);
                this.setDead();
            }
        }
    }

    /**
     * returns if this entity triggers Blocks.onEntityWalking on the blocks they
     * walk on. used for spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public void setDamage(int par1)
    {
        damage = par1;
    }

    public double getDamage()
    {
        return damage;
    }

    /**
     * Sets the amount of knockback the arrow applies when it hits a mob.
     */
    public void setKnockbackStrength(int par1)
    {
        knockbackStrength = par1;
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    @Override
    public boolean canAttackWithItem()
    {
        return false;
    }

    /**
     * Whether the arrow has a stream of critical hit particles flying behind
     * it.
     */
    public void setIsCritical(boolean par1)
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);

        if (par1)
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte) (b0 | 1)));
        }
        else
        {
            dataWatcher.updateObject(16, Byte.valueOf((byte) (b0 & -2)));
        }
    }

    /**
     * Whether the arrow has a stream of critical hit particles flying behind
     * it.
     */
    public boolean getIsCritical()
    {
        byte b0 = dataWatcher.getWatchableObjectByte(16);
        return (b0 & 1) != 0;
    }
}