package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityIsaacBlood extends EntityIsaacNormal
{

    public EntityIsaacBlood(World par1World)
    {
        super(par1World);
    }
    
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Max Health - default 20.0D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(75.0D);
        // Follow Range - default 32.0D - min 0.0D - max 2048.0D
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
        // Knockback Resistance - default 0.0D - min 0.0D - max 1.0D
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D);
        // Movement Speed - default 0.699D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        // Attack Damage - default 2.0D - min 0.0D - max Doubt.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase par1EntityLiving, float par2)
    {
        playSound("necromancy:tear", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        worldObj.spawnEntityInWorld(new EntityTearBlood(worldObj, this, par1EntityLiving));
    }
    
    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        if (!worldObj.isRemote)
        {
            EntityIsaacHead head = new EntityIsaacHead(worldObj);
            EntityIsaacBody body = new EntityIsaacBody(worldObj);
            head.setLocationAndAngles(posX, posY + 1, posZ, rotationYaw, rotationPitch);
            body.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            worldObj.spawnEntityInWorld(head);
            worldObj.spawnEntityInWorld(body);
        }
    }

}
