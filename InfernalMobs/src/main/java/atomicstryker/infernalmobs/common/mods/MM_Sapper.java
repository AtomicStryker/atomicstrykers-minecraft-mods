package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;

public class MM_Sapper extends MobModifier {

    private static String[] suffix = {"ofHunger", "thePaleRider"};
    private static String[] prefix = {"hungering", "starving"};

    public MM_Sapper() {
        super();
    }

    public MM_Sapper(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Sapper";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getEntity() != null
                && (source.getEntity() instanceof LivingEntity)
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getEntity())) {
            LivingEntity ent = (LivingEntity) source.getEntity();
            if (!ent.hasEffect(MobEffects.HUNGER)) {
                ent.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity)
                && !entity.hasEffect(MobEffects.POISON)) {
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
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
