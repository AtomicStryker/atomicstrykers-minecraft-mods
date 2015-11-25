package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;

public class MM_Regen extends MobModifier
{
    
    public MM_Regen()
    {
        super();
    }
    
    public MM_Regen(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Regen";
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 500L;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (mob.getHealth() < getActualMaxHealth(mob))
        {
            long time = System.currentTimeMillis();
            if (time > nextAbilityUse)
            {
                nextAbilityUse = time+coolDown;
                InfernalMobsCore.instance().setEntityHealthPastMax(mob, mob.getHealth()+1);
            }
        }
        return super.onUpdate(mob);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofWTFIMBA", "theCancerous", "ofFirstAid" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "regenerating", "healing", "nighunkillable" };
}
