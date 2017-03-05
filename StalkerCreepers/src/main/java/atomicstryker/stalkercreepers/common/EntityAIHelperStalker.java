package atomicstryker.stalkercreepers.common;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("unused")
public class EntityAIHelperStalker
{

    public static boolean isSeenByTarget(EntityCreeper stalker)
    {
        EntityLivingBase seer = stalker.getAttackTarget();
        
        if (seer == null || stalker.getEntityBoundingBox() == null) return true;
        
        Vec3d visionVec = seer.getLook(1.0F).normalize();
        Vec3d targetVec = new Vec3d(stalker.posX - seer.posX,
                                            stalker.getEntityBoundingBox().minY + (double)(stalker.height / 2.0F) - (seer.posY + (double)seer.getEyeHeight()),
                                            stalker.posZ - seer.posZ);
        targetVec = targetVec.normalize();
        double dotProduct = visionVec.dotProduct(targetVec);
        
        boolean inFOV = dotProduct > 0.1 && seer.canEntityBeSeen(stalker);

        //System.out.println("dotProduct result in isSeenByTarget: "+dotProduct+"; inFOV: "+inFOV);
        
        return inFOV;
    }
}
