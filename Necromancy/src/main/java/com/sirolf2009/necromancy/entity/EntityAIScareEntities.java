package com.sirolf2009.necromancy.entity;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.IMob;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

public class EntityAIScareEntities extends EntityAIBase
{

    private EntityLiving target;
    private EntityTeddy entity;
    private float seekingRange;
    private float scaringRange;
    private final Class<?> targetSpecs;
    private ArrayList<Entity> entities;
    private final ArrayList<Entity> targets;
    private PathNavigate pathFinderSelf;
    private PathNavigate pathFinderTarget;
    private PathEntity pathEntity;

    public EntityAIScareEntities(EntityLiving entity, float seekingRange, float scaringRange, float speed, Class<?> class1)
    {
        targets = new ArrayList<Entity>();
        this.entity = (EntityTeddy) entity;
        this.seekingRange = seekingRange;
        this.scaringRange = scaringRange;
        targetSpecs = class1;
        pathFinderSelf = entity.getNavigator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean shouldExecute()
    {
        if ((entities =
                (ArrayList<Entity>) entity.worldObj.getEntitiesWithinAABB(targetSpecs,
                        entity.boundingBox.expand(seekingRange, seekingRange, seekingRange))) != null)
        {
            Iterator<Entity> it = entities.iterator();
            do
            {
                if (!it.hasNext())
                {
                    break;
                }
                Entity temp = it.next();
                if (temp != null && temp instanceof IMob)
                {
                    targets.add(temp);
                }
            }
            while (true);
            if (targets != null && entity.entityState == EntityTeddy.EntityState.DEFENDING)
                return true;
        }
        return false;
    }

    @Override
    public void updateTask()
    {
        if ((target = getClosestEntity()) != null)
        {
            pathFinderTarget = target.getNavigator();
            pathFinderSelf.tryMoveToEntityLiving(target, entity.getAIMoveSpeed());
            if (entity.getDistanceToEntity(target) < scaringRange)
            {
                Vec3 var2 =
                        RandomPositionGenerator.findRandomTargetBlockAwayFrom((EntityCreature) target, 16, 7, Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ));
                if (var2 != null && entity.getDistanceSq(var2.xCoord, var2.yCoord, var2.zCoord) >= entity.getDistanceSqToEntity(target))
                {
                    pathEntity = pathFinderTarget.getPathToXYZ(var2.xCoord, var2.yCoord, var2.zCoord);
                    if (pathEntity != null)
                    {
                        pathFinderTarget.setPath(pathEntity, 0.4F);
                    }
                }
            }
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return target != null && entity.entityState == EntityTeddy.EntityState.DEFENDING;
    }

    private EntityLiving getClosestEntity()
    {
        Iterator<Entity> iterator = targets.iterator();
        EntityLiving tempEntityTarget = null;
        double tempRange = seekingRange + 1.0F;
        do
        {
            if (!iterator.hasNext())
            {
                break;
            }
            EntityLiving tempEntity = (EntityLiving) iterator.next();
            if (entity.getDistanceToEntity(tempEntity) < tempRange)
            {
                tempRange = entity.getDistanceToEntity(tempEntity);
                tempEntityTarget = tempEntity;
            }
        }
        while (true);
        return tempEntityTarget;
    }
}
