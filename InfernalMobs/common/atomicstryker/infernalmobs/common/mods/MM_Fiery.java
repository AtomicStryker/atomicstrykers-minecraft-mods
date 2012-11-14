package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Fiery extends MobModifier
{
    public MM_Fiery(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Fiery";
    }
    
    public MM_Fiery(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Fiery";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLiving))
        {
            ((EntityLiving)source.getEntity()).setFire(3);
        }
        
        mob.extinguish();
        return super.onHurt(source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null)
        {
            entity.setFire(3);
        }
        
        return super.onAttack(entity, source, damage);
    }
}
