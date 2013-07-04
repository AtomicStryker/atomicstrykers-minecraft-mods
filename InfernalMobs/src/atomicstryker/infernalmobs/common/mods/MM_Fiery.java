package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Fiery extends MobModifier
{
    public MM_Fiery(EntityLivingBase mob)
    {
        this.modName = "Fiery";
    }
    
    public MM_Fiery(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Fiery";
        this.nextMod = prevMod;
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLivingBase))
        {
            ((EntityLivingBase)source.getEntity()).setFire(3);
        }
        
        mob.extinguish();
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
    {
        if (entity != null)
        {
            entity.setFire(3);
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " of Conflagration", " the Phoenix", " of Crispyness" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { " burning ", " toasting " };
    
}
