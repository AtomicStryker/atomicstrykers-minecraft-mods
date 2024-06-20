package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

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
        if (isDirectAttack(source)
                && (source.getDirectEntity() instanceof Player p)
                && !isCreativePlayer(p)) {
            p.getInventory().getSelected().hurtAndBreak(4,
                    (LivingEntity) source.getDirectEntity(), LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof Player) {
            hurtArmor((Player) entity, entity.damageSources().magic(), damage * 3);
        }
        return super.onAttack(entity, source, damage);
    }

    private void hurtArmor(Player player, DamageSource damageSource, float pDamageAmount) {
        if (pDamageAmount > 0.0F) {
            int i = (int) Math.max(1.0F, pDamageAmount / 4.0F);
            EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
            for (EquipmentSlot equipmentSlot : slots) {
                ItemStack itemstack = player.getItemBySlot(equipmentSlot);
                if (itemstack.getItem() instanceof ArmorItem && itemstack.canBeHurtBy(damageSource)) {
                    itemstack.hurtAndBreak(i, player, equipmentSlot);
                }
            }
        }
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
