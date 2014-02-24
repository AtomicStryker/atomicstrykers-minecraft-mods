package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityIsaacNormal extends EntityIsaacBody implements IRangedAttackMob
{
    
    public EntityIsaacNormal(World par1World)
    {
        super(par1World);
        if (!par1World.isRemote)
        {
            tasks.addTask(1, new EntityAIArrowAttack(this, 0.25f, 18, 50F));
        }
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f)
    {
        playSound("necromancy:tear", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        worldObj.spawnEntityInWorld(new EntityTear(worldObj, this, entitylivingbase));
    }
    
    @Override
    protected String getLivingSound()
    {
        return "mob.ghast.moan";
    }
    
    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        super.onDeath(par1DamageSource);
        if (!worldObj.isRemote)
        {
            EntityIsaacBlood isaac = new EntityIsaacBlood(worldObj);
            isaac.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            worldObj.spawnEntityInWorld(isaac);
        }
    }
    
}
