package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Potion;
import net.minecraft.src.PotionEffect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Poisonous extends MobModifier
{
    public MM_Poisonous(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Poisonous";
    }
    
    public MM_Poisonous(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Poisonous";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityLiving))
        {
            EntityLiving ent = (EntityLiving)source.getEntity();
            if (!ent.isPotionActive(Potion.poison))
            {
                ent.addPotionEffect(new PotionEffect(Potion.poison.id, 120, 0));
            }
        }
        
        return super.onHurt(source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null
        && !entity.isPotionActive(Potion.poison))
        {
            entity.addPotionEffect(new PotionEffect(Potion.poison.id, 120, 0));
        }
        
        return super.onAttack(entity, source, damage);
    }
}
