package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

public class MM_Cloaking extends MobModifier
{
    
    public MM_Cloaking()
    {
        super();
    }
    
    public MM_Cloaking(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Cloaking";
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 10000L;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob);
        }
        
        return super.onUpdate(mob);
    }
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLivingBase)
        {
            tryAbility(mob);
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            mob.addPotionEffect(new PotionEffect(Potion.invisibility.id, 200));
        }
    }
    
    @Override
    public Class<?>[] getBlackListMobClasses()
    {
        return disallowed;
    }
    private static Class<?>[] disallowed = { EntitySpider.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofStalking", "theUnseen", "thePredator" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "stalking", "unseen", "hunting" };
    
}
