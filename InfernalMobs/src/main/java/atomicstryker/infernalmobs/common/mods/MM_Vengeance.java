package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class MM_Vengeance extends MobModifier {

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
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && source.getTrueSource() != mob
                && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getTrueSource())) {
            source.getTrueSource().attackEntityFrom(DamageSource.causeMobDamage(mob),
                    InfernalMobsCore.instance().getLimitedDamage(Math.max(damage / 2, 1)));
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = {"ofRetribution", "theThorned", "ofStrikingBack"};

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = {"thorned", "thorny", "spiky"};

}
