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
        this.mob = mob;
        this.modName = "Cloaking";
    }
    
    public MM_Cloaking(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Cloaking";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 10000L;
    
    @Override
    public boolean onUpdate()
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility();
        }
        
        return super.onUpdate();
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLiving)
        {
            tryAbility();
        }
        
        return super.onHurt(source, damage);
    }

    private void tryAbility()
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
