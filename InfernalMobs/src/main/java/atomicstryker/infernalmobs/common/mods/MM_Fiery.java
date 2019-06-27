package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;

public class MM_Fiery extends MobModifier {

    private static String[] suffix = {"ofConflagration", "thePhoenix", "ofCrispyness"};
    private static String[] prefix = {"burning", "toasting"};

    public MM_Fiery() {
        super();
    }

    public MM_Fiery(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Fiery";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof LivingEntity)
                && !(source instanceof IndirectEntityDamageSource)) {
            source.getTrueSource().setFire(3);
        }

        mob.extinguish();
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            entity.setFire(3);
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
