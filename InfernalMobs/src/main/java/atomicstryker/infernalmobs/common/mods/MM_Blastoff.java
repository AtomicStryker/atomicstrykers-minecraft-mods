package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class MM_Blastoff extends MobModifier
{
    
    public MM_Blastoff()
    {
        super();
    }
    
    public MM_Blastoff(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Blastoff";
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public boolean onUpdate(EntityLivingBase mob)
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob, getMobTarget());
        }
        
        return super.onUpdate(mob);
    }

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        if (source.getTrueSource() != null
        && source.getTrueSource() instanceof EntityLivingBase)
        {
            tryAbility(mob, (EntityLivingBase) source.getTrueSource());
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse)
        {
            nextAbilityUse = time+coolDown;
            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
            
            if (target.world.isRemote || !(target instanceof EntityPlayerMP))
            {
                target.addVelocity(0, 1.1D, 0);
            }
            else
            {
                InfernalMobsCore.instance().sendVelocityPacket((EntityPlayerMP)target, 0f, 1.1f, 0f);
            }
        }
    }
    
    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Webber.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofMissionControl", "theNASA", "ofWEE" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "thumping", "trolling", "byebye" };
    
}
