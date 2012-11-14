package atomicstryker.infernalmobs.common.mods;

import java.lang.reflect.Field;

import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Regen extends MobModifier
{
    public MM_Regen(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Regen";
    }
    
    public MM_Regen(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Regen";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 500L;
    
    @Override
    public boolean onUpdate()
    {
        if (mob.getHealth() < (mob.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER))
        {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse)
            {
                nextAbilityUse = time+coolDown;
                InfernalMobsCore.setEntityHealthPastMax(mob, mob.getHealth()+1);
            }
        }
        return super.onUpdate();
    }
}
