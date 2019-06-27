package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

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
                tryAbility(mob, mob.world.getClosestPlayer(mob, 12f));
            }
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(LivingEntity mob, LivingEntity target) {
        if (target == null || !mob.canEntityBeSeen(target)) {
            return;
        }

        if (mob.getDistanceSq(target) > MIN_DISTANCE) {
            double diffX = target.posX + target.getMotion().x - mob.posX;
            double diffY = target.posY + (double) target.getEyeHeight() - 1.100000023841858D - mob.posY;
            double diffZ = target.posZ + target.getMotion().z - mob.posZ;
            float distance = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

            Potion potiontype = Potions.HARMING;

            if (distance >= 8.0F && !target.isPotionActive(Effects.SLOWNESS)) {
                potiontype = Potions.SLOWNESS;
            } else if (target.getHealth() >= 8.0F && !target.isPotionActive(Effects.POISON)) {
                potiontype = Potions.POISON;
            } else if (distance <= 3.0F && !target.isPotionActive(Effects.WEAKNESS) && mob.getRNG().nextFloat() < 0.25F) {
                potiontype = Potions.WEAKNESS;
            }

            PotionEntity potionentity = new PotionEntity(mob.world, mob);
            potionentity.setItem(PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potiontype));
            potionentity.rotationPitch -= -20.0F;
            potionentity.shoot(diffX, diffY + (double) (distance * 0.2F), diffZ, 0.75F, 8.0F);
            mob.world.playSound(null, mob.posX, mob.posY, mob.posZ, SoundEvents.ENTITY_WITCH_THROW, mob.getSoundCategory(), 1.0F, 0.8F + mob.world.rand.nextFloat() * 0.4F);
            mob.world.addEntity(potionentity);
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
