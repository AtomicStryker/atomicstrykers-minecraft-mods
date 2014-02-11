package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityTearBlood extends EntityTear
{

    public EntityTearBlood(World par1World)
    {
        super(par1World);
        damage = 6;
    }

    public EntityTearBlood(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
        damage = 6;
    }

    public EntityTearBlood(World par1World, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving, float par4, float par5)
    {
        super(par1World, par2EntityLiving, par3EntityLiving, par4, par5);
        damage = 6;
    }

    public EntityTearBlood(World par1World, EntityLivingBase par2EntityLiving, float par3)
    {
        super(par1World, par2EntityLiving, par3);
        damage = 6;
    }

}
