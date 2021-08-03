package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;

public class MM_Cloaking extends MobModifier {

    private final static long coolDown = 10000L;
    private static Class<?>[] disallowed = {Spider.class};
    private static String[] suffix = {"ofStalking", "theUnseen", "thePredator"};
    private static String[] prefix = {"stalking", "unseen", "hunting"};
    private long nextAbilityUse = 0L;

    public MM_Cloaking() {
        super();
    }

    public MM_Cloaking(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Cloaking";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()
                && getMobTarget() instanceof Player) {
            tryAbility(mob);
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() != null
                && source.getEntity() instanceof LivingEntity) {
            tryAbility(mob);
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob) {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
        }
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
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
