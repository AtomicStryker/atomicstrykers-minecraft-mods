package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

public class MM_Poisonous extends MobModifier
{
    
    public MM_Poisonous()
    {
        super();
    }
    
    public MM_Poisonous(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Poisonous";
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getTrueSource() != null
        && (source.getTrueSource() instanceof EntityLivingBase)
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getTrueSource()))
        {
            EntityLivingBase ent = (EntityLivingBase)source.getTrueSource();
            if (!ent.isPotionActive(MobEffects.POISON)
            && !(source instanceof EntityDamageSourceIndirect))
            {
                ent.addPotionEffect(new PotionEffect(MobEffects.POISON, 120, 0));
            }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        if (entity != null
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity)
        && !entity.isPotionActive(MobEffects.POISON))
        {
            entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 120, 0));
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofVenom", "thedeadlyChalice" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "poisonous", "stinging", "despoiling" };
    
}
