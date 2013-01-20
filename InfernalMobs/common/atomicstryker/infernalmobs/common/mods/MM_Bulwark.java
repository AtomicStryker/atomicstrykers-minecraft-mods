package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
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
        return super.onHurt(source, Math.max(damage/2, 1));
    }
}
