package atomicstryker.stalkercreepers.mixin;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.CreeperSwellGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(CreeperSwellGoal.class)
public class CreeperSwellGoalMixin {

    // use mixin to access field in the original class
    @Shadow
    private CreeperEntity swellingCreeper;

    // hooks public boolean shouldExecute()
    @Inject(at = @At("HEAD"), method = "shouldExecute()Z", cancellable = true)
    private void shouldExecute(CallbackInfoReturnable<Boolean> callback) {
        LivingEntity livingentity = swellingCreeper.getAttackTarget();
        if (isSeenByTarget(swellingCreeper)) {
            callback.setReturnValue(swellingCreeper.getCreeperState() > 0 || livingentity != null && swellingCreeper.getDistanceSq(livingentity) < 9.0D);
        } else {
            callback.setReturnValue(false);
        }
    }

    private boolean isSeenByTarget(CreeperEntity stalker) {
        LivingEntity seer = stalker.getAttackTarget();

        if (seer == null) {
            return true;
        }

        Vector3d visionVec = seer.getLook(1.0F).normalize();
        Vector3d targetVec = new Vector3d(stalker.getPosX() - seer.getPosX(),
                stalker.getBoundingBox().minY + (double) (stalker.getHeight() / 2.0F) - (seer.getPosY() + (double) seer.getEyeHeight()),
                stalker.getPosZ() - seer.getPosZ());

        targetVec = targetVec.normalize();
        double dotProduct = visionVec.dotProduct(targetVec);

        boolean inFOV = dotProduct > 0.1 && seer.canEntityBeSeen(stalker);

        //System.out.println("dotProduct result in isSeenByTarget: "+dotProduct+"; inFOV: "+inFOV);

        return inFOV;
    }
}
