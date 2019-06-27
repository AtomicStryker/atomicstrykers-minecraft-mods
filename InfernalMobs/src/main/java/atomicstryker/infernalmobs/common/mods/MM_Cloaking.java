package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;

public class MM_Cloaking extends MobModifier {

    private final static long coolDown = 10000L;
    private static Class<?>[] disallowed = {SpiderEntity.class};
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
                && getMobTarget() instanceof PlayerEntity) {
            tryAbility(mob);
        }

        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && source.getTrueSource() instanceof LivingEntity) {
            tryAbility(mob);
        }

        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(LivingEntity mob) {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse) {
            nextAbilityUse = time + coolDown;
            mob.addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 200));
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
