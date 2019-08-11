package atomicstryker.petbat.common.batAI;

import atomicstryker.petbat.common.EntityPetBat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

public class PetBatAIOwnerAttacks extends TargetGoal {
    private EntityPetBat batEnt;
    private LivingEntity theTarget;

    public PetBatAIOwnerAttacks(EntityPetBat bat) {
        super(bat, false);
        batEnt = bat;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean shouldExecute() {
        if (batEnt.getOwnerEntity() != null) {
            theTarget = batEnt.getOwnerEntity().getLastAttackedEntity();
            if (theTarget instanceof EntityPetBat) {
                EntityPetBat otherBat = (EntityPetBat) theTarget;
                if (otherBat.getOwnerUUID().equals(batEnt.getOwnerUUID())) {
                    theTarget = null;
                    return false;
                }
            }
            if (theTarget == null) {
                return false;
            }
            return this.shouldAttackEntity(theTarget, batEnt.getOwnerEntity());
        }

        return false;
    }

    private boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof WolfEntity) {
                WolfEntity wolfentity = (WolfEntity) target;
                if (wolfentity.isTamed() && wolfentity.getOwner() == owner) {
                    return false;
                }
            }

            if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity) owner).canAttackPlayer((PlayerEntity) target)) {
                return false;
            } else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity) target).isTame()) {
                return false;
            } else {
                return !(target instanceof CatEntity) || !((CatEntity) target).isTamed();
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return (theTarget != null && theTarget.isAlive());
    }

    @Override
    public void resetTask() {
        theTarget = null;
    }

    @Override
    public void startExecuting() {
        batEnt.setAttackTarget(theTarget);
        super.startExecuting();
    }
}
