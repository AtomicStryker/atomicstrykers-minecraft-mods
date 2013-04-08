package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Rust extends MobModifier
{
    public MM_Rust(EntityLiving mob)
    {
        this.modName = "Rust";
    }
    
    public MM_Rust(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Rust";
        this.nextMod = prevMod;
    }
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityPlayer))
        {
            EntityPlayer p = (EntityPlayer)source.getEntity();
            if (p.inventory.getCurrentItem() != null)
            {
                p.inventory.getCurrentItem().damageItem(4, (EntityLiving) source.getEntity());
            }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public int onAttack(EntityLiving entity, DamageSource source, int damage)
    {
        if (entity != null
        && entity instanceof EntityPlayer)
        {
            ((EntityPlayer)entity).inventory.damageArmor(damage*3);
        }
        
        return super.onAttack(entity, source, damage);
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { " of Decay", " the Equipment Haunter" };
    
}
