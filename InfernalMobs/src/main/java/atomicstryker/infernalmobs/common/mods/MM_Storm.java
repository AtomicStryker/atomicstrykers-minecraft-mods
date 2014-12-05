package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Storm extends MobModifier
{
    public MM_Storm(EntityLivingBase mob)
    {
        this.modName = "Storm";
    }
    
    public MM_Storm(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Storm";
        this.nextMod = prevMod;
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
        if (target == null || target.ridingEntity != null || !mob.canEntityBeSeen(target))
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
        && mob.getDistanceToEntity(target) > MIN_DISTANCE
        && target.worldObj.canBlockSeeSky(new BlockPos(MathHelper.floor_double(target.posX), MathHelper.floor_double(target.posY), MathHelper.floor_double(target.posZ))))
        {
            nextAbilityUse = time+coolDown;
            mob.worldObj.addWeatherEffect(new EntityLightningBolt(mob.worldObj, target.posX, target.posY-1, target.posZ));
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
