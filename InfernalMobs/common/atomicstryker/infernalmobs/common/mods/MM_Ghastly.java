package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ghastly extends MobModifier
{
    public MM_Ghastly(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Ghastly";
    }
    
    public MM_Ghastly(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Ghastly";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 6000L;
    private final static float MIN_DISTANCE = 3F;
    
    @Override
    public boolean onUpdate()
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(getMobTarget());
        }
        
        return super.onUpdate();
    }
    
    private void tryAbility(EntityLiving target)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
        && mob.getDistanceToEntity(target) > MIN_DISTANCE)
        {
            nextAbilityUse = time+coolDown;

            double diffX = target.posX - mob.posX;
            double diffY = target.boundingBox.minY + (double)(target.height / 2.0F) - (mob.posY + (double)(mob.height / 2.0F));
            double diffZ = target.posZ - mob.posZ;
            mob.renderYawOffset = mob.rotationYaw = -((float)Math.atan2(diffX, diffZ)) * 180.0F / (float)Math.PI;

            if (mob.canEntityBeSeen(target))
            {
                mob.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1008, (int)mob.posX, (int)mob.posY, (int)mob.posZ, 0);
                EntitySmallFireball entFB = new EntitySmallFireball(mob.worldObj, mob, diffX, diffY, diffZ);
                double spawnOffset = 2.0D;
                Vec3 mobLook = mob.getLook(1.0F);
                entFB.posX = mob.posX + mobLook.xCoord * spawnOffset;
                entFB.posY = mob.posY + (double)(mob.height / 2.0F) + 0.5D;
                entFB.posZ = mob.posZ + mobLook.zCoord * spawnOffset;
                mob.worldObj.spawnEntityInWorld(entFB);
            }
            target = null;
        }
    }
    
    @Override
    public Class[] getWhiteListMobClasses()
    {
        return allowed;
    }
    private static Class[] allowed = { EntityMob.class, EntityWolf.class };
}
