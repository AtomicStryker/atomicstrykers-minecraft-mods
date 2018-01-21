package atomicstryker.battletowers.common;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AS_EntityGolemFireball extends Entity
{
    private boolean wasDeflected;
    public EntityLiving shooterEntity;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    public AS_EntityGolemFireball(World world)
    {
        super(world);
        setSize(0.3F, 0.3F);
        wasDeflected = false;
    }

    @Override
    protected void entityInit()
    {
    }

    public AS_EntityGolemFireball(World world, EntityLiving entityliving, double diffX, double diffY, double diffZ)
    {
        this(world);

        shooterEntity = entityliving;
        motionX = motionY = motionZ = 0.0D;
        diffX += rand.nextGaussian() * 0.4D;
        diffY += rand.nextGaussian() * 0.4D;
        diffZ += rand.nextGaussian() * 0.4D;
        double targetDistance = MathHelper.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        accelerationX = (diffX / targetDistance) * 0.1D;
        accelerationY = (diffY / targetDistance) * 0.1D;
        accelerationZ = (diffZ / targetDistance) * 0.1D;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        this.setFire(1);

        Vec3d curVec = new Vec3d(posX, posY, posZ);
        Vec3d nextVec = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
        RayTraceResult collisionPosition = world.rayTraceBlocks(curVec, nextVec);
        curVec = new Vec3d(posX, posY, posZ);
        nextVec = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
        if (collisionPosition != null)
        {
            nextVec = new Vec3d(collisionPosition.hitVec.x, collisionPosition.hitVec.y, collisionPosition.hitVec.z);
        }
        Entity hitEntity = null;
        List list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().offset(motionX, motionY, motionZ).grow(1.0D, 1.0D, 1.0D));
        double minDist = 0.0D;
        for (Object aList : list)
        {
            Entity ent = (Entity) aList;
            if (!ent.canBeCollidedWith() || (ent == shooterEntity && ticksExisted < 25 && !wasDeflected))
            {
                continue;
            }
            AxisAlignedBB axisalignedbb = ent.getEntityBoundingBox().grow(0.3F, 0.3F, 0.3F);
            RayTraceResult entCollision = axisalignedbb.calculateIntercept(curVec, nextVec);
            if (entCollision == null)
            {
                continue;
            }
            double distToCollision = curVec.distanceTo(entCollision.hitVec);
            if (distToCollision < minDist || minDist == 0.0D)
            {
                hitEntity = ent;
                minDist = distToCollision;
            }
        }

        if (hitEntity != null)
        {
            collisionPosition = new RayTraceResult(hitEntity);
        }
        if (collisionPosition != null)
        {
            if (!world.isRemote)
            {
                if (collisionPosition.entityHit != null)
                {
                    // this causes the fall through floor bug?!?!?!
                    // collisionPosition.entityHit.attackEntityFrom(DamageSource.causeMobDamage(shooterEntity),
                    // 0);
                }
                world.newExplosion(null, posX, posY, posZ, 1.0F, true, true);
            }
            setDead();
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) ((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        for (rotationPitch = (float) ((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F)
        {
        }
        for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F)
        {
        }
        for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F)
        {
        }
        for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F)
        {
        }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float f1 = 0.95F;
        if (isInWater())
        {
            for (int k = 0; k < 4; k++)
            {
                float f3 = 0.25F;
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double) f3, posY - motionY * (double) f3, posZ - motionZ * (double) f3, motionX, motionY, motionZ);
            }

            f1 = 0.8F;
        }
        motionX += accelerationX;
        motionY += accelerationY;
        motionZ += accelerationZ;
        motionX *= f1;
        motionY *= f1;
        motionZ *= f1;
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
        setPosition(posX, posY, posZ);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public float getCollisionBorderSize()
    {
        return 1.0F;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damage, float i)
    {
        markVelocityChanged();
        Entity entity = damage.getTrueSource();
        if (entity != null)
        {
            Vec3d vec3d = entity.getLookVec();
            if (vec3d != null)
            {
                motionX = vec3d.x;
                motionY = vec3d.y;
                motionZ = vec3d.z;
                accelerationX = motionX * 0.1D;
                accelerationY = motionY * 0.1D;
                accelerationZ = motionZ * 0.1D;

                wasDeflected = true;
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
    }

}
