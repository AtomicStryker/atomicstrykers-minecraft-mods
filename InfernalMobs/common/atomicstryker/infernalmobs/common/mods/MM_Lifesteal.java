package atomicstryker.infernalmobs.common.mods;

import java.lang.reflect.Field;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Lifesteal extends MobModifier
{
    public MM_Lifesteal(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "LifeSteal";
    }
    
    public MM_Lifesteal(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "LifeSteal";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null
        && mob.getHealth() < (mob.getMaxHealth()*InfernalMobsCore.RARE_MOB_HEALTH_MODIFIER))
        {
            InfernalMobsCore.setEntityHealthPastMax(mob, mob.getHealth()+damage);
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    public Class[] getWhiteListMobClasses()
    {
        return allowed;
    }
    private static Class[] allowed = { EntityMob.class, EntityWolf.class };
    
    @Override
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class[] disallowed = { EntityCreeper.class };
}
