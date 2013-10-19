package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Rust extends MobModifier
{
    public MM_Rust(EntityLivingBase mob)
    {
        this.modName = "Rust";
    }
    
    public MM_Rust(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Rust";
        this.nextMod = prevMod;
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityPlayer)
        && !(source instanceof EntityDamageSourceIndirect))
        {
            EntityPlayer p = (EntityPlayer)source.getEntity();
            if (p.inventory.getCurrentItem() != null)
            {
                p.inventory.getCurrentItem().damageItem(4, (EntityLivingBase) source.getEntity());
            }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    @Override
    public float onAttack(EntityLivingBase entity, DamageSource source, float damage)
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
    private static String[] suffix = { "ofDecay", "theEquipmentHaunter" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "rusting", "decaying" };
    
}
