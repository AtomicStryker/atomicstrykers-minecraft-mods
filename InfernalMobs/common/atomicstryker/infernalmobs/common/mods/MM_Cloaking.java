package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Cloaking extends MobModifier
{
    public MM_Cloaking(EntityLiving mob)
    {
        this.modName = "Cloaking";
    }
    
    public MM_Cloaking(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Cloaking";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 10000L;
    
    @Override
    public boolean onUpdate(EntityLiving mob)
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob);
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLiving)
        {
            tryAbility(mob);
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLiving mob)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            mob.addPotionEffect(new PotionEffect(Potion.invisibility.id, 200));
        }
    }
    
    @Override
    public Class[] getBlackListMobClasses()
    {
        Class[] r = { EntitySpider.class };
        return r;
    }
    
}
