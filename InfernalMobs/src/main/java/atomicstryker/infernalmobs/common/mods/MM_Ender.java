package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;

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
        if (time > nextAbilityUse && source.getEntity() != null && source.getEntity() != mob && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getEntity())
                && teleportToEntity(mob, source.getEntity())) {
            nextAbilityUse = time + coolDown;
            source.getEntity().hurt(DamageSource.mobAttack(mob), InfernalMobsCore.instance().getLimitedDamage(damage));

            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(LivingEntity mob, Entity par1Entity) {
        Vec3 vector = new Vec3(mob.getX() - par1Entity.getX(), mob.getBoundingBox().minY + (double) (mob.getBbHeight() / 2.0F) - par1Entity.getY() + (double) par1Entity.getEyeHeight(),
                mob.getZ() - par1Entity.getZ());
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.getX() + (mob.level.random.nextDouble() - 0.5D) * 8.0D - vector.x * telDist;
        double destY = mob.getY() + (double) (mob.level.random.nextInt(16) - 8) - vector.y * telDist;
        double destZ = mob.getZ() + (mob.level.random.nextDouble() - 0.5D) * 8.0D - vector.z * telDist;
        // forge event hook
        EntityTeleportEvent event = new EntityTeleportEvent(mob, destX, destY, destZ);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return false;

        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(LivingEntity mob, double destX, double destY, double destZ) {
        double oldX = mob.getX();
        double oldY = mob.getY();
        double oldZ = mob.getZ();
        boolean success = false;
        mob.setPos(destX, destY, destZ);
        int x = Mth.floor(mob.getX());
        int y = Mth.floor(mob.getY());
        int z = Mth.floor(mob.getZ());

        boolean hitGround = false;
        while (!hitGround && y < 96 && y > 0) {
            BlockState bs = mob.level.getBlockState(new BlockPos(x, y - 1, z));
            if (bs.getMaterial().blocksMotion()) {
                hitGround = true;
            } else {
                mob.setPos(destX, --destY, destZ);
                --y;
            }
        }

        if (hitGround) {
            mob.setPos(mob.getX(), mob.getY(), mob.getZ());

            if (mob.randomTeleport(destX, destY, destZ, true)) {
                success = true;
            }
        } else {
            return false;
        }

        if (!success) {
            mob.setPos(oldX, oldY, oldZ);
            return false;
        } else {
            short range = 128;
            for (int i = 0; i < range; ++i) {
                double var19 = (double) i / ((double) range - 1.0D);
                float var21 = (mob.level.random.nextFloat() - 0.5F) * 0.2F;
                float var22 = (mob.level.random.nextFloat() - 0.5F) * 0.2F;
                float var23 = (mob.level.random.nextFloat() - 0.5F) * 0.2F;
                double var24 = oldX + (mob.getX() - oldX) * var19 + (mob.level.random.nextDouble() - 0.5D) * (double) mob.getBbWidth() * 2.0D;
                double var26 = oldY + (mob.getY() - oldY) * var19 + mob.level.random.nextDouble() * (double) mob.getBbHeight();
                double var28 = oldZ + (mob.getZ() - oldZ) * var19 + (mob.level.random.nextDouble() - 0.5D) * (double) mob.getBbWidth() * 2.0D;
                mob.level.addParticle(ParticleTypes.PORTAL, var24, var26, var28, (double) var21, (double) var22, (double) var23);
            }

            mob.level.playSound(null, new BlockPos(oldX, oldY, oldZ), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(),
                    mob.getRandom().nextFloat() * 0.7F + 0.3F);
            mob.level.playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(), mob.getRandom().nextFloat() * 0.7F + 0.3F);
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
