package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Sapper extends MobModifier
{
    
    public MM_Sapper()
    {
        super();
    }
    
    public MM_Sapper(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Sapper";
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLivingBase)
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(source.getEntity()))
        {
            EntityLivingBase ent = (EntityLivingBase)source.getEntity();
            if (!ent.isPotionActive(Potion.hunger))
            {
                ent.addPotionEffect(new PotionEffect(Potion.hunger.id, 120, 0));
            }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        if (entity != null
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(entity)
        && !entity.isPotionActive(Potion.poison))
        {
            entity.addPotionEffect(new PotionEffect(Potion.hunger.id, 120, 0));
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofHunger", "thePaleRider" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "hungering", "starving" };
    
}
