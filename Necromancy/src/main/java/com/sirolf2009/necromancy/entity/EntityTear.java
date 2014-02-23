package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
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

    public EntityTear(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }

    public EntityTear(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
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