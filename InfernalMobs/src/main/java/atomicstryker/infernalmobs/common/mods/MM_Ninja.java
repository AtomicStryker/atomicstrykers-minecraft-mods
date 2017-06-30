package atomicstryker.infernalmobs.common.mods;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MM_Ninja extends MobModifier
{
    
    public MM_Ninja()
    {
        super();
    }
    
    public MM_Ninja(MobModifier next)
    {
        super(next);
    }

    @Override
    public String getModName()
    {
        return "Ninja";
    }
    
    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;
    
    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse
        && source.getTrueSource() != null
        && source.getTrueSource() != mob
        && !InfernalMobsCore.instance().isInfiniteLoop(mob, source.getTrueSource())
        && teleportToEntity(mob, source.getTrueSource()))
        {
            nextAbilityUse = time+coolDown;
            source.getTrueSource().attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));
            return super.onHurt(mob, source, 0);
        }
        
        return super.onHurt(mob, source, damage);
    }
    
    private boolean teleportToEntity(EntityLivingBase mob, Entity par1Entity)
    {
        Vec3d vector = new Vec3d(mob.posX - par1Entity.posX, mob.getEntityBoundingBox().minY + (double)(mob.height / 2.0F) - par1Entity.posY + (double)par1Entity.getEyeHeight(), mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 8.0D;
        double destX = mob.posX + (mob.world.rand.nextDouble() - 0.5D) * 4.0D - vector.x * telDist;
        double destY = mob.posY + (double)(mob.world.rand.nextInt(16) - 4) - vector.y * telDist;
        double destZ = mob.posZ + (mob.world.rand.nextDouble() - 0.5D) * 4.0D - vector.z * telDist;
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
        int x = MathHelper.floor(mob.posX);
        int y = MathHelper.floor(mob.posY);
        int z = MathHelper.floor(mob.posZ);

        boolean hitGround = false;
        while (!hitGround && y < 96 && y > 0)
        {
            IBlockState bs = mob.world.getBlockState(new BlockPos(x, y - 1, z));
            if (bs.getMaterial().blocksMovement())
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
            
            mob.world.playSound(null, new BlockPos(mob), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F + mob.getRNG().nextFloat(), mob.getRNG().nextFloat() * 0.7F + 0.3F);
            mob.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, oldX, oldY, oldZ, 0D, 0D, 0D);
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
