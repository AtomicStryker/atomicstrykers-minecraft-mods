package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Fiery extends MobModifier
{
    public MM_Fiery(EntityLiving mob)
    {
        this.modName = "Fiery";
    }
    
    public MM_Fiery(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Fiery";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLiving))
        {
            ((EntityLiving)source.getEntity()).setFire(3);
        }
        
        mob.extinguish();
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null)
        {
            entity.setFire(3);
        }
        
        return super.onAttack(entity, source, damage);
    }
}
