package atomicstryker.petbat.common.batAI;

import java.util.Random;

import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.ItemPocketedPetBat;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.EntityAIBase;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;

public class PetBatAIFlying extends EntityAIBase
{
    private final int BAT_OWNER_FOLLOW_Y_OFFSET = 3;
    private final long OWNER_FIND_INTERVAL = 5000L;
    private final long SITTINGSPOT_REACHTIME = 3000L;
    private final double OWNER_DISTANCE_TO_TAKEOFF = 100D;
    private final double OWNER_DISTANCE_TO_TELEPORT = 400D;

    private final EntityPetBat petBat;
    private ChunkCoordinates currentFlightTarget;
    private Random rand;
    private long nextOwnerCheckTime;
    private long sittingSpotAbortTime;

    public PetBatAIFlying(EntityPetBat bat)
    {
        petBat = bat;
        rand = bat.getRNG();
        nextOwnerCheckTime = System.currentTimeMillis();
        sittingSpotAbortTime = -1L;
    }

    @Override
    public boolean shouldExecute()
    {
        return true;
    }

    @Override
    public boolean continueExecuting()
    {
        return true;
    }

    @Override
    public void startExecuting()
    {
        super.startExecuting();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
    }

    @Override
    public void updateTask()
    {
        // map reload or owner disconnected - search owner entity every couple seconds
        if (petBat.getOwnerEntity() == null)
        {
            lookForOwnerEntity();
        }

        if (petBat.getIsBatHanging())
        {
            checkTakeOffConditions();
        }
        else
        {
            updateFlightTarget();
            performFlightMovement();
        }

        super.updateTask();
    }

