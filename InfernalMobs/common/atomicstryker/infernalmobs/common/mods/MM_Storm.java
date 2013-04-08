package atomicstryker.infernalmobs.common.mods;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Storm extends MobModifier
{
    public MM_Storm(EntityLiving mob)
    {
        this.modName = "Storm";
    }
    
    public MM_Storm(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Storm";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;
    private final static float MIN_DISTANCE = 3F;
    
    @Override
    public boolean onUpdate(EntityLiving mob)
    {
        if (getMobTarget() != null
        && getMobTarget() instanceof EntityPlayer)
        {
            tryAbility(mob, getMobTarget());
        }
        
        return super.onUpdate(mob);
    }

    private void tryAbility(EntityLiving mob, EntityLiving target)
    {
        if (target == null)
        {
            return;
        }
        
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
        && mob.getDistanceToEntity(target) > MIN_DISTANCE
        && target.worldObj.canBlockSeeTheSky(MathHelper.floor_double(target.posX), MathHelper.floor_double(target.posY), MathHelper.floor_double(target.posZ)))
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
    private static String[] suffix = { " of Lightning", " the Raiden" };
    
}
