package atomicstryker.minions.common.entity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;

public class MinionAIWander extends EntityAIBase
{
    private EntityMinion entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private float speed;
    private long lastMoveTime;
    private long currentTimeInterval;
    private final long moveTimeIntervals = 10000L;

    public MinionAIWander(EntityMinion minion, float movespeed)
    {
        this.entity = minion;
        this.speed = movespeed;
        this.setMutexBits(1);
        lastMoveTime = System.currentTimeMillis();
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if (entity.getCurrentTask() == null
        || entity.currentTarget == null
        || !entity.getNavigator().noPath()
        || entity.riddenByEntity != null)
        {
            return false;
        }
        else if (lastMoveTime+currentTimeInterval < System.currentTimeMillis())
        {
            float var5 = -99999.0F;

            for (int var6 = 0; var6 < 10; ++var6)
            {
                int var7 = MathHelper.floor_double(entity.currentTarget.posX + (double)entity.getRNG().nextInt(7) - 6.0D);
                int var8 = MathHelper.floor_double(entity.currentTarget.posY + (double)entity.getRNG().nextInt(4) - 3.0D);
                int var9 = MathHelper.floor_double(entity.currentTarget.posZ + (double)entity.getRNG().nextInt(7) - 6.0D);
                float var10 = entity.getBlockPathWeight(var7, var8, var9);
                if (var10 > var5)
                {
                    var5 = var10;
                    xPosition = var7;
                    yPosition = var8;
                    zPosition = var9;
                    lastMoveTime = System.currentTimeMillis();
                    currentTimeInterval = (long) (moveTimeIntervals * entity.getRNG().nextFloat()) + 2000L;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        return !this.entity.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.entity.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
    }
}
