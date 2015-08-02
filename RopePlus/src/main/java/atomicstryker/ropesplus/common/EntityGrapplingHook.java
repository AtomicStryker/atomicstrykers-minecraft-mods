package atomicstryker.ropesplus.common;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;


public class EntityGrapplingHook extends Entity
{

    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private boolean inGround;
    public EntityPlayer owner;
    private Entity plantedHook;
    private int ticksInGround;
    private int ticksInAir;
    private int ticksCatchable;
    private int rotationTicks;
    private double positionX;
    private double positionY;
    private double positionZ;
    private double rotYaw;
    private double rotPitch;
    private double startPosX;
    private double startPosZ;
    
    public EntityGrapplingHook(World world)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = Blocks.air;
        inGround = false;
        ticksInAir = 0;
        ticksCatchable = 0;
        setSize(0.25F, 0.25F);
        ignoreFrustumCheck = true;
        plantedHook = null;
    }
    
    public EntityGrapplingHook(World world, EntityPlayer entityplayer)
    {
        this(world);
        owner = entityplayer;
        setLocationAndAngles(entityplayer.posX, (entityplayer.posY + 1.6200000000000001D) - (double)entityplayer.getYOffset(), entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch);
        posX -= MathHelper.cos((rotationYaw / 180F) * 3.141593F) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin((rotationYaw / 180F) * 3.141593F) * 0.16F;
        setPosition(posX, posY, posZ);
        float f = 0.4F;
        motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f;
        motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f;
        motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F) * f;
        calculateVelocity(motionX, motionY, motionZ, 1.5F, 1.0F);
        startPosX = owner.posX;
        startPosZ = owner.posZ;
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    public boolean isInRangeToRenderDist(double d)
    {
        return true;
    }

    public void calculateVelocity(double d, double d1, double d2, float f, float f1)
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

    /*
    @Override
    public void setPositionAndRotation2(double d, double d1, double d2, float f, float f1, int i)
    {
        positionX = d;
        positionY = d1;
        positionZ = d2;
        rotYaw = f;
        rotPitch = f1;
        rotationTicks = i;
        motionX = velocityX;
        motionY = velocityY;
        motionZ = velocityZ;
    }
    */

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if(rotationTicks > 0)
        {
            double d = posX + (positionX - posX) / (double)rotationTicks;
            double d1 = posY + (positionY - posY) / (double)rotationTicks;
            double d2 = posZ + (positionZ - posZ) / (double)rotationTicks;
            double d4;
            for(d4 = rotYaw - (double)rotationYaw; d4 < -180D; d4 += 360D) { }
            for(; d4 >= 180D; d4 -= 360D) { }
            rotationYaw += d4 / (double)rotationTicks;
            rotationPitch += (rotPitch - (double)rotationPitch) / (double)rotationTicks;
            rotationTicks--;
            setPosition(d, d1, d2);
            setRotation(rotationYaw, rotationPitch);
            return;
        }
        if(!worldObj.isRemote)
        {
            if(owner == null)
            {
                setDead();
                return;
            }
            ItemStack itemstack = owner.getCurrentEquippedItem();
            if(owner.isDead
			|| itemstack == null
			|| itemstack.getItem() != RopesPlusCore.instance.itemGrapplingHook
			|| getDistanceSqToEntity(owner) > 1024D)
            {
                setDead();
                return;
            }
            if(plantedHook != null)
            {
                if(plantedHook.isDead)
                {
                    plantedHook = null;
                } else
                {
                    posX = plantedHook.posX;
                    posY = plantedHook.getEntityBoundingBox().minY + (double)plantedHook.height * 0.80000000000000004D;
                    posZ = plantedHook.posZ;
                    return;
                }
            }
        }
        if(inGround)
        {
            Block i = worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock();
            if(i != inTile)
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksInAir = 0;
            } else
            {
                ticksInGround++;
                if(ticksInGround == 1200)
                {
                    setDead();
                }
                return;
            }
        } else
        {
            ticksInAir++;
        }
        Vec3 currentVec = new Vec3(posX, posY, posZ);
        Vec3 projectedVec = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition projectedCollision = worldObj.rayTraceBlocks(currentVec, projectedVec);
        if(projectedCollision != null)
        {
            projectedVec = projectedCollision.hitVec;
        }
        Entity entity = null;
        @SuppressWarnings("rawtypes")
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double d3 = 0.0D;
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if(!entity1.canBeCollidedWith() || entity1 == owner && ticksInAir < 5)
            {
                continue;
            }
            float f2 = 0.3F;
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f2, f2, f2);
            MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(currentVec, projectedVec);
            if(movingobjectposition1 == null)
            {
                continue;
            }
            double d8 = currentVec.distanceTo(movingobjectposition1.hitVec);
            if(d8 < d3 || d3 == 0.0D)
            {
                entity = entity1;
                d3 = d8;
            }
        }

        if(entity != null)
        {
            projectedCollision = new MovingObjectPosition(entity);
        }
        if(projectedCollision != null)
        {
            if(projectedCollision.entityHit != null)
            {
                if(projectedCollision.entityHit.attackEntityFrom(DamageSource.causePlayerDamage(owner), 0))
                {
                    plantedHook = projectedCollision.entityHit;
                }
            }
            else
            {
                double orientationX = motionX;
                double orientationZ = motionZ;
                xTile = (int) projectedCollision.getBlockPos().getX();
                yTile = (int) projectedCollision.getBlockPos().getY();
                zTile = (int) projectedCollision.getBlockPos().getZ();
                inTile = worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock();
                motionX = (float)(projectedCollision.hitVec.xCoord - posX);
                motionY = (float)(projectedCollision.hitVec.yCoord - posY);
                motionZ = (float)(projectedCollision.hitVec.zCoord - posZ);
                float f3 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                posX -= (motionX / (double)f3) * 0.05000000074505806D;
                posY -= (motionY / (double)f3) * 0.05000000074505806D;
                posZ -= (motionZ / (double)f3) * 0.05000000074505806D;
                
                double hookLyingHeight = projectedCollision.hitVec.yCoord - (double)yTile;
                
                if(((hookLyingHeight == 1.0D && (worldObj.getBlockState(new BlockPos(xTile, yTile + 1, zTile)).getBlock() == Blocks.air)
                || worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock() == Blocks.snow))
                && yTile + 1 < 256)
                {
                    if(orientationX == 0.0D || orientationZ == 0.0D)
                    {
                        orientationX = posX - startPosX;
                        orientationZ = posZ - startPosZ;
                    }
                    byte xOffset = ((byte)(orientationX <= 0.0D ? -1 : 1));
                    byte zOffset = ((byte)(orientationZ <= 0.0D ? -1 : 1));
                    
                    boolean snowSituation = (worldObj.getBlockState(new BlockPos(xTile, yTile, zTile)).getBlock() == Blocks.snow);
                    
                    boolean canPlaceAtXOffset = (worldObj.getBlockState(new BlockPos(xTile - xOffset, yTile, zTile)).getBlock() == Blocks.air || worldObj.getBlockState(new BlockPos(xTile - xOffset, yTile, zTile)).getBlock() == Blocks.snow) && worldObj.getBlockState(new BlockPos(xTile - xOffset, yTile + 1, zTile)).getBlock() == Blocks.air;
                    boolean canPlaceAtZOffset = (worldObj.getBlockState(new BlockPos(xTile, yTile, zTile - zOffset)).getBlock() == Blocks.air || worldObj.getBlockState(new BlockPos(xTile, yTile, zTile - zOffset)).getBlock() == Blocks.snow) && worldObj.getBlockState(new BlockPos(xTile, yTile + 1, zTile - zOffset)).getBlock() == Blocks.air;
                    int xRope = xTile;
                    int yRope = yTile;
                    int zRope = zTile;
                    byte metaData = 0;
                    boolean canPlace = false;
                    if(canPlaceAtXOffset && !canPlaceAtZOffset || canPlaceAtXOffset && canPlaceAtZOffset && Math.abs(orientationX) > Math.abs(orientationZ))
                    {
                        xRope -= xOffset;
                        canPlace = true;
                        if(xOffset > 0)
                        {
                            metaData = 8;
                        } else
                        {
                            metaData = 2;
                        }
                    } else
                    if(!canPlaceAtXOffset && canPlaceAtZOffset || canPlaceAtXOffset && canPlaceAtZOffset && Math.abs(orientationX) <= Math.abs(orientationZ))
                    {
                        zRope -= zOffset;
                        canPlace = true;
                        if(zOffset > 0)
                        {
                            metaData = 1;
                        } else
                        {
                            metaData = 4;
                        }
                    }
                    if(canPlace)
                    {
                    	if (snowSituation) // in the snow case we need to lower hook and rope by 1, into the snow
                    	{
                    		yTile--;
                    		yRope--;
                    	}
                    	
                        worldObj.setBlockState(new BlockPos(xTile,  yTile + 1,  zTile),  RopesPlusCore.instance.blockGrapplingHook.getStateFromMeta( metaData));
                        worldObj.setBlockState(new BlockPos(xRope,  yRope,  zRope),  RopesPlusCore.instance.blockRopeWall.getStateFromMeta( metaData));
                        
                        TileEntityRope newent = new TileEntityRope(worldObj, xRope, yRope, zRope, 32);
                        RopesPlusCore.instance.addRopeToArray(newent);
                        
                        if(owner != null)
                        {
                            owner.destroyCurrentEquippedItem();
                            if (!worldObj.isRemote)
                            {
                            	RopesPlusCore.instance.getGrapplingHookMap().remove(owner);
                            }
                        }
                        setDead();
                    } else
                    {
                        inGround = true;
                    }
                }
            }
        }
        if(inGround)
        {
            return;
        }
        moveEntity(motionX, motionY, motionZ);
        float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        for(rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
        for(; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }
        for(; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
        for(; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float f1 = 0.92F;
        if(onGround || isCollidedHorizontally)
        {
            f1 = 0.5F;
        }
        double d7 = 0.0D;
        if(d7 > 0.0D)
        {
            if(ticksCatchable > 0)
            {
                ticksCatchable--;
            } else
            if(rand.nextInt(500) == 0)
            {
                ticksCatchable = rand.nextInt(30) + 10;
                motionY -= 0.20000000298023224D;
                worldObj.playSoundAtEntity(this, "random.splash", 0.25F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                float f4 = MathHelper.floor_double(getEntityBoundingBox().minY);
                for(int i1 = 0; (float)i1 < 1.0F + width * 20F; i1++)
                {
                    float f5 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                    float f7 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX + (double)f5, f4 + 1.0F, posZ + (double)f7, motionX, motionY - (double)(rand.nextFloat() * 0.2F), motionZ);
                }

                for(int j1 = 0; (float)j1 < 1.0F + width * 20F; j1++)
                {
                    float f6 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                    float f8 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                    worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX + (double)f6, f4 + 1.0F, posZ + (double)f8, motionX, motionY, motionZ);
                }

            }
        }
        if(ticksCatchable > 0)
        {
            motionY -= (double)(rand.nextFloat() * rand.nextFloat() * rand.nextFloat()) * 0.20000000000000001D;
        }
        double d9 = d7 * 2D - 1.0D;
        motionY += 0.039999999105930328D * d9;
        if(d7 > 0.0D)
        {
            f1 = (float)((double)f1 * 0.90000000000000002D);
            motionY *= 0.80000000000000004D;
        }
        motionX *= f1;
        motionY *= f1;
        motionZ *= f1;
        setPosition(posX, posY, posZ);
    }
	
    public void recallHook(EntityPlayer entityplayer)
    {
        if (owner == null)
        {
            owner = entityplayer;
        }
        
        if(plantedHook != null)
        {
            double d = owner.posX - posX;
            double d1 = owner.posY - posY;
            double d2 = owner.posZ - posZ;
            double d3 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
            double d4 = 0.10000000000000001D;
            plantedHook.motionX += d * d4;
            plantedHook.motionY += d1 * d4 + (double)MathHelper.sqrt_double(d3) * 0.080000000000000002D;
            plantedHook.motionZ += d2 * d4;
        }
        setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setShort("xTile", (short)xTile);
        nbttagcompound.setShort("yTile", (short)yTile);
        nbttagcompound.setShort("zTile", (short)zTile);
        nbttagcompound.setByte("inGround", (byte)(inGround ? 1 : 0));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        xTile = nbttagcompound.getShort("xTile");
        yTile = nbttagcompound.getShort("yTile");
        zTile = nbttagcompound.getShort("zTile");
        inGround = nbttagcompound.getByte("inGround") == 1;
    }
    
    /*
    @Override
    public float getShadowSize()
    {
        return 0.0F;
    }
    */

    @Override
    public void setDead()
    {
        super.setDead();
        owner = null;
    }

}
