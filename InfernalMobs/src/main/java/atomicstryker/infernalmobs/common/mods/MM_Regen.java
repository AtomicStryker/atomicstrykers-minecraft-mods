package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;

public class MM_Regen extends MobModifier {

    private final static long coolDown = 500L;
    private static String[] suffix = {"ofWTFIMBA", "theCancerous", "ofFirstAid"};
    private static String[] prefix = {"regenerating", "healing", "nighunkillable"};
    private long nextAbilityUse = 0L;
    public MM_Regen() {
        super();
    }

    public MM_Regen(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Regen";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (mob.getHealth() < getActualMaxHealth(mob)) {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth() + 1);
            }
        }
        return super.onUpdate(mob);
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
