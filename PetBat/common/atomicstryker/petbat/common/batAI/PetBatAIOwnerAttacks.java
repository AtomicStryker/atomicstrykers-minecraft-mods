package atomicstryker.petbat.common.batAI;

import atomicstryker.petbat.common.EntityPetBat;
import net.minecraft.src.EntityAITarget;
import net.minecraft.src.EntityLiving;

public class PetBatAIOwnerAttacks extends EntityAITarget
{
    private EntityPetBat batEnt;
    private EntityLiving theTarget;
    
    public PetBatAIOwnerAttacks(EntityPetBat bat)
    {
        super(bat, 16F, false);
        batEnt = bat;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute()
    {
        if (batEnt.getOwnerEntity() != null)
        {
            theTarget = batEnt.getOwnerEntity().getLastAttackingEntity();
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
