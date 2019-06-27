package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;

public class MM_Poisonous extends MobModifier {

    private static String[] suffix = {"ofVenom", "thedeadlyChalice"};
    private static String[] prefix = {"poisonous", "stinging", "despoiling"};

    public MM_Poisonous() {
        super();
    }

    public MM_Poisonous(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Poisonous";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof LivingEntity)
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getTrueSource())) {
            LivingEntity ent = (LivingEntity) source.getTrueSource();
            if (!ent.isPotionActive(Effects.POISON)
                    && !(source instanceof IndirectEntityDamageSource)) {
                ent.addPotionEffect(new EffectInstance(Effects.POISON, 120, 0));
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity)
                && !entity.isPotionActive(Effects.POISON)) {
            entity.addPotionEffect(new EffectInstance(Effects.POISON, 120, 0));
        }

        return super.onAttack(entity, source, damage);
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
