package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

public class MM_Lifesteal extends MobModifier {

    private static Class<?>[] disallowed = {Creeper.class};
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

        if (entity != null && source.getDirectEntity() != null) {
            LivingEntity mob = (LivingEntity) source.getDirectEntity();
            if (!mob.level.isClientSide && mob.getHealth() < getActualMaxHealth(mob)) {
                mob.setHealth(mob.getHealth() + damage);
            }
        }
        return super.
                onAttack(entity, source, damage);

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
