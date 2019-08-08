package atomicstryker.petbat.common.batAI;

import atomicstryker.petbat.common.EntityPetBat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PetBatAIAttack extends Goal {

    private final EntityPetBat petBat;
    private Entity entityTarget;
    private int attackTick;

    public PetBatAIAttack(LivingEntity bat) {
        petBat = (EntityPetBat) bat;
        attackTick = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (petBat.getAttackTarget() != null
                && petBat.getAttackTarget().isAlive()) {
            entityTarget = petBat.getAttackTarget();
            return true;
        } else if (petBat.getFoodAttackTarget() != null
                && petBat.getFoodAttackTarget().isAlive()) {
            entityTarget = petBat.getFoodAttackTarget();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (entityTarget instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) entityTarget;
            if (entityTarget.getName().equals(petBat.getOwnerName()) && p.getHealth() < p.getMaxHealth() / 2) {
                petBat.recallToOwner();
                return false;
            }
        }

        return entityTarget != null && entityTarget.isAlive() || super.shouldContinueExecuting();

    }

    @Override
    public void startExecuting() {
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        entityTarget = null;
        attackTick = 0;
        super.resetTask();
    }

    @Override
    public void tick() {
        petBat.getLookController().setLookPositionWithEntity(entityTarget, 30.0F, 30.0F);

        attackTick = Math.max(attackTick - 1, 0);

        double maxReach = petBat.getWidth() * petBat.getWidth() * 5.0D;
        if (petBat.getDistanceSq(entityTarget.posX, entityTarget.getBoundingBox().maxY, entityTarget.posZ) <= maxReach || petBat.getBoundingBox().intersects(entityTarget.getBoundingBox())) {
            if (entityTarget instanceof ItemEntity) {
                if (attackTick == 0) {
                    attackTick = 40;
                } else if (attackTick == 1) {
                    entityTarget.remove();
                    displayEatingEffects(((ItemEntity) entityTarget).getItem(), 16);
                    petBat.world.playSound(null, new BlockPos(petBat), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.AMBIENT, 0.5F, petBat.getRNG().nextFloat() * 0.1F + 0.9F);
                    petBat.heal(18);
                    petBat.setFoodAttackTarget(null);
                } else if (attackTick % 3 == 0) {
                    displayEatingEffects(((ItemEntity) entityTarget).getItem(), 5);
                }
            } else {
                if (attackTick == 0) {
                    attackTick = 20;
                    petBat.attackEntityAsMob(entityTarget);

                    double xKnock = entityTarget.posX - petBat.posX;
                    double zKnock = entityTarget.posZ - petBat.posZ;
                    for (; xKnock * xKnock + zKnock * zKnock < 1.0E-4D; zKnock = (Math.random() - Math.random()) * 0.01D) {
                        xKnock = (Math.random() - Math.random()) * 0.01D;
                    }
                    petBat.knockBack(entityTarget, 0, xKnock, zKnock);
                }
            }
        }
    }

    private void displayEatingEffects(ItemStack item, int power) {
        for (int var3 = 0; var3 < power; ++var3) {
            Vec3d var4 = new Vec3d(((double) petBat.getRNG().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            var4.rotatePitch(-petBat.rotationPitch * (float) Math.PI / 180.0F);
            var4.rotateYaw(-petBat.rotationYaw * (float) Math.PI / 180.0F);
            Vec3d var5 = new Vec3d(((double) petBat.getRNG().nextFloat() - 0.5D) * 0.3D, (double) (-petBat.getRNG().nextFloat()) * 0.6D - 0.3D, 0.6D);
            var5.rotatePitch(-petBat.rotationPitch * (float) Math.PI / 180.0F);
            var5.rotateYaw(-petBat.rotationYaw * (float) Math.PI / 180.0F);
            var5 = var5.add(petBat.posX, petBat.posY + (double) petBat.getEyeHeight(), petBat.posZ);
            petBat.world.addParticle(ParticleTypes.HEART, var5.x, var5.y, var5.z, var4.x, var4.y + 0.05D, var4.z);
        }

        petBat.world.playSound(null, new BlockPos(petBat), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.AMBIENT, 0.5F + 0.5F * (float) petBat.getRNG().nextInt(2),
                (petBat.getRNG().nextFloat() - petBat.getRNG().nextFloat()) * 0.2F + 1.0F);
    }

}
