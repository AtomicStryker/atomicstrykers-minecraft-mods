package atomicstryker.ropesplus.common;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityDamageSourceIndirect;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

public abstract class EntityProjectileBase extends Entity
{
    public float speed;
    public float slowdown;
    public float curvature;
    public float precision;
    public float hitBox;
    public int dmg;
    public ItemStack item;
    public int ttlInGround;
    public int xTile;
    public int yTile;
    public int zTile;
    public int inTile;
    public int inData;
    public boolean inGround;
    public int arrowShake;
    public EntityLiving shooter;
    public int ticksInGround;
    public int ticksFlying;
    private final int ticksBeforeCollidable = 5;
    public boolean shotByPlayer;
    public boolean arrowCritical = false;

    public EntityProjectileBase(World world)
    {
        super(world);
        shooter = null;
    }

    public EntityProjectileBase(World world, EntityLiving entityliving)
    {
        this(world);
        shooter = entityliving;
        shotByPlayer = entityliving instanceof EntityPlayer;
        setLocationAndAngles(entityliving.posX, entityliving.posY + (double)entityliving.getEyeHeight(), entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
        posX -= MathHelper.cos((rotationYaw / 180F) * 3.141593F) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin((rotationYaw / 180F) * 3.141593F) * 0.16F;
        setPosition(posX, posY, posZ);
        motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F);
        motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F);
        motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F);
        setArrowHeading(motionX, motionY, motionZ, speed, precision);
    }

    @Override
    protected void entityInit()
    {
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inGround = false;
        arrowShake = 0;
        ticksFlying = 0;
        setSize(0.5F, 0.5F);
        yOffset = 0.0F;
        hitBox = 0.3F;
        speed = 1.0F;
        slowdown = 0.99F;
        curvature = 0.03F;
        dmg = 4;
        precision = 1.0F;
        ttlInGround = 1200;
        item = null;
    }

    @Override
    public void setDead()
    {
        super.setDead();
    }

    public void setArrowHeading(double d, double d1, double d2, float f, float f1)
    {
        float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
        d /= f2;
        d1 /= f2;
        d2 /= f2;
        d += rand.nextGaussian() * 0.0074999998323619366D * (double)f1;
        d1 += rand.nextGaussian() * 0.0074999998323619366D * (double)f1;
        d2 += rand.nextGaussian() * 0.0074999998323619366D * (double)f1;
        d *= f;
        d1 *= f;
        d2 *= f;
        motionX = d;
        motionY = d1;
        motionZ = d2;
        float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
        prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
        prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f3) * 180D) / 3.1415927410125732D);
        ticksInGround = 0;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if(prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
            prevRotationPitch = rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D);
        }
        if(arrowShake > 0)
        {
            arrowShake--;
        }
        if(inGround)
        {
            int i = worldObj.getBlockId(xTile, yTile, zTile);
            int j = worldObj.getBlockMetadata(xTile, yTile, zTile);
            if(i != inTile || j != inData)
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksFlying = 0;
            } else
            {
                ticksInGround++;
                tickInGround();
                if(ticksInGround == ttlInGround)
                {
                	setDead();
                }
                return;
            }
        } else
        {
            ticksFlying++;
        }
        tickFlying();
        Vec3 vec3 = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 vec31 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec3, vec31);
        vec3 = Vec3.createVectorHelper(posX, posY, posZ);
        vec31 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        if(movingobjectposition != null)
        {
            vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
        }
        Entity entity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double d = 0.0D;
        for(int k = 0; k < list.size(); k++)
        {
            Entity entity2 = (Entity)list.get(k);
            if(!canBeShot(entity2))
            {
                continue;
            }
            float f3 = hitBox;
            AxisAlignedBB axisalignedbb = entity2.boundingBox.expand(f3, f3, f3);
            MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);
            if(movingobjectposition1 == null)
            {
                continue;
            }
            double d1 = vec3.distanceTo(movingobjectposition1.hitVec);
            if(d1 < d || d == 0.0D)
            {
                entity = entity2;
                d = d1;
            }
        }

        if(entity != null)
        {
            movingobjectposition = new MovingObjectPosition(entity);
        }
        if(movingobjectposition != null && (ticksFlying > ticksBeforeCollidable) && onHit())
        {
            Entity entity1 = movingobjectposition.entityHit;
            if(entity1 != null)
            {
                if(onHitTarget(entity1))
                {
                    if (shooter != null)
                    {
                        entity1.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)shooter), this.arrowCritical ? dmg*2 : dmg);
                    }
                    else
                    {
                        entity1.attackEntityFrom((new EntityDamageSourceIndirect("arrow", this, this)).setProjectile(), dmg);
                    }
                    
                    setDead();
                }
            } else
            {
                xTile = movingobjectposition.blockX;
                yTile = movingobjectposition.blockY;
                zTile = movingobjectposition.blockZ;
                inTile = worldObj.getBlockId(xTile, yTile, zTile);
                inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
                if(onHitBlock(movingobjectposition))
                {
                    motionX = (float)(movingobjectposition.hitVec.xCoord - posX);
                    motionY = (float)(movingobjectposition.hitVec.yCoord - posY);
                    motionZ = (float)(movingobjectposition.hitVec.zCoord - posZ);
                    float f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    posX -= (motionX / (double)f2) * 0.05000000074505806D;
                    posY -= (motionY / (double)f2) * 0.05000000074505806D;
                    posZ -= (motionZ / (double)f2) * 0.05000000074505806D;
                    inGround = true;
                    arrowShake = 7;
                    this.arrowCritical = false;
                } else
                {
                    inTile = 0;
                    inData = 0;
                }
            }
        }
        
        if(this.arrowCritical) {
            for(int var8 = 0; var8 < 4; ++var8) {
               this.worldObj.spawnParticle("crit", this.posX + this.motionX * (double)var8 / 4.0D, this.posY + this.motionY * (double)var8 / 4.0D, this.posZ + this.motionZ * (double)var8 / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
            }
         }
        
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        handleMotionUpdate();
        float f1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        for(rotationPitch = (float)((Math.atan2(motionY, f1) * 180D) / 3.1415927410125732D); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
        for(; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }
        for(; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
        for(; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        setPosition(posX, posY, posZ);
    }

    public void handleMotionUpdate()
    {
        float f = slowdown;
        if(handleWaterMovement())
        {
            for(int i = 0; i < 4; i++)
            {
                float f1 = 0.25F;
                worldObj.spawnParticle("bubble", posX - motionX * (double)f1, posY - motionY * (double)f1, posZ - motionZ * (double)f1, motionX, motionY, motionZ);
            }

            f *= 0.8F;
        }
        motionX *= f;
        motionY *= f;
        motionZ *= f;
        motionY -= curvature;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setShort("xTile", (short)xTile);
        nbttagcompound.setShort("yTile", (short)yTile);
        nbttagcompound.setShort("zTile", (short)zTile);
        nbttagcompound.setByte("inTile", (byte)inTile);
        nbttagcompound.setByte("inData", (byte)inData);
        nbttagcompound.setByte("shake", (byte)arrowShake);
        nbttagcompound.setByte("inGround", (byte)(inGround ? 1 : 0));
        nbttagcompound.setBoolean("player", shotByPlayer);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        xTile = nbttagcompound.getShort("xTile");
        yTile = nbttagcompound.getShort("yTile");
        zTile = nbttagcompound.getShort("zTile");
        inTile = nbttagcompound.getByte("inTile") & 0xff;
        inData = nbttagcompound.getByte("inData") & 0xff;
        arrowShake = nbttagcompound.getByte("shake") & 0xff;
        inGround = nbttagcompound.getByte("inGround") == 1;
        shotByPlayer = nbttagcompound.getBoolean("player");
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
        if(item == null)
        {
            return;
        }
        if(worldObj.isRemote)
        {
            return;
        }
        if(inGround && shotByPlayer && arrowShake <= 0 && entityplayer.inventory.addItemStackToInventory(item.copy()))
        {
            worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.onItemPickup(this, 1);
            setDead();
        }
    }

    public boolean canBeShot(Entity entity)
    {
        return entity.canBeCollidedWith() && (entity != shooter || ticksFlying >= 2) && (!(entity instanceof EntityLiving) || ((EntityLiving)entity).deathTime <= 0);
    }

    public boolean onHit()
    {
        return true;
    }

    public boolean onHitTarget(Entity entity)
    {
        worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
        return true;
    }

    public void tickFlying()
    {
    }

    public void tickInGround()
    {
    }

    public boolean onHitBlock(MovingObjectPosition movingobjectposition)
    {
        return onHitBlock();
    }

    public boolean onHitBlock()
    {
        worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
        return true;
    }
}
