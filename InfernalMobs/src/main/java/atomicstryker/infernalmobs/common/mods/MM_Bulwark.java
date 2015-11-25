package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class MM_Bulwark extends MobModifier
{
    
    public MM_Bulwark()
    {
        super();
    }
    
    public MM_Bulwark(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Bulwark";
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        return super.onHurt(mob, source, Math.max(damage/2, 1));
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofTurtling", "theDefender", "ofeffingArmor" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "turtling", "defensive", "armoured" };
    
}
