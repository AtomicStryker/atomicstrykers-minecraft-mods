package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MM_Ender extends MobModifier {

    private final static long coolDown = 15000L;
    private static String[] suffix = {"theEnderborn", "theTrickster"};
    private static String[] prefix = {"enderborn", "tricky"};
    private long nextAbilityUse = 0L;

    public MM_Ender() {
        super();
    }

    public MM_Ender(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Ender";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse && source.getTrueSource() != null && source.getTrueSource() != mob && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getTrueSource())
                && teleportToEntity(mob, source.getTrueSource())) {
            nextAbilityUse = time + coolDown;
            source.getTrueSource().attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));

            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(LivingEntity mob, Entity par1Entity) {
        Vec3d vector = new Vec3d(mob.func_226277_ct_() - par1Entity.func_226277_ct_(), mob.getBoundingBox().minY + (double) (mob.getHeight() / 2.0F) - par1Entity.func_226278_cu_() + (double) par1Entity.getEyeHeight(),
                mob.func_226281_cx_() - par1Entity.func_226281_cx_());
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.func_226277_ct_() + (mob.world.rand.nextDouble() - 0.5D) * 8.0D - vector.x * telDist;
        double destY = mob.func_226278_cu_() + (double) (mob.world.rand.nextInt(16) - 8) - vector.y * telDist;
        double destZ = mob.func_226281_cx_() + (mob.world.rand.nextDouble() - 0.5D) * 8.0D - vector.z * telDist;
        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(LivingEntity mob, double destX, double destY, double destZ) {
        double oldX = mob.func_226277_ct_();
        double oldY = mob.func_226278_cu_();
        double oldZ = mob.func_226281_cx_();
        boolean success = false;
        // setPos
        mob.func_226288_n_(destX, destY, destZ);
        int x = MathHelper.floor(mob.func_226277_ct_());
        int y = MathHelper.floor(mob.func_226278_cu_());
        int z = MathHelper.floor(mob.func_226281_cx_());

        boolean hitGround = false;
        while (!hitGround && y < 96 && y > 0) {
            BlockState bs = mob.world.getBlockState(new BlockPos(x, y - 1, z));
            if (bs.getMaterial().blocksMovement()) {
                hitGround = true;
            } else {
                // setPos
                mob.func_226288_n_(destX, --destY, destZ);
                --y;
            }
        }

        if (hitGround) {
            mob.setPosition(mob.func_226277_ct_(), mob.func_226278_cu_(), mob.func_226281_cx_());

            if (mob.attemptTeleport(destX, destY, destZ, true)) {
                success = true;
            }
        } else {
            return false;
        }

        if (!success) {
            mob.setPosition(oldX, oldY, oldZ);
            return false;
        } else {
            short range = 128;
            for (int i = 0; i < range; ++i) {
                double var19 = (double) i / ((double) range - 1.0D);
                float var21 = (mob.world.rand.nextFloat() - 0.5F) * 0.2F;
                float var22 = (mob.world.rand.nextFloat() - 0.5F) * 0.2F;
                float var23 = (mob.world.rand.nextFloat() - 0.5F) * 0.2F;
                double var24 = oldX + (mob.func_226277_ct_() - oldX) * var19 + (mob.world.rand.nextDouble() - 0.5D) * (double) mob.getWidth() * 2.0D;
                double var26 = oldY + (mob.func_226278_cu_() - oldY) * var19 + mob.world.rand.nextDouble() * (double) mob.getHeight();
                double var28 = oldZ + (mob.func_226281_cx_() - oldZ) * var19 + (mob.world.rand.nextDouble() - 0.5D) * (double) mob.getWidth() * 2.0D;
                mob.world.addParticle(ParticleTypes.PORTAL, var24, var26, var28, (double) var21, (double) var22, (double) var23);
            }

            mob.world.playSound(null, new BlockPos(oldX, oldY, oldZ), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(),
                    mob.getRNG().nextFloat() * 0.7F + 0.3F);
            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
        }
        return true;
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
