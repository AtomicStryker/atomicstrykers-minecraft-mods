package atomicstryker.infernalmobs.common.mods;

import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Webber extends MobModifier
{
    public MM_Webber(EntityLiving mob)
    {
        this.mob = mob;
        this.modName = "Webber";
    }
    
    public MM_Webber(EntityLiving mob, MobModifier prevMod)
    {
        this.mob = mob;
        this.modName = "Webber";
        this.nextMod = prevMod;
    }
    
    private long lastAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public boolean onUpdate()
    {
        if (mob.getAttackTarget() != null
        && mob.getAttackTarget() instanceof EntityPlayer)
        {
            tryAbility(mob.getAttackTarget());
        }
        
        return super.onUpdate();
    }
    
    @Override
    public int onHurt(DamageSource source, int damage)
    {
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLiving)
        {
            tryAbility((EntityLiving) source.getEntity());
        }
        
        return super.onHurt(source, damage);
    }

    private void tryAbility(EntityLiving target)
    {
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
