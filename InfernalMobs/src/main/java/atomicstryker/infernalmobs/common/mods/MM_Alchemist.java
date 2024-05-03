package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class MM_Alchemist extends MobModifier {

    private final static long coolDown = 6000L;
    private final static float MIN_DISTANCE = 2F;
    private static String[] suffix = {"theWitchkin", "theBrewmaster", "theSinged"};
    private static String[] prefix = {"witchkin", "brewing", "singed"};
    private long nextAbilityUse = 0L;

    public MM_Alchemist() {
        super();
    }

    public MM_Alchemist(MobModifier next) {
        super(next);
    }

    @Override
    public String getModName() {
        return "Alchemist";
    }

    @Override
    public boolean onUpdate(LivingEntity mob) {
        if (hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse) {
                nextAbilityUse = time + coolDown;
                tryAbility(mob, mob.level().getNearestPlayer(mob, 12f));
            }
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !canMobSeeTarget(mob, target)) {
            return;
        }

        if (mob.distanceToSqr(target) > MIN_DISTANCE) {
            float diffX = (float) (target.getX() + target.getDeltaMovement().x - mob.getX());
            float diffY = (float) (target.getY() + target.getEyeHeight() - 1.100000023841858D - mob.getY());
            float diffZ = (float) (target.getZ() + target.getDeltaMovement().z - mob.getZ());
            float distance = Mth.sqrt(diffX * diffX + diffZ * diffZ);

            Holder<Potion> potiontype = Potions.HARMING;

            if (distance >= 8.0F && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                potiontype = Potions.SLOWNESS;
            } else if (target.getHealth() >= 8.0F && !target.hasEffect(MobEffects.POISON)) {
                potiontype = Potions.POISON;
            } else if (distance <= 3.0F && !target.hasEffect(MobEffects.WEAKNESS) && mob.getRandom().nextFloat() < 0.25F) {
                potiontype = Potions.WEAKNESS;
            }

            ThrownPotion potionentity = new ThrownPotion(mob.level(), mob);
            potionentity.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, potiontype));
            potionentity.setXRot(potionentity.getXRot() + 20.0F);
            potionentity.shoot(diffX, diffY + (double) (distance * 0.2F), diffZ, 0.75F, 8.0F);
            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.WITCH_THROW, mob.getSoundSource(), 1.0F, 0.8F + mob.level().random.nextFloat() * 0.4F);
            mob.level().addFreshEntity(potionentity);
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
