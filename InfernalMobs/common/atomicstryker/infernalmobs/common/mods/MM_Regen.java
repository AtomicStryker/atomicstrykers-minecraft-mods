package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Regen extends MobModifier
{
    public MM_Regen(EntityLiving mob)
    {
        this.modName = "Regen";
    }
    
    public MM_Regen(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Regen";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 500L;
    
    @Override
    public boolean onUpdate(EntityLiving mob)
    {
        if (mob.getHealth() < getActualMaxHealth(mob))
        {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse)
            {
                nextAbilityUse = time+coolDown;
                InfernalMobsCore.setEntityHealthPastMax(mob, mob.getHealth()+1);
            }
        }
        return super.onUpdate(mob);
    }
}
