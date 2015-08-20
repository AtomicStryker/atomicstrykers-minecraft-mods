package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

public class MM_Gravity extends MobModifier
{
    
    public MM_Gravity()
    {
        super();
    }
    
    public MM_Gravity(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Gravity";
    }

    private long nextAbilityUse = 0L;
    private final static long coolDown = 5000L;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob, getMobTarget());
        }
        
        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;

            double diffX = target.posX - mob.posX;
            double diffZ;
            for (diffZ = target.posZ - mob.posZ; diffX * diffX + diffZ * diffZ < 1.0E-4D; diffZ = (Math.random() - Math.random()) * 0.01D)
            {
                diffX = (Math.random() - Math.random()) * 0.01D;
            }
            
            mob.worldObj.playSoundAtEntity(mob, "mob.irongolem.throw", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            
            if (mob.worldObj.isRemote || !(target instanceof EntityPlayerMP))
            {
                knockBack(target, diffX, diffZ);
            }
            else
            {
                InfernalMobsCore.instance().sendKnockBackPacket((EntityPlayerMP) target, (float)diffX, (float)diffZ);
            }
        }
    }
    
    public static void knockBack(EntityLivingBase target, double x, double z)
    {
        target.isAirBorne = true;
        float normalizedPower = MathHelper.sqrt_double(x * x + z * z);
        float knockPower = 0.8F;
        target.motionX /= 2.0D;
        target.motionY /= 2.0D;
        target.motionZ /= 2.0D;
        target.motionX -= x / (double)normalizedPower * (double)knockPower;
        target.motionY += (double)knockPower;
        target.motionZ -= z / (double)normalizedPower * (double)knockPower;

        if (target.motionY > 0.4000000059604645D)
        {
            target.motionY = 0.4000000059604645D;
        }
    }
    
    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Webber.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofRepulsion", "theFlipper" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "repulsing", "sproing" };
    
}
