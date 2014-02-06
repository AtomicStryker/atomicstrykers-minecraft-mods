package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIMinion extends EntityAIBase
{

    public EntityAIMinion(EntityMinion entityMinion)
    {
        minion = entityMinion;
        minion.tasks.addTask(1, new net.minecraft.entity.ai.EntityAISwimming(minion));
        minion.tasks.addTask(3, new net.minecraft.entity.ai.EntityAIAttackOnCollide(minion, 0.3F, true));
        minion.tasks.addTask(4, new net.minecraft.entity.ai.EntityAIWander(minion, 0.3F));
        minion.tasks.addTask(5, new net.minecraft.entity.ai.EntityAIFollowOwner(minion, 0.3F, 5.0F, 2.0F));
        minion.tasks.addTask(9, new net.minecraft.entity.ai.EntityAIWatchClosest(minion, net.minecraft.entity.player.EntityPlayer.class, 8.0F));
        minion.tasks.addTask(9, new net.minecraft.entity.ai.EntityAILookIdle(minion));
    }

    public float getSpeed(EntityMinion minion)
    {
        return 0.5F;
    }

    public int getHealth(EntityMinion minion)
    {
        return 10;
    }

    public String getSound(EntityMinion minion)
    {
        return minion.getBodyPartsNames()[0].toString();
    }

    @Override
    public boolean shouldExecute()
    {
        return true;
    }

    public void log(Object msg)
    {
        System.out.println(this.getClass() + "	" + msg);
    }

    EntityMinion minion;
}
