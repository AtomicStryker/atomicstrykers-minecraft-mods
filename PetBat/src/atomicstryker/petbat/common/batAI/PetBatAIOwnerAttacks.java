package atomicstryker.petbat.common.batAI;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import atomicstryker.petbat.common.EntityPetBat;

public class PetBatAIOwnerAttacks extends EntityAITarget
{
    private EntityPetBat batEnt;
    private EntityLivingBase theTarget;
    
    public PetBatAIOwnerAttacks(EntityPetBat bat)
    {
        super(bat, false);
        batEnt = bat;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute()
    {
        if (batEnt.getOwnerEntity() != null)
        {
            theTarget = batEnt.getOwnerEntity().getLastAttacker();
            return this.isSuitableTarget(theTarget, false);
        }
        
        return false;
    }
    
    @Override
    public boolean continueExecuting()
    {
        return (theTarget != null && theTarget.isEntityAlive());
    }
    
    @Override
    public void resetTask()
    {
        theTarget = null;
    }
    
    @Override
    public void startExecuting()
    {
        batEnt.setAttackTarget(theTarget);
        super.startExecuting();
    }
}
