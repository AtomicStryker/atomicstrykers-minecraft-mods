package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

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
        if (isDirectAttack(source)) {
            source.getDirectEntity().setRemainingFireTicks(3 * 20);
        }

        mob.clearFire();
        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            entity.setRemainingFireTicks(3 * 20);
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
