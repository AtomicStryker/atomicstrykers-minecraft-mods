package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
                && wantsToAttack(mob, p)) {
            ItemStack equippedStack = p.getInventory().getEquipment().get(EquipmentSlot.MAINHAND);
            equippedStack.hurtAndBreak(4, (LivingEntity) source.getDirectEntity(), EquipmentSlot.MAINHAND);
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity instanceof Player) {
            hurtEquipment((Player) entity, entity.damageSources().magic(), damage * 3);
        }
        return super.onAttack(entity, source, damage);
    }

    private void hurtEquipment(Player player, DamageSource damageSource, float damage) {
        if (damage > 0.0F) {
            int i = (int) Math.max(1.0F, damage / 4.0F);
            for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
                ItemStack itemstack = player.getItemBySlot(equipmentSlot);
                if (itemstack.canBeHurtBy(damageSource)) {
                    itemstack.hurtAndBreak(i, player, equipmentSlot);
                    break;
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
