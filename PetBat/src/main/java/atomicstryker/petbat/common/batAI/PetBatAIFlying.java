package atomicstryker.petbat.common.batAI;

import atomicstryker.petbat.common.EntityPetBat;
import atomicstryker.petbat.common.ItemPocketedPetBat;
import atomicstryker.petbat.common.PetBatMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;

import java.util.Random;

public class PetBatAIFlying extends MovementController {

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

    public PetBatAIFlying(EntityPetBat bat) {
        super(bat);
        petBat = bat;
        rand = bat.getRNG();
        nextOwnerCheckTime = System.currentTimeMillis();
        sittingSpotAbortTime = -1L;
    }

    @Override
    public void tick() {
        lookForOwnerEntity();

        if (petBat.getIsBatHanging()) {
            checkTakeOffConditions();
        } else {
            updateFlightTarget();
            performFlightMovement();
        }

        super.tick();
    }

    private void updateFlightTarget() {
        if (petBat.getOwnerEntity() != null) {
            if (petBat.getDistanceSq(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TAKEOFF
                    || (sittingSpotAbortTime > 0 && System.currentTimeMillis() > sittingSpotAbortTime)) {
                petBat.setHangingSpot(null);
            }

            if (petBat.getDistanceSq(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TELEPORT) {
                if (PetBatMod.instance().getPetBatInventoryTeleportEnabled()) {
                    ItemStack batstack = ItemPocketedPetBat.fromBatEntity(petBat);
                    if (batstack != ItemStack.EMPTY) {
                        ItemStack flute = PetBatMod.instance().removeFluteFromPlayer(petBat.getOwnerEntity(), petBat.getName().getString());
                        if (petBat.getOwnerEntity().inventory.addItemStackToInventory(batstack)) {
                            petBat.world.playSound(null, new BlockPos(petBat), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.AMBIENT, 1F, 1F);
                            petBat.setDeadWithoutRecall();
                        } else {
                            petBat.getOwnerEntity().inventory.addItemStackToInventory(flute);
                        }
                    }
                } else {
                    petBat.setPosition(petBat.getOwnerEntity().posX, petBat.getOwnerEntity().posY, petBat.getOwnerEntity().posZ);
                }
            }
        }

        if (petBat.getHangingSpot() == null) {
            sittingSpotAbortTime = -1L;

            // target invalid or no free block
            if (currentFlightTarget != null
                    && (!petBat.world.isAirBlock(currentFlightTarget) || currentFlightTarget.getY() < 1)) {
                currentFlightTarget = null;
            }

            // finding a new target, randomly
            if (currentFlightTarget == null || rand.nextInt(30) == 0
                    || currentFlightTarget.distanceSq(petBat.posX, petBat.posY, petBat.posZ, false) < 4.0F) {
                currentFlightTarget = getRandomFlightCoordinates();
            }
        } else {
            currentFlightTarget = petBat.getHangingSpot();

            if (sittingSpotAbortTime < 0) {
                sittingSpotAbortTime = System.currentTimeMillis() + SITTINGSPOT_REACHTIME;
            }

            if (currentFlightTarget.distanceSq(petBat.posX, petBat.posY, petBat.posZ, false) < 2F) {
                land();
            }
        }
    }

    /**
     * Attack targets >>> Food targets >>> currentFlightTarget
     */
    private void performFlightMovement() {
        double diffX, diffY, diffZ;
        if (petBat.getAttackTarget() == null || !petBat.getAttackTarget().isAlive()) {
            if (petBat.getFoodAttackTarget() != null && petBat.getFoodAttackTarget().isAlive()) {
                // Attack the food!
                diffX = petBat.getFoodAttackTarget().posX - petBat.posX;
                diffY = petBat.getFoodAttackTarget().posY - petBat.posY;
                diffZ = petBat.getFoodAttackTarget().posZ - petBat.posZ;
            } else if (currentFlightTarget != null) {
                // go for ChunkCoords flight target!
                diffX = (double) currentFlightTarget.getX() + 0.5D - petBat.posX;
                diffY = (double) currentFlightTarget.getY() + 0.1D - petBat.posY;
                diffZ = (double) currentFlightTarget.getZ() + 0.5D - petBat.posZ;
            } else {
                diffX = diffY = diffZ = 0D;
            }
        } else {
            // Attack the target!
            diffX = petBat.getAttackTarget().posX - petBat.posX;
            diffY = petBat.getAttackTarget().posY - petBat.posY + 1.5D;
            diffZ = petBat.getAttackTarget().posZ - petBat.posZ;
        }

        double newX = petBat.getMotion().x;
        double newY = petBat.getMotion().y;
        double newZ = petBat.getMotion().z;
        newX += (Math.signum(diffX) * 0.5D - newX) * 0.1D;
        newY += (Math.signum(diffY) * 0.7D - newY) * 0.1D;
        newZ += (Math.signum(diffZ) * 0.5D - newZ) * 0.1D;
        petBat.setMotion(newX, newY, newZ);
        float var7 = (float) (Math.atan2(newZ, newX) * 180.0D / Math.PI) - 90.0F;
        float var8 = MathHelper.wrapDegrees(var7 - petBat.rotationYaw);
        petBat.setMoveForward(0.5F);
        petBat.rotationYaw += var8;
    }

    private BlockPos getRandomFlightCoordinates() {
        if (petBat.getOwnerEntity() != null) {
            if (!petBat.getOwnerEntity().isAlive()) {
                petBat.setOwnerEntity(null);
                nextOwnerCheckTime = System.currentTimeMillis() + OWNER_FIND_INTERVAL;
            } else {
                petBat.updateOwnerCoords();
            }
        }

        int x = 0;
        int y = 0;
        int z = 0;
        Vec3d orig;
        Vec3d dest;
        for (int i = 0; i < 10; i++) {
            x = petBat.getLastOwnerX() + rand.nextInt(7) - rand.nextInt(7);
            y = petBat.getLastOwnerY() + rand.nextInt(6) - 2 + BAT_OWNER_FOLLOW_Y_OFFSET;
            z = petBat.getLastOwnerZ() + rand.nextInt(7) - rand.nextInt(7);

            orig = new Vec3d(petBat.posX, petBat.posY, petBat.posZ);
            dest = new Vec3d(x + 0.5D, y + 0.5D, z + 0.5D);
            RayTraceResult result = petBat.world.rayTraceBlocks(new RayTraceContext(orig, dest, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, petBat));
            if (result.getType() == RayTraceResult.Type.MISS) // no collision detected, path is free
            {
                break;
            }
        }

        return new BlockPos(x, y, z);
    }

    private void lookForOwnerEntity() {
        if (!petBat.getOwnerName().equals("") && System.currentTimeMillis() > nextOwnerCheckTime) {
            petBat.setOwnerEntity(petBat.world.getPlayerByUuid(petBat.getOwnerName()));
            nextOwnerCheckTime = System.currentTimeMillis() + OWNER_FIND_INTERVAL;
        }
    }

    private void checkTakeOffConditions() {
        // block it was hanging from is no more
        BlockPos bp = new BlockPos(MathHelper.floor(petBat.posX), (int) petBat.posY + 1, MathHelper.floor(petBat.posZ));
        BlockState ib = petBat.world.getBlockState(bp);
        if (!ib.isNormalCube(petBat.world, bp)) {
            takeOff();
        }

        if (!petBat.getIsBatStaying()) {
            if (petBat.getHasTarget()) {
                takeOff();
            }

            if (petBat.getOwnerEntity() != null && petBat.getOwnerEntity().isAlive()
                    && petBat.getDistanceSq(petBat.getOwnerEntity()) > OWNER_DISTANCE_TO_TAKEOFF) {
                takeOff();
            }

            // player scare
            PlayerEntity nearest = petBat.world.getClosestPlayer(petBat, 4.0D);
            if (nearest != null && nearest != petBat.getOwnerEntity()) {
                takeOff();
            }
        }
    }

    private void land() {
        sittingSpotAbortTime = -1L;
        petBat.setPosition(currentFlightTarget.getX() + 0.5D, currentFlightTarget.getY() + 0.5D, currentFlightTarget.getZ() + 0.5D);
        petBat.setIsBatHanging(true);
    }

    private void takeOff() {
        petBat.setIsBatHanging(false);
        petBat.setPosition(petBat.posX, petBat.posY - 1D, petBat.posZ);
        petBat.world.playSound(null, petBat.getPosition(), PetBatMod.soundTakeoff, SoundCategory.NEUTRAL, 0.05F, (petBat.getRNG().nextFloat() - petBat.getRNG().nextFloat()) * 0.2F + 1.0F);
    }
}
