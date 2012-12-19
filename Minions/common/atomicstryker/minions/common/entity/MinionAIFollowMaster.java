package atomicstryker.minions.common.entity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class MinionAIFollowMaster extends EntityAIBase
{
    private EntityMinion theMinion;
    World theWorld;
    private float followSpeed;
    private PathNavigate petPathfinder;
    private int updateTicker;
    float maxDist;
    float minDist;
    private boolean isAvoidingWater;

    public MinionAIFollowMaster(EntityMinion minion, float movespeed, float min, float max)
    {
        this.theMinion = minion;
        this.theWorld = minion.worldObj;
        this.followSpeed = movespeed;
        this.petPathfinder = minion.getNavigator();
        this.minDist = min;
        this.maxDist = max;
        this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (theMinion.master != null
        && theMinion.getDistanceSqToEntity(theMinion.master) > (double)(this.minDist * this.minDist)
        && (theMinion.currentState == EnumMinionState.FOLLOWING_PLAYER
        || (theMinion.currentState == EnumMinionState.RETURNING_GOODS && theMinion.returnChestOrInventory == null)))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.petPathfinder.noPath()
                && this.theMinion.getDistanceSqToEntity(theMinion.master) > (double)(this.maxDist * this.maxDist)
                && (theMinion.currentState == EnumMinionState.FOLLOWING_PLAYER
                || (theMinion.currentState == EnumMinionState.RETURNING_GOODS && theMinion.returnChestOrInventory == null));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.updateTicker = 0;
        this.isAvoidingWater = this.theMinion.getNavigator().getAvoidsWater();
        this.theMinion.getNavigator().setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.petPathfinder.clearPathEntity();
        this.theMinion.getNavigator().setAvoidsWater(this.isAvoidingWater);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.theMinion.getLookHelper().setLookPositionWithEntity(theMinion.master, 10.0F, (float)this.theMinion.getVerticalFaceSpeed());

        if (theMinion.currentState == EnumMinionState.FOLLOWING_PLAYER
        || (theMinion.currentState == EnumMinionState.RETURNING_GOODS && theMinion.returnChestOrInventory == null))
        {
            if (--this.updateTicker <= 0)
            {
                this.updateTicker = 10;

                if (!this.petPathfinder.tryMoveToEntityLiving(theMinion.master, this.followSpeed))
                {
                    if (this.theMinion.getDistanceSqToEntity(theMinion.master) >= 144.0D)
                    {
                        int var1 = MathHelper.floor_double(theMinion.master.posX) - 2;
                        int var2 = MathHelper.floor_double(theMinion.master.posZ) - 2;
                        int var3 = MathHelper.floor_double(theMinion.master.boundingBox.minY);

                        for (int var4 = 0; var4 <= 4; ++var4)
                        {
                            for (int var5 = 0; var5 <= 4; ++var5)
                            {
                                if ((var4 < 1 || var5 < 1 || var4 > 3 || var5 > 3) && this.theWorld.isBlockNormalCube(var1 + var4, var3 - 1, var2 + var5) && !this.theWorld.isBlockNormalCube(var1 + var4, var3, var2 + var5) && !this.theWorld.isBlockNormalCube(var1 + var4, var3 + 1, var2 + var5))
                                {
                                    this.theMinion.setLocationAndAngles((double)((float)(var1 + var4) + 0.5F), (double)var3, (double)((float)(var2 + var5) + 0.5F), this.theMinion.rotationYaw, this.theMinion.rotationPitch);
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
}
