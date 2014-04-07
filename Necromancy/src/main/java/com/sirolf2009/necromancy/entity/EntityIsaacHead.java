package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

public class EntityIsaacHead extends EntityIsaacBlood
{

    public EntityIsaacHead(World par1World)
    {
        super(par1World);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Max Health - default 20.0D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
        // Follow Range - default 32.0D - min 0.0D - max 2048.0D
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
        // Knockback Resistance - default 0.0D - min 0.0D - max 1.0D
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D);
        // Movement Speed - default 0.699D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        // Attack Damage - default 2.0D - min 0.0D - max Doubt.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
    }

    @Override
    protected Item getDropItem()
    {
        return RegistryNecromancyItems.isaacsHead;
    }
    
    @Override
    public void onDeath(DamageSource par1DamageSource)
    {
        // prevent circular spawning
    }
}
