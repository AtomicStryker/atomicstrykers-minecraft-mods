package atomicstryker.petbat.common.batAI;

import java.util.Random;

import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class PetBatAIFlying extends EntityAIBase
{
    private final int BAT_OWNER_FOLLOW_Y_OFFSET = 3;
    private final long OWNER_FIND_INTERVAL = 5000L;
    private final long SITTINGSPOT_REACHTIME = 3000L;
    private final double OWNER_DISTANCE_TO_TAKEOFF = 100D;
    private final double OWNER_DISTANCE_TO_TELEPORT = 400D;

    private final EntityPetBat petBat;
    private BlockPos currentFlightTarget;
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
    public boolean shouldContinueExecuting()
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
        lookForOwnerEntity();

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
                if (PetBatMod.instance().getPetBatInventoryTeleportEnabled())
                {
                    ItemStack batstack = ItemPocketedPetBat.fromBatEntity(petBat);
                    if (batstack != ItemStack.EMPTY)
                    {
                        ItemStack flute = PetBatMod.instance().removeFluteFromPlayer(petBat.getOwnerEntity(), petBat.getName());
                        if (petBat.getOwnerEntity().inventory.addItemStackToInventory(batstack))
                        {
                            petBat.world.playSound(null, new BlockPos(petBat), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.AMBIENT, 1F, 1F);
                            petBat.setDeadWithoutRecall();
                        }
                        else
                        {
                            petBat.getOwnerEntity().inventory.addItemStackToInventory(flute);
                        }
                    }
                }
                else
                {
                    petBat.setPosition(petBat.getOwnerEntity().posX, petBat.getOwnerEntity().posY, petBat.getOwnerEntity().posZ);
                }
            }
        }

        if (petBat.getHangingSpot() == null)
        {
            sittingSpotAbortTime = -1L;

            // target invalid or no free block
            if (currentFlightTarget != null
                    && (!petBat.world.isAirBlock(currentFlightTarget) || currentFlightTarget.getY() < 1))
            {
                currentFlightTarget = null;
            }

            // finding a new target, randomly
            if (currentFlightTarget == null || rand.nextInt(30) == 0
                    || currentFlightTarget.distanceSq(petBat.posX, petBat.posY, petBat.posZ) < 4.0F)
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

            if (currentFlightTarget.distanceSq(petBat.posX, petBat.posY, petBat.posZ) < 2F)
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
                diffX = (double) currentFlightTarget.getX() + 0.5D - petBat.posX;
                diffY = (double) currentFlightTarget.getY() + 0.1D - petBat.posY;
                diffZ = (double) currentFlightTarget.getZ() + 0.5D - petBat.posZ;
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
        float var8 = MathHelper.wrapDegrees(var7 - petBat.rotationYaw);
        petBat.setMoveForward(0.5F);
        petBat.rotationYaw += var8;
    }

    private BlockPos getRandomFlightCoordinates()
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

        int x = 0;
        int y = 0;
        int z = 0;
        Vec3d orig;
        Vec3d dest;
        RayTraceResult RayTraceResult;
        for (int i = 0; i < 10; i++)
        {
            x = petBat.getLastOwnerX() + rand.nextInt(7) - rand.nextInt(7);
            y = petBat.getLastOwnerY() + rand.nextInt(6) - 2 + BAT_OWNER_FOLLOW_Y_OFFSET;
            z = petBat.getLastOwnerZ() + rand.nextInt(7) - rand.nextInt(7);

            orig = new Vec3d(petBat.posX, petBat.posY, petBat.posZ);
            dest = new Vec3d(x + 0.5D, y + 0.5D, z + 0.5D);
            RayTraceResult = petBat.world.rayTraceBlocks(orig, dest, false, true, false);
            if (RayTraceResult == null) // no collision detected, path is
                                              // free
            {
                break;
            }
        }

        return new BlockPos(x, y, z);
    }

    private void lookForOwnerEntity()
    {
        if (!petBat.getOwnerName().equals("") && System.currentTimeMillis() > nextOwnerCheckTime)
        {
            petBat.setOwnerEntity(petBat.world.getPlayerEntityByName(petBat.getOwnerName()));
            nextOwnerCheckTime = System.currentTimeMillis() + OWNER_FIND_INTERVAL;
        }
    }

    private void checkTakeOffConditions()
    {
        // block it was hanging from is no more
        IBlockState ib = petBat.world.getBlockState(new BlockPos( MathHelper.floor(petBat.posX), (int) petBat.posY + 1, MathHelper.floor(petBat.posZ)));
        if (!ib.isNormalCube())
        {
            takeOff();
        }

        if (!petBat.getIsBatStaying())
        {
            if (petBat.getHasTarget())
            {
                takeOff();
            }

            if (petBat.getOwnerEntity() != null && petBat.getOwnerEntity().isEntityAlive()
                    && petBat.getDistanceSqToEntity(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TAKEOFF)
            {
                takeOff();
            }

            // player scare
            EntityPlayer nearest = petBat.world.getClosestPlayerToEntity(petBat, 4.0D);
            if (nearest != null && nearest != petBat.getOwnerEntity())
            {
                takeOff();
            }
        }
    }

    private void land()
    {
        sittingSpotAbortTime = -1L;
        petBat.setPosition(currentFlightTarget.getX() + 0.5D, currentFlightTarget.getY() + 0.5D, currentFlightTarget.getZ() + 0.5D);
        petBat.setIsBatHanging(true);
    }

    private void takeOff()
    {
        petBat.setIsBatHanging(false);
        petBat.setPosition(petBat.posX, petBat.posY - 1D, petBat.posZ);
        petBat.world.playSound(null, petBat.getPosition(), PetBatMod.soundTakeoff, SoundCategory.NEUTRAL, 0.05F, (petBat.getRNG().nextFloat() - petBat.getRNG().nextFloat()) * 0.2F + 1.0F);
    }
}
