package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.DamageSource;

public class MM_Berserk extends MobModifier {

    private static Class<?>[] disallowed = {CreeperEntity.class};
    private static String[] suffix = {"ofRecklessness", "theRaging", "ofSmashing"};
    private static String[] prefix = {"reckless", "raging", "smashing"};

    public MM_Berserk() {
        super();
    }

    public MM_Berserk(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Berserk";
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            source.getTrueSource().attackEntityFrom(DamageSource.GENERIC, damage);
            damage *= 2;
            damage = InfernalMobsCore.instance().getLimitedDamage(damage);
        }

        return super.onAttack(entity, source, damage);
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
