package atomicstryker.minions.common.entity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import atomicstryker.minions.common.MinionsCore;

public class MinionAIFollowMaster extends EntityAIBase
{

    private final EntityMinion theMinion;
    private final float followSpeed;
    private final PathNavigate petPathfinder;
    private final float maxDist;
    private final float minDist;
    private boolean isAvoidingWater;

    private int updateTicker;
    private final double followRangeSq;

    public MinionAIFollowMaster(EntityMinion minion, float movespeed, float min, float max)
    {
        this.theMinion = minion;
        this.followSpeed = movespeed;
        this.petPathfinder = minion.getNavigator();
        this.minDist = min;
        this.maxDist = max;
        this.setMutexBits(3);
        followRangeSq = MinionsCore.instance.minionFollowRange * MinionsCore.instance.minionFollowRange;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        return theMinion.master != null && theMinion.getDistanceSqToEntity(theMinion.master) > (double) (this.minDist * this.minDist)
                && shouldFollowMaster();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
        return !this.petPathfinder.noPath() && this.theMinion.getDistanceSqToEntity(theMinion.master) > (double) (this.maxDist * this.maxDist)
                && shouldFollowMaster();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.updateTicker = 0;
        this.isAvoidingWater = this.theMinion.getNavigator().getAvoidsWater();
        this.theMinion.getNavigator().setAvoidsWater(false);
        theMinion.setWorking(false);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        this.petPathfinder.clearPathEntity();
        this.theMinion.getNavigator().setAvoidsWater(this.isAvoidingWater);
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
        this.theMinion.getLookHelper().setLookPositionWithEntity(theMinion.master, 10.0F, (float) this.theMinion.getVerticalFaceSpeed());

        if (shouldFollowMaster())
        {
            if (--this.updateTicker <= 0)
            {
                this.updateTicker = 30;

                if (!this.petPathfinder.tryMoveToEntityLiving(theMinion.master, this.followSpeed))
                {
                    if (this.theMinion.getDistanceSqToEntity(theMinion.master) >= followRangeSq)
                    {
                        int var1 = MathHelper.floor_double(theMinion.master.posX) - 2;
                        int var2 = MathHelper.floor_double(theMinion.master.posZ) - 2;
                        int var3 = MathHelper.floor_double(theMinion.master.boundingBox.minY);

                        for (int var4 = 0; var4 <= 4; ++var4)
                        {
                            for (int var5 = 0; var5 <= 4; ++var5)
                            {
                                if ((var4 < 1 || var5 < 1 || var4 > 3 || var5 > 3) && theMinion.worldObj.getBlock(var1 + var4, var3 - 1, var2 + var5).isBlockNormalCube()
                                        && !theMinion.worldObj.getBlock(var1 + var4, var3, var2 + var5).isBlockNormalCube() && !theMinion.worldObj.getBlock(var1 + var4, var3 + 1, var2 + var5).isBlockNormalCube())
                                {
                                    this.theMinion.setLocationAndAngles((double) ((float) (var1 + var4) + 0.5F), (double) var3, (double) ((float) (var2 + var5) + 0.5F), this.theMinion.rotationYaw,
                                            this.theMinion.rotationPitch);
                                    this.petPathfinder.clearPathEntity();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean shouldFollowMaster()
    {
        return theMinion.followingMaster || (theMinion.returningGoods && theMinion.returnChestOrInventory == null);
    }
}
