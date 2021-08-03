package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

public class MM_Bulwark extends MobModifier {

    private static String[] suffix = {"ofTurtling", "theDefender", "ofeffingArmor"};
    private static String[] prefix = {"turtling", "defensive", "armoured"};

    public MM_Bulwark() {
        super();
    }

    public MM_Bulwark(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Bulwark";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        return super.onHurt(mob, source, Math.max(damage / 2, 1));
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
