package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Webber extends MobModifier
{
    public MM_Webber(EntityLiving mob)
    {
        this.modName = "Webber";
    }
    
    public MM_Webber(EntityLiving mob, MobModifier prevMod)
    {
        this.modName = "Webber";
        this.nextMod = prevMod;
    }
    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
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
    
    @Override
    public int onHurt(EntityLiving mob, DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLiving)
        {
            tryAbility(mob, (EntityLiving) source.getEntity());
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLiving mob, EntityLiving target)
    {
        if (target == null)
        {
            return;
        }
        
        int x = MathHelper.floor_double(target.posX);
        int y = MathHelper.floor_double(target.posY);
        int z = MathHelper.floor_double(target.posZ);
        
        long time = System.currentTimeMillis();
        if (time > lastAbilityUse+coolDown)
        {
            int offset;
            if (target.worldObj.getBlockId(x, y-1, z) == 0)
            {
                offset = -1;
            }
            else if (target.worldObj.getBlockId(x, y, z) == 0)
            {
                offset = 0;
            }
            else
            {
                return;
            }
            
            lastAbilityUse = time;
            target.worldObj.setBlockWithNotify(x, y+offset, z, Block.web.blockID);
            mob.worldObj.playSoundAtEntity(mob, "mob.spider", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }
    
    @Override
    public Class[] getModsNotToMixWith()
    {
        return modBans;
    }
    private static Class[] modBans = { MM_Gravity.class, MM_Blastoff.class };
}
