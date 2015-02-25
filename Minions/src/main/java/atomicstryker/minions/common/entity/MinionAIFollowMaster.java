package atomicstryker.minions.common.entity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import atomicstryker.minions.common.MinionsCore;

public class MinionAIFollowMaster extends EntityAIBase
{

    private final EntityMinion theMinion;
    private final float followSpeed;
    private final PathNavigate petPathfinder;
    private final float maxDist;
    private final float minDist;

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
        theMinion.setWorking(false);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        this.petPathfinder.clearPathEntity();
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
        theMinion.getLookHelper().setLookPositionWithEntity(theMinion.master, 10.0F, (float) theMinion.getVerticalFaceSpeed());

        if (shouldFollowMaster())
        {
            if (--updateTicker <= 0)
            {
                updateTicker = 30;

                if (!petPathfinder.tryMoveToEntityLiving(theMinion.master, followSpeed))
                {
                    if (theMinion.getDistanceSqToEntity(theMinion.master) >= followRangeSq)
                    {
                        int x = MathHelper.floor_double(theMinion.master.posX) - 2;
                        int z = MathHelper.floor_double(theMinion.master.posZ) - 2;
                        int y = MathHelper.floor_double(theMinion.master.getEntityBoundingBox().minY);

                        for (int xIter = 0; xIter <= 4; ++xIter)
                        {
                            for (int zIter = 0; zIter <= 4; ++zIter)
                            {
                                if ((xIter < 1 || zIter < 1 || xIter > 3 || zIter > 3)
                                        && theMinion.worldObj.getBlockState(new BlockPos(x + xIter, y - 1, z + zIter)).getBlock().isNormalCube()
                                        && !theMinion.worldObj.getBlockState(new BlockPos(x + xIter, y, z + zIter)).getBlock().isNormalCube()
                                        && !theMinion.worldObj.getBlockState(new BlockPos(x + xIter, y + 1, z + zIter)).getBlock().isNormalCube())
                                {
                                    theMinion.setLocationAndAngles((double) (x + xIter) + 0.5D, (double) y, (double) (z + zIter) + 0.5D,
                                            theMinion.rotationYaw, theMinion.rotationPitch);
                                    petPathfinder.clearPathEntity();
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
