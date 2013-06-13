package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Vengeance extends MobModifier
{
    public MM_Vengeance(EntityLiving mob)
    {
        this.modName = "Vengeance";
    }
    
    public MM_Vengeance(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Vengeance";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && source.getEntity() != mob)
        {
            source.getEntity().attackEntityFrom(DamageSource.causeMobDamage(mob), Math.max(damage/2, 1));
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " of Retribution", " the Thorned", " of Striking Back" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { " thorned ", " thorny ", " spiky " };
    
}
