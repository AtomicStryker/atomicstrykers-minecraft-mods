package atomicstryker.battletowers.common;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
        double targetDistance = MathHelper.sqrt_double(diffX * diffX + diffY * diffY + diffZ * diffZ);
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
        
        Vec3 curVec = new Vec3(posX, posY, posZ);
        Vec3 nextVec = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition collisionPosition = worldObj.rayTraceBlocks(curVec, nextVec);
        curVec = new Vec3(posX, posY, posZ);
        nextVec = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        if(collisionPosition != null)
        {
            nextVec = new Vec3(collisionPosition.hitVec.xCoord, collisionPosition.hitVec.yCoord, collisionPosition.hitVec.zCoord);
        }
        Entity hitEntity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double minDist = 0.0D;
        for(int index = 0; index < list.size(); index++)
        {
            Entity ent = (Entity)list.get(index);
            if(!ent.canBeCollidedWith() || (ent == shooterEntity && ticksExisted < 25 && !wasDeflected))
            {
                continue;
            }
            AxisAlignedBB axisalignedbb = ent.getBoundingBox().expand( 0.3F,  0.3F,  0.3F);
            MovingObjectPosition entCollision = axisalignedbb.calculateIntercept(curVec, nextVec);
            if(entCollision == null)
            {
                continue;
            }
            double distToCollision = curVec.distanceTo(entCollision.hitVec);
            if(distToCollision < minDist || minDist == 0.0D)
            {
                hitEntity = ent;
                minDist = distToCollision;
            }
        }

        if(hitEntity != null)
        {
            collisionPosition = new MovingObjectPosition(hitEntity);
        }
        if(collisionPosition != null)
        {
            if(!worldObj.isRemote)
            {
                if(collisionPosition.entityHit != null)
                {
                    if(!collisionPosition.entityHit.attackEntityFrom(DamageSource.causeMobDamage(shooterEntity), 0));
                }
                worldObj.newExplosion(null, posX, posY, posZ, 1.0F, true, true);
            }
            setDead();
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        for(rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
        for(; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }
        for(; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
        for(; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float f1 = 0.95F;
        if(isInWater())
        {
            for(int k = 0; k < 4; k++)
            {
                float f3 = 0.25F;
                worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * (double)f3, posY - motionY * (double)f3, posZ - motionZ * (double)f3, motionX, motionY, motionZ);
            }

            f1 = 0.8F;
        }
        motionX += accelerationX;
        motionY += accelerationY;
        motionZ += accelerationZ;
        motionX *= f1;
        motionY *= f1;
        motionZ *= f1;
        worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.5D, posZ, 0.0D, 0.0D, 0.0D);
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
        setBeenAttacked();
        Entity entity = damage.getEntity();
        if(entity != null)
        {
            Vec3 vec3d = entity.getLookVec();
            if(vec3d != null)
            {
                motionX = vec3d.xCoord;
                motionY = vec3d.yCoord;
                motionZ = vec3d.zCoord;
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
