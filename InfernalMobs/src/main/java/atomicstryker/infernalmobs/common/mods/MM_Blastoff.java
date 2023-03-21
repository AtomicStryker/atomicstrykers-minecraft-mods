package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;

public class MM_Blastoff extends MobModifier {

    private final static long coolDown = 15000L;
    private static Class<?>[] modBans = {MM_Webber.class};
    private static String[] suffix = {"ofMissionControl", "theNASA", "ofWEE"};
    private static String[] prefix = {"thumping", "trolling", "byebye"};
    private long nextAbilityUse = 0L;

    public MM_Blastoff() {
        super();
    }

    public MM_Blastoff(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Blastoff";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()
                && getMobTarget() instanceof Player) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getDirectEntity() != null
                && source.getDirectEntity() instanceof LivingEntity) {
            tryAbility(mob, (LivingEntity) source.getDirectEntity());
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !canMobSeeTarget(mob, target)) {
            return;
        }

        long time = System.currentTimeMillis();
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            mob.level.playSound(null, mob.blockPosition(), SoundEvents.SLIME_JUMP, SoundSource.HOSTILE, 1.0F + mob.getRandom().nextFloat(), mob.getRandom().nextFloat() * 0.7F + 0.3F);

            if (target.level.isClientSide || !(target instanceof ServerPlayer)) {
                target.push(0, 1.1D, 0);
            } else {
                InfernalMobsCore.instance().sendVelocityPacket((ServerPlayer) target, 0f, 1.1f, 0f);
            }
        }
    }

    @Override
    public Class<?>[] getModsNotToMixWith() {
        return modBans;
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
