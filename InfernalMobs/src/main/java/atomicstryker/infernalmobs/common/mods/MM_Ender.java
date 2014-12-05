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

public class MM_Ender extends MobModifier
{
    public MM_Ender(EntityLivingBase mob)
    {
        this.modName = "Ender";
    }

    public MM_Ender(EntityLivingBase mob, MobModifier prevMod)
    {
        this.modName = "Ender";
        this.nextMod = prevMod;
    }

    private long nextAbilityUse = 0L;
    private final static long coolDown = 15000L;

    @Override
    public float onHurt(EntityLivingBase mob, DamageSource source, float damage)
    {
        long time = System.currentTimeMillis();
        if (time > nextAbilityUse && source.getEntity() != null && source.getEntity() != mob && teleportToEntity(mob, source.getEntity()))
        {
            nextAbilityUse = time + coolDown;
            source.getEntity().attackEntityFrom(DamageSource.causeMobDamage(mob), InfernalMobsCore.instance().getLimitedDamage(damage));

            return super.onHurt(mob, source, 0);
        }

        return super.onHurt(mob, source, damage);
    }

    private boolean teleportToEntity(EntityLivingBase mob, Entity par1Entity)
    {
        Vec3 vector =
                new Vec3(mob.posX - par1Entity.posX, mob.getBoundingBox().minY + (double) (mob.height / 2.0F) - par1Entity.posY
                        + (double) par1Entity.getEyeHeight(), mob.posZ - par1Entity.posZ);
        vector = vector.normalize();
        double telDist = 16.0D;
        double destX = mob.posX + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.xCoord * telDist;
        double destY = mob.posY + (double) (mob.worldObj.rand.nextInt(16) - 8) - vector.yCoord * telDist;
        double destZ = mob.posZ + (mob.worldObj.rand.nextDouble() - 0.5D) * 8.0D - vector.zCoord * telDist;
        return teleportTo(mob, destX, destY, destZ);
    }

    private boolean teleportTo(EntityLivingBase mob, double destX, double destY, double destZ)
    {
        double oldX = mob.posX;
        double oldY = mob.posY;
        double oldZ = mob.posZ;
        boolean success = false;
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
                --mob.posY;
                --y;
            }
        }

        if (hitGround)
        {
            mob.setPosition(mob.posX, mob.posY, mob.posZ);

            if (mob.worldObj.getCollidingBoundingBoxes(mob, mob.getBoundingBox()).isEmpty() && !mob.worldObj.isAnyLiquid(mob.getBoundingBox()))
            {
                success = true;
            }
        }
        else
        {
            return false;
        }

        if (!success)
        {
            mob.setPosition(oldX, oldY, oldZ);
            return false;
        }
        else
        {
            short range = 128;
            for (int i = 0; i < range; ++i)
            {
                double var19 = (double) i / ((double) range - 1.0D);
                float var21 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
                float var22 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
                float var23 = (mob.worldObj.rand.nextFloat() - 0.5F) * 0.2F;
                double var24 = oldX + (mob.posX - oldX) * var19 + (mob.worldObj.rand.nextDouble() - 0.5D) * (double) mob.width * 2.0D;
                double var26 = oldY + (mob.posY - oldY) * var19 + mob.worldObj.rand.nextDouble() * (double) mob.height;
                double var28 = oldZ + (mob.posZ - oldZ) * var19 + (mob.worldObj.rand.nextDouble() - 0.5D) * (double) mob.width * 2.0D;
                mob.worldObj.spawnParticle(EnumParticleTypes.PORTAL, var24, var26, var28, (double) var21, (double) var22, (double) var23);
            }

            mob.worldObj.playSoundEffect(oldX, oldY, oldZ, "mob.endermen.portal", 1.0F, 1.0F);
            mob.worldObj.playSoundAtEntity(mob, "mob.endermen.portal", 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    protected String[] getModNameSuffix()
    {
        return suffix;
    }

    private static String[] suffix = { "theEnderborn", "theTrickster" };

    @Override
    protected String[] getModNamePrefix()
    {
        return prefix;
    }

    private static String[] prefix = { "enderborn", "tricky" };

}
