package atomicstryker.minions.common.entity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.Vec3;

public class MinionAIWander extends EntityAIBase
{
    private EntityMinion entity;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private float speed;
    private long nextMoveTime;
    private final long moveTimeIntervals = 10000L;

    public MinionAIWander(EntityMinion minion, float movespeed)
    {
        this.entity = minion;
        this.speed = movespeed;
        nextMoveTime = System.currentTimeMillis();
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if (entity.getCurrentTask() != null
        || entity.riddenByEntity != null)
        {
            return false;
        }
        else if (nextMoveTime < System.currentTimeMillis())
        {
            Vec3 vec3 = RandomPositionGenerator.findRandomTarget(this.entity, 6, 4);

            if (vec3 == null)
            {
                return false;
            }
            else
            {
                this.xPosition = vec3.xCoord;
                this.yPosition = vec3.yCoord;
                this.zPosition = vec3.zCoord;
                nextMoveTime = System.currentTimeMillis() + (long) (moveTimeIntervals * entity.getRNG().nextFloat());
                return true;
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
        return entity.getCurrentTask() == null && !this.entity.getNavigator().noPath();
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
