package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class MM_Quicksand extends MobModifier
{
    
    public MM_Quicksand()
    {
        super();
    }
    
    public MM_Quicksand(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Quicksand";
    }
    
    int ticker = 0;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != null
        && InfernalMobsCore.instance().getIsEntityAllowedTarget(getMobTarget())
        && mob.canEntityBeSeen(getMobTarget())
        && ++ticker == 50)
        {
            ticker = 0;
            getMobTarget().addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 45, 0));
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofYouCantRun", "theSlowingB" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "slowing", "Quicksand" };
    
}
