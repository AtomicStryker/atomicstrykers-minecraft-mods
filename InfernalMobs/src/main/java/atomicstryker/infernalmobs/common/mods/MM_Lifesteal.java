package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.DamageSource;

public class MM_Lifesteal extends MobModifier {

    private static Class<?>[] disallowed = {CreeperEntity.class};
    private static String[] suffix = {"theVampire", "ofTransfusion", "theBloodsucker"};
    private static String[] prefix = {"vampiric", "transfusing", "bloodsucking"};

    public MM_Lifesteal() {
        super();
    }

    public MM_Lifesteal(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "LifeSteal";
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        LivingEntity mob = (LivingEntity) source.getTrueSource();
        if (entity != null
                && mob.getHealth() < getActualMaxHealth(mob)) {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth() + damage);
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
