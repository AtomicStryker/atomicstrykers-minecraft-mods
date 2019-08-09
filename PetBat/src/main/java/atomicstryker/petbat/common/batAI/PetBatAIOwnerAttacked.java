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

public class PetBatAIOwnerAttacked extends TargetGoal {
    private EntityPetBat batEnt;
    private LivingEntity theOwnerAttacker;

    public PetBatAIOwnerAttacked(EntityPetBat bat) {
        super(bat, false);
        batEnt = bat;
        this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean shouldExecute() {
        if (batEnt.getOwnerEntity() != null) {
            theOwnerAttacker = batEnt.getOwnerEntity().getRevengeTarget();
            return theOwnerAttacker != batEnt.getOwnerEntity() && shouldAttackEntity(theOwnerAttacker, batEnt.getOwnerEntity());
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
    public void startExecuting() {
        goalOwner.setAttackTarget(theOwnerAttacker);
        super.startExecuting();
    }
}