    private void updateFlightTarget()
    {
        if (petBat.getOwnerEntity() != null)
        {
            if (petBat.getDistanceSqToEntity(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TAKEOFF
            || (sittingSpotAbortTime > 0 && System.currentTimeMillis() > sittingSpotAbortTime))
            {
                petBat.setHangingSpot(null);
            }

            if (petBat.getDistanceSqToEntity(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TELEPORT)
            {
                ItemStack batstack = ItemPocketedPetBat.fromBatEntity(petBat);
                if (batstack != null)
                {
                    if (petBat.getOwnerEntity().inventory.addItemStackToInventory(batstack))
                    {
                        petBat.worldObj.playSoundAtEntity(petBat.getOwnerEntity(), "mob.slime.big", 1F, 1F);
                        petBat.setDeadWithoutRecall();
                    }
                }
            }
        }
        
        if (petBat.getHangingSpot() == null)
        {
            sittingSpotAbortTime = -1L;
            
            // target invalid or no free block
            if (currentFlightTarget != null && (!petBat.worldObj.isAirBlock(currentFlightTarget.posX, currentFlightTarget.posY, currentFlightTarget.posZ) || currentFlightTarget.posY < 1))
            {
                currentFlightTarget = null;
            }

            // finding a new target, randomly
            if (currentFlightTarget == null || rand.nextInt(30) == 0 || currentFlightTarget.getDistanceSquared((int) petBat.posX, (int) petBat.posY, (int) petBat.posZ) < 4.0F)
            {
                currentFlightTarget = getRandomFlightCoordinates();
            }
        }
        else
        {
			currentFlightTarget = petBat.getHangingSpot();
		
            if (sittingSpotAbortTime < 0)
            {
                sittingSpotAbortTime = System.currentTimeMillis() + SITTINGSPOT_REACHTIME;
            }
            
            if (currentFlightTarget.getDistanceSquared((int) petBat.posX, (int) petBat.posY, (int) petBat.posZ) < 2F)
            {
                land();
            }
        }
    }
    
    /**
     * Attack targets >>> Food targets >>> currentFlightTarget
     */
    private void performFlightMovement()
    {
        double diffX, diffY, diffZ;
        if (petBat.getAttackTarget() == null || !petBat.getAttackTarget().isEntityAlive())
        {
            if (petBat.getFoodAttackTarget() != null && petBat.getFoodAttackTarget().isEntityAlive())
            {
                // Attack the food!
                diffX = petBat.getFoodAttackTarget().posX - petBat.posX;
                diffY = petBat.getFoodAttackTarget().posY - petBat.posY;
                diffZ = petBat.getFoodAttackTarget().posZ - petBat.posZ;
            }
            else if (currentFlightTarget != null)
            {
                // go for ChunkCoords flight target!
                diffX = (double) currentFlightTarget.posX + 0.5D - petBat.posX;
                diffY = (double) currentFlightTarget.posY + 0.1D - petBat.posY;
                diffZ = (double) currentFlightTarget.posZ + 0.5D - petBat.posZ;
            }
            else
            {
                diffX = diffY = diffZ = 0D;
            }
        }
        else
        {
            // Attack the target!
            diffX = petBat.getAttackTarget().posX - petBat.posX;
            diffY = petBat.getAttackTarget().posY - petBat.posY + 1.5D;
            diffZ = petBat.getAttackTarget().posZ - petBat.posZ;
        }

        petBat.motionX += (Math.signum(diffX) * 0.5D - petBat.motionX) * 0.1D;
        petBat.motionY += (Math.signum(diffY) * 0.7D - petBat.motionY) * 0.1D;
        petBat.motionZ += (Math.signum(diffZ) * 0.5D - petBat.motionZ) * 0.1D;
        float var7 = (float) (Math.atan2(petBat.motionZ, petBat.motionX) * 180.0D / Math.PI) - 90.0F;
        float var8 = MathHelper.wrapAngleTo180_float(var7 - petBat.rotationYaw);
        petBat.setMoveForward(0.5F);
        petBat.rotationYaw += var8;
    }

    private ChunkCoordinates getRandomFlightCoordinates()
    {
        if (petBat.getOwnerEntity() != null)
        {
            if (!petBat.getOwnerEntity().isEntityAlive())
            {
                petBat.setOwnerEntity(null);
                nextOwnerCheckTime = System.currentTimeMillis() + OWNER_FIND_INTERVAL;
            }
            else
            {
                petBat.updateOwnerCoords();
            }
        }

        return new ChunkCoordinates(
                petBat.getLastOwnerX() + rand.nextInt(7) - rand.nextInt(7),
                petBat.getLastOwnerY() + rand.nextInt(6) - 2 + BAT_OWNER_FOLLOW_Y_OFFSET,
                petBat.getLastOwnerZ() + rand.nextInt(7) - rand.nextInt(7)
                );
    }
    
    private void lookForOwnerEntity()
    {
        if (!petBat.getOwnerName().equals("") && System.currentTimeMillis() > nextOwnerCheckTime)
        {
            petBat.setOwnerEntity(petBat.worldObj.getPlayerEntityByName(petBat.getOwnerName()));
            nextOwnerCheckTime = System.currentTimeMillis() + OWNER_FIND_INTERVAL;
        }
    }
    
    private void checkTakeOffConditions()
    {
        if (petBat.getHasTarget())
        {
            takeOff();
        }
        
        if (petBat.getOwnerEntity() != null && petBat.getOwnerEntity().isEntityAlive() &&  petBat.getDistanceSqToEntity(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TAKEOFF)
        {
            takeOff();
        }
        
        // block it was hanging from is no more
        if (!petBat.worldObj.isBlockNormalCube(MathHelper.floor_double(petBat.posX), (int) petBat.posY + 1, MathHelper.floor_double(petBat.posZ)))
        {
            takeOff();
        }

        // player scare
        EntityPlayer nearest = petBat.worldObj.getClosestPlayerToEntity(petBat, 4.0D);
        if (nearest != null && nearest != petBat.getOwnerEntity())
        {
            takeOff();
        }
    }
    
    private void land()
    {
        sittingSpotAbortTime = -1L;
        petBat.setPosition(currentFlightTarget.posX+0.5D, currentFlightTarget.posY+0.5D, currentFlightTarget.posZ+0.5D);
        petBat.setIsBatHanging(true);
    }
    
    private void takeOff()
    {
        petBat.setIsBatHanging(false);
        petBat.setPosition(petBat.posX, petBat.posY-1D, petBat.posZ);
        petBat.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1015, (int) petBat.posX, (int) petBat.posY, (int) petBat.posZ, 0);
    }
}
