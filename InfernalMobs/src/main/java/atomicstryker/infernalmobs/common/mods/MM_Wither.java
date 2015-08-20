package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Wither extends MobModifier
{
    
    public MM_Wither()
    {
        super();
    }
    
    public MM_Wither(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Wither";
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLivingBase)
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getEntity())
        && !(source instanceof EntityDamageSourceIndirect))
        {
            ((EntityLivingBase)source.getEntity()).addPotionEffect(new PotionEffect(Potion.wither.id, 120, 0));
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        if (entity != null
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity))
        {
            entity.addPotionEffect(new PotionEffect(Potion.wither.id, 120, 0));
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofDarkSkulls", "Doomskull" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "withering" };
    
}
