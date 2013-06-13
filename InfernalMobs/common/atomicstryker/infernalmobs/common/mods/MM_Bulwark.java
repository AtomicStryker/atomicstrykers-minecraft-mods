package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Bulwark extends MobModifier
{
    public MM_Bulwark(EntityLiving mob)
    {
        this.modName = "Bulwark";
    }
    
    public MM_Bulwark(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Bulwark";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        return super.onHurt(mob, source, Math.max(damage/2, 1));
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " of Turtling", " the Defender", " of effing Armor" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { " turtling ", " defensive ", " armoured " };
    
}
