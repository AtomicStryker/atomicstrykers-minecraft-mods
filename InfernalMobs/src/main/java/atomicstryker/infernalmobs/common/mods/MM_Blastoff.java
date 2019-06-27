package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

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
                && getMobTarget() instanceof PlayerEntity) {
            tryAbility(mob, getMobTarget());
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && source.getTrueSource() instanceof LivingEntity) {
            tryAbility(mob, (LivingEntity) source.getTrueSource());
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        long time = System.currentTimeMillis();
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);

            if (target.world.isRemote || !(target instanceof ServerPlayerEntity)) {
                target.addVelocity(0, 1.1D, 0);
            } else {
                InfernalMobsCore.instance().sendVelocityPacket((ServerPlayerEntity) target, 0f, 1.1f, 0f);
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
