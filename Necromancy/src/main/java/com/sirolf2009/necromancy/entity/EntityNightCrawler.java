package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityNightCrawler extends EntityMob
{

    public EntityNightCrawler(World par1World)
    {
        super(par1World);
        setSize(0.6F, 1F);
        float moveSpeed = 0.25F;
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        tasks.addTask(3, new EntityAIRestrictSun(this));
        tasks.addTask(4, new EntityAIFleeSun(this, moveSpeed));
        tasks.addTask(5, new EntityAIWander(this, moveSpeed));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
    }
    
    @Override
    protected boolean isAIEnabled()
    {
        return true;
    }
    
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Max Health - default 20.0D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(35.0D);
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
    protected void dropRareDrop(int par1)
    {
        entityDropItem(new ItemStack(Items.ender_pearl, 1, 1), 0.0F);
    }
    
    @Override
    protected Item getDropItem()
    {
        return Items.rotten_flesh;
    }
    
    @Override
    protected String getLivingSound()
    {
        return rand.nextBoolean() ? "necromancy:nightcrawler.howl" : "necromancy:nightcrawler.scream";
    }
    
    @Override
    protected String getHurtSound()
    {
        return "mob.endermen.hit";
    }
    
    @Override
    protected String getDeathSound()
    {
        return "mob.endermen.death";
    }
    
    @Override
    public boolean attackEntityAsMob(Entity target)
    {
        boolean flag = super.attackEntityAsMob(target);
        if (flag && target instanceof EntityLivingBase)
        {
            ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.wither.id, 120, 0));
        }
        return flag;
    }
}
