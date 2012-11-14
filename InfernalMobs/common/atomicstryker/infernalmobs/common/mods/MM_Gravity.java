package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.MathHelper;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Gravity extends MobModifier
{
    public MM_Gravity(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Gravity";
        offensive = mob.getClass().isAssignableFrom(EntityMob.class);
    }
    
    public MM_Gravity(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Gravity";
        this.nextMod = prevMod;
        offensive = mob.getClass().isAssignableFrom(EntityMob.class);
    }
    
    private final boolean offensive;    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 5000L;
    
    @Override
    public boolean onUpdate()
    {
        if (offensive
        && mob.getAttackTarget() != null
        && mob.getAttackTarget() instanceof EntityPlayer)
        {
            tryAbility(mob.getAttackTarget());
        }
        
        return super.onUpdate();
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (!offensive
        && source.getEntity() != null
        && source.getEntity() instanceof EntityLiving)
        {
            tryAbility((EntityLiving) source.getEntity());
        }
        
        return super.onHurt(source, damage);
    }

    private void tryAbility(EntityLiving target)
    {
        long time = System.currentTimeMillis();
        if (time > lastAbilityUse+coolDown)
        {
            lastAbilityUse = time;
            
            EntityLiving source = offensive ? mob : target;
            EntityLiving destination = offensive ? target : mob;
            double diffX = destination.posX - source.posX;
            double diffZ;
            for (diffZ = destination.posZ - source.posZ; diffX * diffX + diffZ * diffZ < 1.0E-4D; diffZ = (Math.random() - Math.random()) * 0.01D)
            {
                diffX = (Math.random() - Math.random()) * 0.01D;
            }
            
            mob.worldObj.playSoundAtEntity(mob, "mob.irongolem.throw", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
            
            if (mob.worldObj.isRemote || !(target instanceof EntityPlayer))
            {
                knockBack(target, diffX, diffZ);
            }
            else
            {
                InfernalMobsCore.instance().sendKnockBackPacket((EntityPlayer) target, diffX, diffZ);
            }
        }
    }
    
    public static void knockBack(EntityLiving target, double x, double z)
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
    public Class[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class[] modBans = { MM_Webber.class };
}
