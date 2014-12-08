package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ghastly extends MobModifier
{
    public MM_Ghastly(EntityLivingBase mob)
    {
        this.modName = "Ghastly";
    }
    
    public MM_Ghastly(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Ghastly";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 6000L;
    private final static float MIN_DISTANCE = 3F;
    
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
            double diffX = target.posX - mob.posX;
            double diffY = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (mob.posY + (double)(mob.height / 2.0F));
            double diffZ = target.posZ - mob.posZ;
            mob.renderYawOffset = mob.rotationYaw = -((float)Math.atan2(diffX, diffZ)) * 180.0F / (float)Math.PI;

            mob.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1008, new BlockPos((int)mob.posX, (int)mob.posY, (int)mob.posZ), 0);
            EntityLargeFireball entFB = new EntityLargeFireball(mob.worldObj, mob, diffX, diffY, diffZ);
            double spawnOffset = 2.0D;
            Vec3 mobLook = mob.getLook(1.0F);
            entFB.posX = mob.posX + mobLook.xCoord * spawnOffset;
            entFB.posY = mob.posY + (double)(mob.height / 2.0F) + 0.5D;
            entFB.posZ = mob.posZ + mobLook.zCoord * spawnOffset;
            mob.worldObj.spawnEntityInWorld(entFB);
        }
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "OMFGFIREBALLS", "theBomber", "ofBallsofFire" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "bombing", "fireballsy" };
    
}
