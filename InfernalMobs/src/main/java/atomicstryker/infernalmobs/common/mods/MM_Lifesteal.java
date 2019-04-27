package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;

public class MM_Lifesteal extends MobModifier {

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
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        EntityLivingBase mob = (EntityLivingBase) source.getTrueSource();
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

    private static Class<?>[] disallowed = {EntityCreeper.class};

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = {"theVampire", "ofTransfusion", "theBloodsucker"};

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = {"vampiric", "transfusing", "bloodsucking"};

}
