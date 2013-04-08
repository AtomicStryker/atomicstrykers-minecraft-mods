package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Quicksand extends MobModifier
{
    public MM_Quicksand(EntityLiving mob)
    {
        this.modName = "Quicksand";
    }
    
    public MM_Quicksand(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Quicksand";
        this.nextMod = prevMod;
    }
    
    int ticker = 0;
    
    @Override
    public boolean onUpdate(EntityLiving mob)
    {
        if (getMobTarget() != null && ++ticker == 50)
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
    private static String[] suffix = { " of You-Cant-Run", " the slowing B****" };
    
}
