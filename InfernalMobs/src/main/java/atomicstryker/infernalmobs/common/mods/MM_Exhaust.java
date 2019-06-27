package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;

public class MM_Exhaust extends MobModifier {

    private static String[] suffix = {"ofFatigue", "theDrainer"};
    private static String[] prefix = {"exhausting", "draining"};

    public MM_Exhaust() {
        super();
    }

    public MM_Exhaust(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Exhaust";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof PlayerEntity)) {
            ((PlayerEntity) source.getTrueSource()).addExhaustion(1F);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null
                && entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).addExhaustion(1F);
        }

        return super.onAttack(entity, source, damage);
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
