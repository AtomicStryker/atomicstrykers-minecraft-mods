package atomicstryker.minions.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class MinionAIStalkAndGrab extends EntityAIBase
{
    private EntityMinion theMinion;
    private EntityLivingBase target;

    int grabDelay = 0;
    final int maxGrabDelay = 20;
    float moveSpeed;

    public MinionAIStalkAndGrab(EntityMinion minion, float par2)
    {
        this.theMinion = minion;
        this.moveSpeed = par2;
        this.setMutexBits(3);
        target = null;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        if (theMinion.targetEntityToGrab != null)
        {
            target = theMinion.targetEntityToGrab;
            return true;
        }
        
        target = null;
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        return target != null && target.isEntityAlive() && this.grabDelay < maxGrabDelay;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        this.target = null;
        this.grabDelay = 0;
        theMinion.targetEntityToGrab = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
        this.theMinion.getLookHelper().setLookPositionWithEntity(this.target, 10.0F, (float)this.theMinion.getVerticalFaceSpeed());
        this.theMinion.getNavigator().tryMoveToEntityLiving(this.target, this.moveSpeed);
        ++this.grabDelay;

        if (this.grabDelay == maxGrabDelay)
        {
            target.startRiding(theMinion);
            resetTask();
        }
    }
}
