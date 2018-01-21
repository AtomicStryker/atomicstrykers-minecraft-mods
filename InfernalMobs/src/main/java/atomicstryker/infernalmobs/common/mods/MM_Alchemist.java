package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.MathHelper;

public class MM_Alchemist extends MobModifier
{

    public MM_Alchemist()
    {
        super();
    }

    public MM_Alchemist(MobModifier next)
    {
        super(next);
    }

    private long nextAbilityUse = 0L;
    private final static long coolDown = 6000L;
    private final static float MIN_DISTANCE = 2F;

    @Override
    public String getModName()
    {
        return "Alchemist";
    }

    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (hasSteadyTarget()) {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse)
            {
                nextAbilityUse = time + coolDown;
                tryAbility(mob, mob.world.getClosestPlayerToEntity(mob, 12f));
            }
        }
        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
        {
            return;
        }

        if (mob.getDistanceSq(target) > MIN_DISTANCE)
        {
            double diffX = target.posX + target.motionX - mob.posX;
            double diffY = target.posY + (double) target.getEyeHeight() - 1.100000023841858D - mob.posY;
            double diffZ = target.posZ + target.motionZ - mob.posZ;
            float distance = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

            PotionType potiontype = PotionTypes.HARMING;

            if (distance >= 8.0F && !target.isPotionActive(MobEffects.SLOWNESS))
            {
                potiontype = PotionTypes.SLOWNESS;
            }
            else if (target.getHealth() >= 8.0F && !target.isPotionActive(MobEffects.POISON))
            {
                potiontype = PotionTypes.POISON;
            }
            else if (distance <= 3.0F && !target.isPotionActive(MobEffects.WEAKNESS) && mob.getRNG().nextFloat() < 0.25F)
            {
                potiontype = PotionTypes.WEAKNESS;
            }

            EntityPotion potion = new EntityPotion(mob.world, mob, PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potiontype));
            potion.rotationPitch -= -20.0F;
            potion.shoot(diffX, diffY + (double) (distance * 0.2F), diffZ, 0.75F, 8.0F);
            mob.world.spawnEntity(potion);
        }
    }

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "theWitchkin", "theBrewmaster", "theSinged" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "witchkin", "brewing", "singed" };

}
