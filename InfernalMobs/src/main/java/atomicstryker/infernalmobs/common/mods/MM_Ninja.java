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
        if (time > nextAbilityUse && source.getTrueSource() != null && source.getTrueSource() != mob && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getTrueSource())
                && teleportToEntity(mob, source.getTrueSource())) {
            nextAbilityUse = time + coolDown;
            source.getTrueSource().attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));
            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(LivingEntity mob, Entity par1Entity) {
        Vec3d vector = new Vec3d(mob.getPosX() - par1Entity.getPosX(), mob.getBoundingBox().minY + (double) (mob.getHeight() / 2.0F) - par1Entity.getPosY() + (double) par1Entity.getEyeHeight(),
                mob.getPosZ() - par1Entity.getPosZ());
        vector = vector.normalize();
        double telDist = 8.0D;
        double destX = mob.getPosX() + (mob.world.rand.nextDouble() - 0.5D) * 4.0D - vector.x * telDist;
        double destY = mob.getPosY() + (double) (mob.world.rand.nextInt(16) - 4) - vector.y * telDist;
        double destZ = mob.getPosZ() + (mob.world.rand.nextDouble() - 0.5D) * 4.0D - vector.z * telDist;

        // forge event hook
        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(mob, destX, destY, destZ, 0);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return false;

        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(LivingEntity mob, double destX, double destY, double destZ) {
        double oldX = mob.getPosX();
        double oldY = mob.getPosY();
        double oldZ = mob.getPosZ();
        boolean success = false;
        mob.setPosition(destX, destY, destZ);
        int x = MathHelper.floor(mob.getPosX());
        int y = MathHelper.floor(mob.getPosY());
        int z = MathHelper.floor(mob.getPosZ());

        boolean hitGround = false;
        while (!hitGround && y < 96 && y > 0) {
            BlockState bs = mob.world.getBlockState(new BlockPos(x, y - 1, z));
            if (bs.getMaterial().blocksMovement()) {
                hitGround = true;
            } else {
                mob.setPosition(destX, --destY, destZ);
                --y;
            }
        }

        if (hitGround) {
            mob.setPosition(mob.getPosX(), mob.getPosY(), mob.getPosZ());
            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
            mob.world.addParticle(ParticleTypes.EXPLOSION, oldX, oldY, oldZ, 0D, 0D, 0D);

            if (mob.attemptTeleport(destX, destY, destZ, true)) {
                success = true;
            }
        } else {
            return false;
        }

        if (!success) {
            mob.setPosition(oldX, oldY, oldZ);
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
