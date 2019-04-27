package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

public class MM_Rust extends MobModifier {

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
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof EntityPlayer)
                && !(source instanceof EntityDamageSourceIndirect)) {
            EntityPlayer p = (EntityPlayer) source.getTrueSource();
            if (p.inventory.getCurrentItem() != null) {
                p.inventory.getCurrentItem().damageItem(4, (EntityLivingBase) source.getTrueSource());
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage) {
        if (entity != null
                && entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).inventory.damageArmor(damage * 3);
        }

        return super.onAttack(entity, source, damage);
    }

    @Override
    protected String[] getModNameSuffix() {
        return suffix;
    }

    private static String[] suffix = {"ofDecay", "theEquipmentHaunter"};

    @Override
    protected String[] getModNamePrefix() {
        return prefix;
    }

    private static String[] prefix = {"rusting", "decaying"};

}
