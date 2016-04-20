package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class MM_Sticky extends MobModifier
{
    
    public MM_Sticky()
    {
        super();
    }
    
    public MM_Sticky(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Sticky";
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && (source.getEntity() instanceof EntityPlayer))
        {
            EntityPlayer p = (EntityPlayer)source.getEntity();
            ItemStack weapon = p.inventory.getStackInSlot(p.inventory.currentItem);
            if (weapon != null)
            {
                long time = System.currentTimeMillis();
                if (time > nextAbilityUse
                && source.getEntity() != null
                && !(source instanceof EntityDamageSourceIndirect))
                {
                    nextAbilityUse = time+coolDown;
                    EntityItem drop = p.dropItem(p.inventory.decrStackSize(p.inventory.currentItem, 1), false);
                    if (drop != null)
                    {
                        drop.setPickupDelay(50);
                        mob.worldObj.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
                    }
                }
            }
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    private Class<?>[] disallowed = { EntityCreeper.class };
    
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofSnagging", "theQuickFingered", "ofPettyTheft", "yoink" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "thieving", "snagging", "quickfingered" };
    
}
