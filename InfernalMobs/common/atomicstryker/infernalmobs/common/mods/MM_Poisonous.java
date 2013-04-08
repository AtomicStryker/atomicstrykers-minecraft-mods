package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Poisonous extends MobModifier
{
    public MM_Poisonous(EntityLiving mob)
    {
        this.modName = "Poisonous";
    }
    
    public MM_Poisonous(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Poisonous";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
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
        
        return super.onHurt(mob, source, damage);
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
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " of Venom", " the deadly Chalice" };
    
}
