package com.sirolf2009.necromancy.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityBoltShadow extends EntityThrowable
{

    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile = Blocks.air;
    private int inData = 0;
    private boolean inGround = false;

    public int arrowShake = 0;

    public Entity shootingEntity;
    private int ticksInAir = 0;
    private double damage = 2.0D;

    public EntityBoltShadow(World par1World, EntityLiving par2EntityLiving, EntityLiving par3EntityLiving, float par4, float par5)
    {
        super(par1World);
        shootingEntity = par2EntityLiving;

        posY = par2EntityLiving.posY + par2EntityLiving.getEyeHeight() - 0.10000000149011612D;
        double var6 = par3EntityLiving.posX - par2EntityLiving.posX;
        double var8 = par3EntityLiving.posY + par3EntityLiving.getEyeHeight() - 0.699999988079071D - posY;
        double var10 = par3EntityLiving.posZ - par2EntityLiving.posZ;
        double var12 = MathHelper.sqrt_double(var6 * var6 + var10 * var10);

        if (var12 >= 1.0E-7D)
        {
            float var14 = (float) (Math.atan2(var10, var6) * 180.0D / Math.PI) - 90.0F;
            float var15 = (float) -(Math.atan2(var8, var12) * 180.0D / Math.PI);
            double var16 = var6 / var12;
            double var18 = var10 / var12;
            this.setLocationAndAngles(par2EntityLiving.posX + var16, posY, par2EntityLiving.posZ + var18, var14, var15);
            yOffset = 0.0F;
            float var20 = (float) var12 * 0.2F;
            this.setThrowableHeading(var6, var8 + var20, var10, par4, par5);
        }
    }

    public EntityBoltShadow(World par1World, EntityLiving par2EntityLiving, float par3)
    {
        super(par1World);
        shootingEntity = par2EntityLiving;

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
    public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
    {
        float var9 = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= var9;
        par3 /= var9;
        par5 /= var9;
        par1 += rand.nextGaussian() * 0.007499999832361937D * par8;
        par3 += rand.nextGaussian() * 0.007499999832361937D * par8;
        par5 += rand.nextGaussian() * 0.007499999832361937D * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        motionX = par1;
        motionY = par3;
        motionZ = par5;
        float var10 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
        prevRotationYaw = rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(par3, var10) * 180.0D / Math.PI);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float var1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
            prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, var1) * 180.0D / Math.PI);
        }

        Block var16 = worldObj.getBlock(xTile, yTile, zTile);

        if (var16 != Blocks.air)
        {
            var16.setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
            AxisAlignedBB var2 = var16.getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);

            if (var2 != null && var2.isVecInside(worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ)))
            {
                inGround = true;
            }
        }

        if (arrowShake > 0)
        {
            --arrowShake;
        }

        if (inGround)
        {
            Block var18 = worldObj.getBlock(xTile, yTile, zTile);
            int var19 = worldObj.getBlockMetadata(xTile, yTile, zTile);

            if (var18 == inTile && var19 == inData)
            {
                this.setDead();
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
            Vec3 var17 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
            Vec3 var3 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition var4 = worldObj.rayTraceBlocks(var17, var3, false);
            var17 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
            var3 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);

            if (var4 != null)
            {
                var3 = worldObj.getWorldVec3Pool().getVecFromPool(var4.hitVec.xCoord, var4.hitVec.yCoord, var4.hitVec.zCoord);
            }

            Entity var5 = null;
            List<Entity> var6 =
                    worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
            double var7 = 0.0D;
            int var9;
            float var11;

            for (var9 = 0; var9 < var6.size(); ++var9)
            {
                Entity var10 = (Entity) var6.get(var9);

                if (var10.canBeCollidedWith() && (var10 != shootingEntity || ticksInAir >= 5))
                {
                    var11 = 0.3F;
                    AxisAlignedBB var12 = var10.boundingBox.expand(var11, var11, var11);
                    MovingObjectPosition var13 = var12.calculateIntercept(var17, var3);

                    if (var13 != null)
                    {
                        double var14 = var17.distanceTo(var13.hitVec);

                        if (var14 < var7 || var7 == 0.0D)
                        {
                            var5 = var10;
                            var7 = var14;
                        }
                    }
                }
            }

            if (var5 != null)
            {
                var4 = new MovingObjectPosition(var5);
            }

            float var20;

            if (var4 != null)
            {
                if (var4.entityHit != null)
                {
                    var20 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    int var23 = MathHelper.ceiling_double_int(var20 * damage);

                    DamageSource var21 = null;

                    var21 = DamageSource.causeIndirectMagicDamage(this, shootingEntity);

                    if (var4.entityHit.attackEntityFrom(var21, var23))
                    {
                        if (var4.entityHit instanceof EntityLiving)
                        {
                            if (!worldObj.isRemote)
                            {
                                EntityLiving var24 = (EntityLiving) var4.entityHit;
                                var24.setArrowCountInEntity(var24.getArrowCountInEntity() + 1);
                            }
                            float var25 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);

                            if (var25 > 0.0F)
                            {
                                var4.entityHit.addVelocity(motionX * 5D * 0.6000000238418579D / var25, 0.1D, motionZ * 5 * 0.6000000238418579D
                                        / var25);
                            }
                        }

                        this.playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
                        this.setDead();
                    }
                    else
                    {
                        motionX *= -0.10000000149011612D;
                        motionY *= -0.10000000149011612D;
                        motionZ *= -0.10000000149011612D;
                        rotationYaw += 180.0F;
                        prevRotationYaw += 180.0F;
                        ticksInAir = 0;
                    }
                }
                else
                {
                    xTile = var4.blockX;
                    yTile = var4.blockY;
                    zTile = var4.blockZ;
                    inTile = worldObj.getBlock(xTile, yTile, zTile);
                    inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
                    motionX = (float) (var4.hitVec.xCoord - posX);
                    motionY = (float) (var4.hitVec.yCoord - posY);
                    motionZ = (float) (var4.hitVec.zCoord - posZ);
                    var20 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    posX -= motionX / var20 * 0.05000000074505806D;
                    posY -= motionY / var20 * 0.05000000074505806D;
                    posZ -= motionZ / var20 * 0.05000000074505806D;
                    this.playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
                    inGround = true;
                    arrowShake = 7;

                    if (inTile != Blocks.air)
                    {
                        inTile.onEntityCollidedWithBlock(worldObj, xTile, yTile, zTile, this);
                    }
                }
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            var20 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

            for (rotationPitch = (float) (Math.atan2(motionY, var20) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -=
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
            float var22 = 0.99F;
            var11 = 0.05F;

            if (this.isInWater())
            {
                for (int var26 = 0; var26 < 4; ++var26)
                {
                    float var27 = 0.25F;
                    worldObj.spawnParticle("bubble", posX - motionX * var27, posY - motionY * var27, posZ - motionZ * var27, motionX, motionY,
                            motionZ);
                }

                var22 = 0.8F;
            }

            motionX *= var22;
            motionY *= var22;
            motionZ *= var22;
            motionY -= var11;
            this.setPosition(posX, posY, posZ);
            func_145775_I(); // doBlockCollisions();
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
        par1NBTTagCompound.setDouble("damage", damage);
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
            damage = par1NBTTagCompound.getDouble("damage");
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
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public void setDamage(double par1)
    {
        damage = par1;
    }

    public double getDamage()
    {
        return damage;
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    @Override
    public boolean canAttackWithItem()
    {
        return false;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    protected void init()
    {
    }

    @Override
    protected void onImpact(MovingObjectPosition var1)
    {
    }

}
