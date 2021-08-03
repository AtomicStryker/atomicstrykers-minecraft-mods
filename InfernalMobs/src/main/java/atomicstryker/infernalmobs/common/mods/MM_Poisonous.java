package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;

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
        if (source.getEntity() != null
                && (source.getEntity() instanceof LivingEntity)
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getEntity())) {
            LivingEntity ent = (LivingEntity) source.getEntity();
            if (!ent.hasEffect(MobEffects.POISON)
                    && !(source instanceof IndirectEntityDamageSource)) {
                ent.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0));
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity)
                && !entity.hasEffect(MobEffects.POISON)) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0));
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
