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

public class MM_Ninja extends MobModifier {

    private final static long coolDown = 15000L;
    private static String[] suffix = {"theZenMaster", "ofEquilibrium", "ofInnerPeace"};
    private static String[] prefix = {"totallyzen", "innerlypeaceful", "Ronin"};
    private long nextAbilityUse = 0L;

    public MM_Ninja() {
        super();
    }

    public MM_Ninja(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Ninja";
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
        double telDist = 8.0D;
        double destX = mob.getX() + (mob.level.random.nextDouble() - 0.5D) * 4.0D - vector.x * telDist;
        double destY = mob.getY() + (double) (mob.level.random.nextInt(16) - 4) - vector.y * telDist;
        double destZ = mob.getZ() + (mob.level.random.nextDouble() - 0.5D) * 4.0D - vector.z * telDist;

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
            mob.level.playSound(null, mob.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(), mob.getRandom().nextFloat() * 0.7F + 0.3F);
            mob.level.addParticle(ParticleTypes.EXPLOSION, oldX, oldY, oldZ, 0D, 0D, 0D);

            if (mob.randomTeleport(destX, destY, destZ, true)) {
                success = true;
            }
        } else {
            return false;
        }

        if (!success) {
            mob.setPos(oldX, oldY, oldZ);
            return false;
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
