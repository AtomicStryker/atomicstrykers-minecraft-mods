package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

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
                && mob.canEntityBeSeen(getMobTarget())
                && ++ticker == 50) {
            ticker = 0;
            getMobTarget().addPotionEffect(new EffectInstance(Effects.SLOWNESS, 45, 0));
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
