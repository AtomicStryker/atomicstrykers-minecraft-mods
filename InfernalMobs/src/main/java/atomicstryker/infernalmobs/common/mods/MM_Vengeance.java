package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

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
        if (source.getEntity() != null
                && source.getEntity() != mob
                && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getEntity())) {
            source.getEntity().hurt(DamageSource.mobAttack(mob),
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
