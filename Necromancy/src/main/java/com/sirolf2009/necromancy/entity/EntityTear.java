package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityTear extends EntityThrowable
{
    
    protected float damage = 3f;
    protected String particle = "splash";
    
    public EntityTear(World par1World)
    {
        super(par1World);
    }

    public EntityTear(World world, EntityLivingBase shooter)
    {
        super(world, shooter);
    }
    
    public EntityTear(World world, EntityLivingBase shooter, EntityLivingBase target)
    {
        this(world, shooter);
        
        posY = shooter.posY + shooter.getEyeHeight() - 0.1D;
        double xDiff = target.posX - shooter.posX;
        double yDiff = target.boundingBox.minY + (target.height / 3.0F) - posY;
        double zDiff = target.posZ - shooter.posZ;
        double distEuclid = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        if (distEuclid >= 1.0E-7D)
        {
            float rot = (float) ((Math.atan2(zDiff, xDiff) * 180.0D / Math.PI) - 90.0F);
            float pitch = (float) (-(Math.atan2(yDiff, distEuclid) * 180.0D / Math.PI));
            double extraX = xDiff / distEuclid;
            double extraZ = zDiff / distEuclid;
            setLocationAndAngles(shooter.posX + extraX, this.posY, shooter.posZ + extraZ, rot, pitch);
            yOffset = 0.0F;
            setThrowableHeading(xDiff, yDiff + (distEuclid * 0.2D), zDiff, 1.6F, 2F);
        }
    }

    @Override
    protected void onImpact(MovingObjectPosition mObjPos)
    {
        if (mObjPos.entityHit != null)
        {
            mObjPos.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
        }

        for (int i = 0; i < 8; ++i)
        {
            worldObj.spawnParticle(particle, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
        }

        if (!worldObj.isRemote)
        {
            setDead();
        }
    }
}