package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class MM_Sticky extends MobModifier {

    private final static long coolDown = 15000L;
    private static String[] suffix = {"ofSnagging", "theQuickFingered", "ofPettyTheft", "yoink"};
    private static String[] prefix = {"thieving", "snagging", "quickfingered"};
    private long nextAbilityUse = 0L;
    private Class<?>[] disallowed = {CreeperEntity.class};

    public MM_Sticky() {
        super();
    }

    public MM_Sticky(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Sticky";
    }

    @Override
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (source.getTrueSource() != null
                && (source.getTrueSource() instanceof PlayerEntity)) {
            PlayerEntity p = (PlayerEntity) source.getTrueSource();
            ItemStack weapon = p.inventory.getStackInSlot(p.inventory.currentItem);
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse
                    && source.getTrueSource() != null
                    && !(source instanceof IndirectEntityDamageSource)) {
                nextAbilityUse = time + coolDown;
                ItemEntity drop = p.dropItem(p.inventory.decrStackSize(p.inventory.currentItem, 1), false);
                if (drop != null) {
                    drop.setPickupDelay(50);
                    mob.world.playSound(null, mob.getPosition(), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
                }
            }
        }

        return super.onHurt(mob, source, damage);
    }

    @Override
    public Class<?>[] getBlackListMobClasses() {
        return disallowed;
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
