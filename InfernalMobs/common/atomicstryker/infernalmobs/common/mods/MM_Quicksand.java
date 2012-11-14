package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Quicksand extends MobModifier
{
    public MM_Quicksand(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Quicksand";
    }
    
    public MM_Quicksand(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Quicksand";
        this.nextMod = prevMod;
    }
    
    int ticker = 0;
    
    @Override
    public boolean onUpdate()
    {
        if (mob.getAttackTarget() != null && ++ticker == 50)
        {
            ticker = 0;
            mob.getAttackTarget().addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 45, 0));
        }
        
        return super.onUpdate();
    }
}
