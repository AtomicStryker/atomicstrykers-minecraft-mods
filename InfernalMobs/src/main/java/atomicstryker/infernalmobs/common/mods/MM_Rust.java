package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

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
        if (source.getEntity() != null
                && (source.getEntity() instanceof Player)
                && !(source instanceof IndirectEntityDamageSource)) {
            Player p = (Player) source.getEntity();
            p.getInventory().getSelected();
            p.getInventory().getSelected().hurtAndBreak(4, (LivingEntity) source.getEntity(), (player) -> player.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof Player) {
            ((Player) entity).getInventory().hurtArmor(DamageSource.MAGIC, damage * 3, Inventory.ALL_ARMOR_SLOTS);
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
