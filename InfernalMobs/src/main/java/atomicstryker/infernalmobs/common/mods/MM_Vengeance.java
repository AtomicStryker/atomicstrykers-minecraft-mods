package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class MM_Vengeance extends MobModifier {

    private static String[] suffix = {"ofRetribution", "theThorned", "ofStrikingBack"};
    private static String[] prefix = {"thorned", "thorny", "spiky"};

    public MM_Vengeance() {
        super();
    }

    public MM_Vengeance(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Vengeance";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getDirectEntity() != null
                && source.getDirectEntity() != mob
                && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getDirectEntity())) {
            source.getDirectEntity().hurt(source.getDirectEntity().damageSources().mobAttack(mob),
                    InfernalMobsCore.instance().getLimitedDamage(Math.max(damage / 2, 1)));
        }

        return super.onHurt(mob, source, damage);
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
