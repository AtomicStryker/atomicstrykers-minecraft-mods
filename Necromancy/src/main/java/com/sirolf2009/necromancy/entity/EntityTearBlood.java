package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityTearBlood extends EntityTear
{

    public EntityTearBlood(World par1World)
    {
        super(par1World);
        doInit();
    }

    public EntityTearBlood(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
        doInit();
    }

    public EntityTearBlood(World par1World, EntityLivingBase par2EntityLiving)
    {
        super(par1World, par2EntityLiving);
        doInit();
    }
    
    private void doInit()
    {
        damage = 6;
        particle = "reddust";
    }

}
