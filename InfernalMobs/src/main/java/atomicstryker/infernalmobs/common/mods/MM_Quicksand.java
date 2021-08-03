package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class MM_Quicksand extends MobModifier {

    private static String[] suffix = {"ofYouCantRun", "theSlowingB"};
    private static String[] prefix = {"slowing", "Quicksand"};
    int ticker = 0;

    public MM_Quicksand() {
        super();
    }

    public MM_Quicksand(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Quicksand";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()
                && InfernalMobsCore.instance().getIsEntityAllowedTarget(getMobTarget())
                && canMobSeeTarget(mob, getMobTarget())
                && ++ticker == 50) {
            ticker = 0;
            getMobTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 45, 0));
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
