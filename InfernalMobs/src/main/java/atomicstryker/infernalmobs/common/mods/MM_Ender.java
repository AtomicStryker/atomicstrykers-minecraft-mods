package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.AbstractTeleporter;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class MM_Ender extends AbstractTeleporter {

    private final static long coolDown = 15000L;
    private static String[] suffix = {"theEnderborn", "theTrickster"};
    private static String[] prefix = {"enderborn", "tricky"};
    private long nextAbilityUse = 0L;

    public MM_Ender() {
        super();
    }

    public MM_Ender(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Ender";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse && source.getEntity() != null && source.getEntity() != mob && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getEntity())
                && tryTeleportWithTarget(mob, source.getEntity())) {
            nextAbilityUse = time + coolDown;
            source.getEntity().hurt(DamageSource.mobAttack(mob), InfernalMobsCore.instance().getLimitedDamage(damage));

            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
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
