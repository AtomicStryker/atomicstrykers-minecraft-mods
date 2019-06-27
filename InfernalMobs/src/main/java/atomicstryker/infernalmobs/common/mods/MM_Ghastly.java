package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
                tryAbility(mob, mob.world.getClosestPlayer(mob, 12f));
            }
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        if (mob.getDistance(target) > MIN_DISTANCE) {
            double diffX = target.posX - mob.posX;
            double diffY = target.getBoundingBox().minY + (double) (target.getHeight() / 2.0F) - (mob.posY + (double) (mob.getHeight() / 2.0F));
            double diffZ = target.posZ - mob.posZ;
            mob.renderYawOffset = mob.rotationYaw = -((float) Math.atan2(diffX, diffZ)) * 180.0F / (float) Math.PI;

            mob.world.playEvent(null, 1008, new BlockPos((int) mob.posX, (int) mob.posY, (int) mob.posZ), 0);
            FireballEntity entFB = new FireballEntity(mob.world, mob, diffX, diffY, diffZ);
            double spawnOffset = 2.0D;
            Vec3d mobLook = mob.getLook(1.0F);
            entFB.posX = mob.posX + mobLook.x * spawnOffset;
            entFB.posY = mob.posY + (double) (mob.getHeight() / 2.0F) + 0.5D;
            entFB.posZ = mob.posZ + mobLook.z * spawnOffset;
            mob.world.addEntity(entFB);
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
