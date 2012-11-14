package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Bulwark extends MobModifier
{
    public MM_Bulwark(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Bulwark";
    }
    
    public MM_Bulwark(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Bulwark";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        int reduced = damage/2;
        if (reduced == 0 && damage != 0)
        {
            reduced = 1;
        }
        
        return super.onHurt(source, reduced);
    }
}
