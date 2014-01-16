package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Quicksand extends MobModifier
{
    public MM_Quicksand(EntityLivingBase mob)
    {
        this.modName = "Quicksand";
    }
    
    public MM_Quicksand(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Quicksand";
        this.nextMod = prevMod;
    }
    
    int ticker = 0;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != null
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
