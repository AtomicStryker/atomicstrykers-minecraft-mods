package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;

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
        if (source.getDirectEntity() != null
                && (source.getDirectEntity() instanceof Player)) {
            ((Player) source.getDirectEntity()).causeFoodExhaustion(1F);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof Player p && !isCreativePlayer(p)) {
            p.causeFoodExhaustion(1F);
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
