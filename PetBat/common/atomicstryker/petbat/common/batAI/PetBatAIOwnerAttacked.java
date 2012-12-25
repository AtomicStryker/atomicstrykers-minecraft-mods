package atomicstryker.petbat.common.batAI;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITarget;
import atomicstryker.petbat.common.EntityPetBat;

public class PetBatAIOwnerAttacked extends EntityAITarget
{
    private EntityPetBat batEnt;
    private EntityLiving theOwnerAttacker;
    
    public PetBatAIOwnerAttacked(EntityPetBat bat)
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
