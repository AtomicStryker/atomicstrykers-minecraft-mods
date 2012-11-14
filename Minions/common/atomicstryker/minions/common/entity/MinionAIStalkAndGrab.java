package atomicstryker.minions.common.entity;

import net.minecraft.src.EntityAIBase;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.World;

public class MinionAIStalkAndGrab extends EntityAIBase
{
    private EntityMinion theMinion;
    private World theWorld;
    private EntityLiving target;

    int grabDelay = 0;
    final int maxGrabDelay = 20;
    float moveSpeed;

    public MinionAIStalkAndGrab(EntityMinion minion, float par2)
    {
        this.theMinion = minion;
        this.theWorld = minion.worldObj;
        this.moveSpeed = par2;
        this.setMutexBits(3);
        target = null;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (theMinion.currentState == EnumMinionState.STALKING_TO_GRAB
        && theMinion.targetEntityToGrab != null)
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
    public boolean continueExecuting()
    {
        return target != null && target.isEntityAlive() && theMinion.currentState == EnumMinionState.STALKING_TO_GRAB && this.grabDelay < maxGrabDelay;
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.target = null;
        this.grabDelay = 0;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.theMinion.getLookHelper().setLookPositionWithEntity(this.target, 10.0F, (float)this.theMinion.getVerticalFaceSpeed());
        this.theMinion.getNavigator().tryMoveToEntityLiving(this.target, this.moveSpeed);
        ++this.grabDelay;

        if (this.grabDelay == maxGrabDelay)
        {
            target.mountEntity(theMinion);
            theMinion.currentState = theMinion.nextState;
            resetTask();
        }
    }
}
