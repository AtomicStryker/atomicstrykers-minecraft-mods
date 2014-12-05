package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Alchemist extends MobModifier
{
    public MM_Alchemist(EntityLivingBase mob)
    {
        this.modName = "Alchemist";
    }
    
    public MM_Alchemist(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Alchemist";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 6000L;
    private final static float MIN_DISTANCE = 2F;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            tryAbility(mob, mob.worldObj.getClosestPlayerToEntity(mob, 12f));
        }
        return super.onUpdate(mob);
    }
    
    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        if (mob.getDistanceToEntity(target) > MIN_DISTANCE)
        {
            EntityPotion potion = new EntityPotion(mob.worldObj, mob, 32732);
            potion.rotationPitch -= -20.0F;
            double diffX = target.posX + target.motionX - mob.posX;
            double diffY = target.posY + (double)target.getEyeHeight() - 1.100000023841858D - mob.posY;
            double diffZ = target.posZ + target.motionZ - mob.posZ;
            float distance = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

            if (distance >= 8.0F && !target.isPotionActive(Potion.moveSlowdown))
            {
                potion.setPotionDamage(32698);
            }
            else if (target.getHealth() >= 8 && !target.isPotionActive(Potion.poison))
            {
                potion.setPotionDamage(32660);
            }
            else if (distance <= 3.0F && !target.isPotionActive(Potion.weakness) && mob.getRNG().nextFloat() < 0.25F)
            {
                potion.setPotionDamage(32696);
            }

            potion.setThrowableHeading(diffX, diffY + (double)(distance * 0.2F), diffZ, 0.75F, 8.0F);
            mob.worldObj.spawnEntityInWorld(potion);
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
