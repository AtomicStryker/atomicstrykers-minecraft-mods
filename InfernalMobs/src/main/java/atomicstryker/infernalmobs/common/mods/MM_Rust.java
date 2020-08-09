package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;

public class MM_Rust extends MobModifier {

    private static String[] suffix = {"ofDecay", "theEquipmentHaunter"};
    private static String[] prefix = {"rusting", "decaying"};

    public MM_Rust() {
        super();
    }

    public MM_Rust(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Rust";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof PlayerEntity)
                && !(source instanceof IndirectEntityDamageSource)) {
            PlayerEntity p = (PlayerEntity) source.getTrueSource();
            p.inventory.getCurrentItem();
            p.inventory.getCurrentItem().damageItem(4, (LivingEntity) source.getTrueSource(), (player) -> player.sendBreakAnimation(Hand.MAIN_HAND));
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).inventory.func_234563_a_(DamageSource.MAGIC, damage * 3);
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
