package atomicstryker.stalkercreepers.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public class EntityAIHelperStalker
{
    public static boolean isSeenByTarget(EntityLiving stalker)
    {
        EntityLivingBase seer = stalker.getAttackTarget();
        
        if (stalker == null || seer == null) return true;
        
        Vec3 visionVec = seer.getLook(1.0F).normalize();
        Vec3 targetVec = new Vec3(stalker.posX - seer.posX,
                                            stalker.getEntityBoundingBox().minY + (double)(stalker.height / 2.0F) - (seer.posY + (double)seer.getEyeHeight()),
                                            stalker.posZ - seer.posZ);
        targetVec = targetVec.normalize();
        double dotProduct = visionVec.dotProduct(targetVec);
        
        boolean inFOV = dotProduct > 0.1 && seer.canEntityBeSeen(stalker);

        //System.out.println("dotProduct result in isSeenByTarget: "+dotProduct+"; inFOV: "+inFOV);
        
        return inFOV;
    }
}
