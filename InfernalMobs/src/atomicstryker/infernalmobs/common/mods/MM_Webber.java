package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Webber extends MobModifier
{
    public MM_Webber(EntityLivingBase mob)
    {
        this.modName = "Webber";
    }
    
    public MM_Webber(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Webber";
        this.nextMod = prevMod;
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
        if (source.getEntity() != null
        && source.getEntity() instanceof EntityLivingBase)
        {
            tryAbility(mob, (EntityLivingBase) source.getEntity());
        }
        
        return super.onHurt(mob, source, damage);
    }

    private void tryAbility(EntityLivingBase mob, EntityLivingBase target)
    {
        if (target == null || !mob.canEntityBeSeen(target))
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
            target.worldObj.setBlock(x, y+offset, z, Block.web.blockID, 0, 3);
            mob.worldObj.playSoundAtEntity(mob, "mob.spider", 1.0F, (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F + 1.0F);
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
    private static String[] suffix = { " of Traps", " the Mutated", " the Spider" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { " ensnaring ", " webbing " };
    
}
