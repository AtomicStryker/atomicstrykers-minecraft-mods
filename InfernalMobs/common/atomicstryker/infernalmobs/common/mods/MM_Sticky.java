package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Sticky extends MobModifier
{
    public MM_Sticky(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Sticky";
    }
    
    public MM_Sticky(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Sticky";
        this.nextMod = prevMod;
    }
    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityPlayer))
        {
            EntityPlayer p = (EntityPlayer)source.getEntity();
            ItemStack weapon = p.inventory.getStackInSlot(p.inventory.currentItem);
            if (weapon != null)
            {
                long time = System.currentTimeMillis();
                if (time > lastAbilityUse+coolDown
                && source.getEntity() != null)
                {
                    lastAbilityUse = time;
                    EntityItem drop = p.dropPlayerItemWithRandomChoice(p.inventory.decrStackSize(p.inventory.currentItem, 1), false);
                    if (drop != null)
                    {
                        drop.delayBeforeCanPickup = 50;
                        p.worldObj.playSoundAtEntity(mob, "mob.slimeattack", 1.0F, (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
                    }
                }
            }
        }
        
        return super.onHurt(source, damage);
    }
    
    private Class[] disallowed = { EntityCreeper.class };
    
    @Override
    public Class[] getBlackListMobClasses()
    {
        return disallowed;
    }
}
