package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.AbstractTeleporter;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;

public class MM_Ninja extends AbstractTeleporter {

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
                && tryTeleportWithTarget(mob, source.getEntity())) {
            nextAbilityUse = time + coolDown;
            source.getEntity().hurt(DamageSource.mobAttack(mob), InfernalMobsCore.instance().getLimitedDamage(damage));
            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    protected void playStartEffects(LivingEntity mob, double x, double y, double z) {
        BlockPos soundPos = new BlockPos(x, y, z);
        mob.level.playSound(null, soundPos, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(), mob.getRandom().nextFloat() * 0.7F + 0.3F);
        ((ServerLevel)mob.level).sendParticles(ParticleTypes.EXPLOSION, x, y+1, z, 2, 1D, 0.2D, 0.2D, 0.0D);
    }

    @Override
    protected void playDestinationEffects(LivingEntity mob) {
        // no destination effect
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
