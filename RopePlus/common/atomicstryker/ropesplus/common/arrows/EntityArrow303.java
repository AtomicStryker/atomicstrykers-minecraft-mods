package atomicstryker.ropesplus.common.arrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import atomicstryker.ropesplus.common.EntityProjectileBase;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.*;

public class EntityArrow303 extends EntityProjectileBase
{
    public int[] placeCoords = new int[3];

    public String name;
    public int itemId;
    public int craftingResults;
    public Object tip;
    public Entity target;
    public boolean homing;
    public static int candidates[][] = {
        {
            0, 0, 0
        }, {
            0, -1, 0
        }, {
            0, 1, 0
        }, {
            -1, 0, 0
        }, {
            1, 0, 0
        }, {
            0, 0, -1
        }, {
            0, 0, 1
        }, {
            -1, -1, 0
        }, {
            -1, 0, -1
        }, {
            -1, 0, 1
        }, {
            -1, 1, 0
        }, {
            0, -1, -1
        }, {
            0, -1, 1
        }, {
            0, 1, -1
        }, {
            0, 1, 1
        }, {
            1, -1, 0
        }, {
            1, 0, -1
        }, {
            1, 0, 1
        }, {
            1, 1, 0
        }, {
            -1, -1, -1
        }, {
            -1, -1, 1
        }, {
            -1, 1, -1
        }, {
            -1, 1, 1
        }, {
            1, -1, -1
        }, {
            1, -1, 1
        }, {
            1, 1, -1
        }, {
            1, 1, 1
        }
    };
    
    public int getArrowIconIndex()
    {
        return 0;
    }
    
	@Override
    public void entityInit()
    {
        super.entityInit();
        homing = false;
        name = "Arrow";
        itemId = Item.arrow.shiftedIndex;
        craftingResults = 4;
        tip = Item.flint;
        curvature = 0.03F;
        slowdown = 0.99F;
        precision = 1.0F;
        speed = 1.5F;
        item = new ItemStack(itemId, 1, 0);
        yOffset = 0.0F;
        setSize(0.5F, 0.5F);
    }

    public EntityArrow303 newArrow(World world, EntityLiving entityliving)
    {
        try
        {
            return (EntityArrow303) getClass().getConstructor(new Class[] {
                    net.minecraft.src.World.class,
                    net.minecraft.src.EntityLiving.class }).newInstance(new Object[] {
                    world,
                    entityliving });
        }
        catch(Throwable throwable)
        {
            throw new RuntimeException("Could not construct arrow instance", throwable);
        }
    }

    public EntityArrow303 newArrow(World world)
    {
        try
        {
            return (EntityArrow303)getClass().getConstructor(new Class[] {
                net.minecraft.src.World.class
            }).newInstance(new Object[] {
                world
            });
        }
        catch(Throwable throwable)
        {
            throw new RuntimeException("Could not construct arrow instance", throwable);
        }
    }

    public void setupConfig()
    {
    }

    public EntityArrow303(World world)
    {
        super(world);
    }

    public EntityArrow303(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
        homing = false;
    }

    public boolean isInSight(Entity entity)
    {
        return worldObj.rayTraceBlocks(Vec3.createVectorHelper(posX, posY + (double)getEyeHeight(), posZ), Vec3.createVectorHelper(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ)) == null;
    }

    @Override
    public void handleMotionUpdate()
    {
        if(!homing)
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
        }
        if(target == null)
        {
            motionY -= curvature;
        }
    }

    @Override
    public void tickFlying()
    {
        if(ticksFlying > 1 && homing)
        {
            if(target == null || target.isDead || (target instanceof EntityLiving) && ((EntityLiving)target).deathTime != 0)
            {
                if(shooter instanceof EntityCreature)
                {
                    target = ((EntityCreature)shooter).getAITarget();
                } else
                {
                    target = getTarget(posX, posY, posZ, 16D);
                }
            } else
            if((shooter instanceof EntityPlayer) && !isInSight(target))
            {
                target = getTarget(posX, posY, posZ, 16D);
            }
            if(target != null)
            {
                double d = (target.boundingBox.minX + (target.boundingBox.maxX - target.boundingBox.minX) / 2D) - posX;
                double d1 = (target.boundingBox.minY + (target.boundingBox.maxY - target.boundingBox.minY) / 2D) - posY;
                double d2 = (target.boundingBox.minZ + (target.boundingBox.maxZ - target.boundingBox.minZ) / 2D) - posZ;
                setArrowHeading(d, d1, d2, speed, precision);
            }
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !isDead && !inGround && ticksFlying >= 2;
    }

    @Override
    public boolean canBeShot(Entity entity)
    {
        return !(entity instanceof EntityArrow303) && super.canBeShot(entity);
    }

    private Entity getTarget(double d, double d1, double d2, double d3)
    {
        float f = -1F;
        Entity entity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(d3, d3, d3));
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity1 = (Entity)list.get(i);
            if(!canTarget(entity1))
            {
                continue;
            }
            float f1 = entity1.getDistanceToEntity(this);
            if(f == -1F || f1 < f)
            {
                f = f1;
                entity = entity1;
            }
        }

        return entity;
    }

    public boolean canTarget(Entity entity)
    {
        return (entity instanceof EntityLiving) && entity != shooter && isInSight(entity);
    }

    public boolean tryToPlaceBlock(EntityPlayer shooter, int i)
    {
        int j = MathHelper.floor_double(posX);
        int k = MathHelper.floor_double(posY);
        int l = MathHelper.floor_double(posZ);
        boolean flag = false;
        int ai[][] = candidates;
        int j1 = ai.length;
        int l1 = 0;
        do
        {
            if(l1 >= j1)
            {
                break;
            }
            int ai1[] = ai[l1];
            int i2 = ai1[0];
            int j2 = ai1[1];
            int k2 = ai1[2];
            if(worldObj.canPlaceEntityOnSide(i, j + i2, k + j2, l + k2, true, 1, (Entity)null))
            {
                j += i2;
                k += j2;
                l += k2;
                flag = true;
                break;
            }
            l1++;
        } while(true);
        if(!flag)
        {
            return false;
        }
		
		placeCoords[0] = j;
		placeCoords[1] = k;
		placeCoords[2] = l;
		
        if(!worldObj.isRemote)
        {
			int i1 = worldObj.getBlockId(j, k, l);
			if(i1 > 0)
			{
				int k1 = worldObj.getBlockMetadata(j, k, l);
				Block.blocksList[i1].harvestBlock(worldObj, shooter, j, k, l, k1);
			}
			worldObj.setBlockAndMetadataWithNotify(j, k, l, i, 0);
		}
		
        return true;
    }
}
