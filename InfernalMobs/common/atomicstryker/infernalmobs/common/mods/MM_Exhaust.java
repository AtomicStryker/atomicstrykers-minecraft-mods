package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Exhaust extends MobModifier
{
    public MM_Exhaust(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Exhaust";
    }
    
    public MM_Exhaust(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Exhaust";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityPlayer))
        {
            ((EntityPlayer)source.getEntity()).addExhaustion(1F);
        }
        
        return super.onHurt(source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null
        && entity instanceof EntityPlayer)
        {
            ((EntityPlayer)entity).addExhaustion(1F);
        }
        
        return super.onAttack(entity, source, damage);
    }
}
