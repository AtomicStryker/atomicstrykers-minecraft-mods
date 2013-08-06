package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Lifesteal extends MobModifier
{
    public MM_Lifesteal(EntityLivingBase mob)
    {
        this.modName = "LifeSteal";
    }
    
    public MM_Lifesteal(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "LifeSteal";
        this.nextMod = prevMod;
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        EntityLivingBase mob = (EntityLivingBase) source.getEntity();
        if (entity != null
        && mob.func_110143_aJ() < getActualMaxHealth(mob))
        {
            InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.func_110143_aJ()+damage);
        }
        
        return super.onAttack(entity, source, damage);
    }
        
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class<?>[] disallowed = { EntityCreeper.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "theVampire", "ofTransfusion", "theBloodsucker" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "vampiric", "transfusing", "bloodsucking" };
    
}
