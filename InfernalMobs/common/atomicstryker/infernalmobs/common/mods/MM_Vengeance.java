package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Vengeance extends MobModifier
{
    public MM_Vengeance(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Vengeance";
    }
    
    public MM_Vengeance(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Vengeance";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null)
        {
            source.getEntity().attackEntityFrom(DamageSource.causeMobDamage(mob), damage);
        }
        
        return super.onHurt(source, damage);
    }
}
