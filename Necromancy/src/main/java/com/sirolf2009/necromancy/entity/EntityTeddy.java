package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityTeddy extends EntityTameable
{

    enum EntityState
    {
        WALKING, DEFENDING, SITTING
    };

    public EntityState entityState;
    
    private float moveSpeed;

    public EntityTeddy(World par1World)
    {
        super(par1World);
        setTamed(true);
        setSitting(true);
        moveSpeed = 0.3F;
        setSize(0.6F, 0.8F);
        setAIMoveSpeed(0.15F);
        entityState = EntityState.WALKING;
        getNavigator().setAvoidsWater(true);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, aiSit);
        tasks.addTask(3, new EntityAIFollowOwner(this, 0.3F, 8F, 5F));
        tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 10F));
        tasks.addTask(6, new EntityAIScareEntities(this, 10F, 7F, moveSpeed, EntityMob.class));
    }
    
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        // Max Health - default 20.0D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8.0D);
        // Follow Range - default 32.0D - min 0.0D - max 2048.0D
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
        // Knockback Resistance - default 0.0D - min 0.0D - max 1.0D
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.0D);
        // Movement Speed - default 0.699D - min 0.0D - max Double.MAX_VALUE
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(moveSpeed);
        // Attack Damage - default 2.0D - min 0.0D - max Doubt.MAX_VALUE
        // this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("state", getStateIndex());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        setStateIndex(par1NBTTagCompound.getInteger("state"));
    }

    private int getStateIndex()
    {
        switch (entityState)
        {
        case WALKING:
            return 0;
        case DEFENDING:
            return 1;
        case SITTING:
            return 2;
        }
        return -1;
    }

    private void setStateIndex(int index)
    {
        switch (index)
        {
        case 0:
            entityState = EntityState.WALKING;
            break;
        case 1:
            entityState = EntityState.DEFENDING;
            break;
        case 2:
            entityState = EntityState.SITTING;
            break;
        }
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    @Override
    public boolean isAIEnabled()
    {
        return true;
    }

    @Override
    protected String getLivingSound()
    {
        return "mob.zombie";
    }

    @Override
    protected String getHurtSound()
    {
        return "mob.zombiehurt";
    }

    @Override
    protected String getDeathSound()
    {
        return "mob.zombiedeath";
    }

    @Override
    protected Item getDropItem()
    {
        return Items.leather;
    }

    @Override
    public boolean attackEntityAsMob(Entity par1Entity)
    {
        return par1Entity.attackEntityFrom(DamageSource.causeMobDamage(this), 3);
    }

    @Override
    public boolean interact(EntityPlayer par1EntityPlayer)
    {
        func_152115_b(par1EntityPlayer.getCommandSenderName());

        switch (entityState)
        {
        case WALKING:
            entityState = EntityState.DEFENDING;
            setSitting(true);
            moveSpeed = 0.1F;
            break;

        case DEFENDING:
            entityState = EntityState.SITTING;
            setSitting(true);
            break;

        case SITTING:
            entityState = EntityState.WALKING;
            setSitting(false);
            moveSpeed = 0.3F;
            break;
        }
        if (getOwner() instanceof EntityPlayer)
        {
            ((EntityPlayer)getOwner()).addChatMessage(new ChatComponentText("Animated Teddy is now " + ((entityState).toString().toLowerCase())));
        }
        return true;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable var1)
    {
        return null;
    }

    @Override
    public EntityLivingBase getOwner()
    {
        return worldObj.getPlayerEntityByName(func_152113_b());
    }

}
