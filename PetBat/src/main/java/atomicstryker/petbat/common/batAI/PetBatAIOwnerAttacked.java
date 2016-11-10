package atomicstryker.petbat.common.batAI;

import atomicstryker.petbat.common.EntityPetBat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class PetBatAIOwnerAttacked extends EntityAITarget
{
    private EntityPetBat batEnt;
    private EntityLivingBase theOwnerAttacker;
    
    public PetBatAIOwnerAttacked(EntityPetBat bat)
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
            theOwnerAttacker = batEnt.getOwnerEntity().getAITarget();
            return theOwnerAttacker != batEnt.getOwnerEntity() && isSuitableTarget(theOwnerAttacker, false);
        }
        
        return false;
    }
    
    @Override
    public void startExecuting()
    {
        taskOwner.setAttackTarget(theOwnerAttacker);
        super.startExecuting();
    }
}
