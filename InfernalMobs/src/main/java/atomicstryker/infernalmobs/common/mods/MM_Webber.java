package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MM_Webber extends MobModifier
{
    
    public MM_Webber()
    {
        super();
    }
    
    public MM_Webber(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Webber";
    }
    
    private long lastAbilityUse = 0L;
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
        
        int x = MathHelper.floor(target.posX);
        int y = MathHelper.floor(target.posY);
        int z = MathHelper.floor(target.posZ);
        
        long time = System.currentTimeMillis();
        if (time > lastAbilityUse+coolDown)
        {
            int offset;
            if (target.world.getBlockState(new BlockPos(x, y-1, z)).getBlock() == Blocks.AIR)
            {
                offset = -1;
            }
            else if (target.world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR)
            {
                offset = 0;
            }
            else
            {
                return;
            }
            
            lastAbilityUse = time;
            target.world.setBlockState(new BlockPos(x,  y+offset,  z),  Blocks.WEB.getDefaultState());
            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
        }
    }
    
    @Override
    public Class<?>[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class<?>[] modBans = { MM_Gravity.class, MM_Blastoff.class };
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofTraps", "theMutated", "theSpider" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "ensnaring", "webbing" };
    
}
