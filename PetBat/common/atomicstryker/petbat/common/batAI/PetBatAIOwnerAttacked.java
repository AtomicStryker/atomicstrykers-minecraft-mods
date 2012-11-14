package atomicstryker.petbat.common.batAI;

import atomicstryker.petbat.common.EntityPetBat;
import net.minecraft.src.EntityAITarget;
import net.minecraft.src.EntityLiving;

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
            return isSuitableTarget(this.theOwnerAttacker, false);
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
