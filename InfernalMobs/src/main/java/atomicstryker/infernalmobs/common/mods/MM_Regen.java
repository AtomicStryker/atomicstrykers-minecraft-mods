package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;

public class MM_Regen extends MobModifier {

    private final static long coolDown = 1000L;
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
        float health = mob.getHealth();
        float actualMaxHealth = getActualMaxHealth(mob);
        if (!mob.level.isClientSide && health < actualMaxHealth) {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                if (!mob.isOnFire()) {
                    mob.setHealth(Math.min(health + 1, actualMaxHealth));
                }
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
