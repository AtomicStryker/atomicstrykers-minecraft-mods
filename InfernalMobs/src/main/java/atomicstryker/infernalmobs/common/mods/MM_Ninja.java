package atomicstryker.infernalmobs.common.mods;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;

public class MM_Ninja extends MobModifier
{
    public MM_Ninja(EntityLivingBase mob)
    {
        this.modName = "Ninja";
    }
    
    public MM_Ninja(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Ninja";
        this.nextMod = prevMod;
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
        && source.getEntity() != null
        && source.getEntity() != mob
        && teleportToEntity(mob, source.getEntity()))
        {
            nextAbilityUse = time+coolDown;
            source.getEntity().attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));
            return super.onHurt(mob, source, 0);
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    private boolean teleportToEntity(EntityLivingBase mob, Entity par1Entity)
    {
        Vec3 vector = new Vec3(mob.posX - par1Entity.posX, mob.getBoundingBox().minY + (double)(mob.height / 2.0F) - par1Entity.posY + (double)par1Entity.getEyeHeight(), mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 8.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.xCoord * telDist;
        double destY = mob.posY + (double)(mob.worldObj.rand.nextInt(16) - 4) - vector.yCoord * telDist;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 4.0D - vector.zCoord * telDist;
        return teleportTo(mob, destX, destY, destZ);
    }
    
    private boolean teleportTo(EntityLivingBase mob, double destX, double destY, double destZ)
    {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        mob.posX = destX;
        mob.posY = destY;
        mob.posZ = destZ;
        int x = MathHelper.floor_double(mob.posX);
        int y = MathHelper.floor_double(mob.posY);
        int z = MathHelper.floor_double(mob.posZ);
        Block blockID;

        boolean hitGround = false;
        while (!hitGround && y < 96)
        {
            blockID = mob.worldObj.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
            if (blockID.getMaterial().blocksMovement())
            {
                hitGround = true;
            }
            else
            {
                ++mob.posY;
                ++y;
            }
        }

        if (hitGround)
        {
            mob.setPosition(mob.posX, mob.posY, mob.posZ);
            
            mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "random.explode", 2.0F, (1.0F + (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
            mob.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, oldX, oldY, oldZ, 0D, 0D, 0D);
        }
        else
        {
            return false;
        }
        return true;
    }
    
    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }
    private static String[] suffix = { "theZenMaster", "ofEquilibrium", "ofInnerPeace" };
    
    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }
    private static String[] prefix = { "totallyzen", "innerlypeaceful", "Ronin" };
    
}
