package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

public class MM_Ghastly extends MobModifier {

    private final static long coolDown = 6000L;
    private final static float MIN_DISTANCE = 3F;
    private static String[] suffix = {"OMFGFIREBALLS", "theBomber", "ofBallsofFire"};
    private static String[] prefix = {"bombing", "fireballsy"};
    private long nextAbilityUse = 0L;

    public MM_Ghastly() {
        super();
    }

    public MM_Ghastly(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Ghastly";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                tryAbility(mob, mob.level.getNearestPlayer(mob, 12f));
            }
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !canMobSeeTarget(mob, target)) {
            return;
        }

        if (mob.distanceTo(target) > MIN_DISTANCE) {
            double diffX = target.getX() - mob.getX();
            double diffY = target.getBoundingBox().minY + (double) (target.getBbHeight() / 2.0F) - (mob.getY() + (double) (mob.getBbHeight() / 2.0F));
            double diffZ = target.getZ() - mob.getZ();
            mob.setYRot(-((float) Math.atan2(diffX, diffZ)) * 180.0F / (float) Math.PI);
            mob.setYBodyRot(mob.getYRot());

            mob.level.levelEvent(null, 1008, new BlockPos((int) mob.getX(), (int) mob.getY(), (int) mob.getZ()), 0);
            // the last int parameter is explosionpower, apparently 1 is Ghast default
            LargeFireball entFB = new LargeFireball(mob.level, mob, diffX, diffY, diffZ, 1);
            double spawnOffset = 2.0D;
            Vec3 mobLook = mob.getViewVector(1.0F);
            double newX = mob.getX() + mobLook.x * spawnOffset;
            double newY = mob.getY() + (double) (mob.getBbHeight() / 2.0F) + 0.5D;
            double newZ = mob.getZ() + mobLook.z * spawnOffset;
            mob.setPos(newX, newY, newZ);

            mob.level.addFreshEntity(entFB);
        }
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

}
