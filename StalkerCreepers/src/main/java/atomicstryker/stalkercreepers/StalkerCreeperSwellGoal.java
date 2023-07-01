package atomicstryker.stalkercreepers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

public class StalkerCreeperSwellGoal extends SwellGoal {

    private final Creeper creeper;

    public StalkerCreeperSwellGoal(Creeper creeper) {
        super(creeper);
        this.creeper = creeper;
    }

    @Override
    public boolean canUse() {
        LivingEntity victim = creeper.getTarget();
        return creeper.getSwellDir() > 0 || victim != null &&
                (creeper.distanceToSqr(victim) < 9.0D && isInVictimsView(creeper, victim));
    }

    private boolean isInVictimsView(Creeper stalker, LivingEntity victim) {

        // this code is a variation of EnderMan.isLookingAtMe, if it has to be remade
        Vec3 visionVec = victim.getViewVector(1.0F).normalize();
        Vec3 targetVec = new Vec3(stalker.getX() - victim.getX(),
                stalker.getBoundingBox().minY + (double) (stalker.getBbHeight() / 2.0F) - (victim.getY() + (double) victim.getEyeHeight()),
                stalker.getZ() - victim.getZ());

        targetVec = targetVec.normalize();
        double dotProduct = visionVec.dot(targetVec);

        boolean inFOV = dotProduct > 0.1 && victim.hasLineOfSight(stalker);
        // System.out.println("dotProduct result in isSeenByTarget: " + dotProduct + "; inFOV: " + inFOV);
        return inFOV;
    }
}
