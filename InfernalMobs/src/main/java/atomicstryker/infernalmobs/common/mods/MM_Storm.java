package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MM_Storm extends MobModifier
{
    
    public MM_Storm()
    {
        super();
    }
    
    public MM_Storm(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Storm";
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;
    private final static float MIN_DISTANCE = 3F;
    
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

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || target.getRidingEntity() != null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
        && mob.getDistanceToEntity(target) > MIN_DISTANCE
        && target.world.canBlockSeeSky(new BlockPos(MathHelper.floor(target.posX), MathHelper.floor(target.posY), MathHelper.floor(target.posZ))))
        {
            nextAbilityUse = time+coolDown;
            mob.world.addWeatherEffect(new EntityLightningBolt(mob.world, target.posX, target.posY-1, target.posZ, false));
        }
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "ofLightning", "theRaiden" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "striking", "thundering", "electrified" };
    
}
