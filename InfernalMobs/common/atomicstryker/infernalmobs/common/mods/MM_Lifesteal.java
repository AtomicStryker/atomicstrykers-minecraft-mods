package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Lifesteal extends MobModifier
{
    public MM_Lifesteal(EntityLiving mob)
    {
        this.modName = "LifeSteal";
    }
    
    public MM_Lifesteal(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "LifeSteal";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        EntityLiving mob = (EntityLiving) source.getEntity();
        if (entity != null
        && mob.getHealth() < getActualMaxHealth(mob))
        {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth()+damage);
        }
        
        return super.onAttack(entity, source, damage);
    }
        
    @Override
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class[] disallowed = { EntityCreeper.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " the Vampire", " of Transfusion", " the Bloodsucker" };
    
}
