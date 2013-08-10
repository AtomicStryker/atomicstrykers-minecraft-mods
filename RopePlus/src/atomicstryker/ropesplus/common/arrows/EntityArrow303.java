package atomicstryker.ropesplus.common.arrows;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.EntityProjectileBase;

public class EntityArrow303 extends EntityProjectileBase
{
    public int[] placeCoords = new int[3];

    public String name;
    public int itemId;
    public int craftingResults;
    public Object tip;
    public Entity target;
    protected boolean isArrowHoming;
    protected String icon;
    
    protected final static int candidates[][] = { { 0, 0, 0 }, { 0, -1, 0 }, { 0, 1, 0 }, { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { -1, -1, 0 }, { -1, 0, -1 }, { -1, 0, 1 }, { -1, 1, 0 },
            { 0, -1, -1 }, { 0, -1, 1 }, { 0, 1, -1 }, { 0, 1, 1 }, { 1, -1, 0 }, { 1, 0, -1 }, { 1, 0, 1 }, { 1, 1, 0 }, { -1, -1, -1 }, { -1, -1, 1 }, { -1, 1, -1 }, { -1, 1, 1 }, { 1, -1, -1 },
            { 1, -1, 1 }, { 1, 1, -1 }, { 1, 1, 1 } };
    
    public EntityArrow303(World world)
    {
        super(world);
        isArrowHoming = false;
    }

    public EntityArrow303(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
        isArrowHoming = false;
    }
    
    public EntityArrow303 newArrow(World world, EntityLivingBase entityLivingBase, float power)
    {
        try
        {
            return (EntityArrow303) getClass().getConstructor(new Class[] { World.class, EntityLivingBase.class, float.class }).newInstance(new Object[] { world, entityLivingBase, power });
        }
        catch (Throwable throwable)
        {
            throw new RuntimeException("Could not construct arrow instance", throwable);
        }
    }

    public EntityArrow303 newArrow(World world)
    {
        try
        {
            return (EntityArrow303) getClass().getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
        }
        catch (Throwable throwable)
        {
            throw new RuntimeException("Could not construct arrow instance", throwable);
        }
    }
    
    public String getIcon()
    {
        return icon;
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Arrow";
        itemId = Item.arrow.itemID;
        tip = Item.flint;
        curvature = 0.03F;
        slowdown = 0.99F;
        precision = 1.0F;
        speed = 1.5F;
        item = new ItemStack(itemId, 1, 0);
        yOffset = 0.0F;
        setSize(0.5F, 0.5F);
    }

    public boolean isInSight(Entity entity)
    {
        return worldObj.clip(worldObj.getWorldVec3Pool().getVecFromPool(posX, posY + (double) getEyeHeight(), posZ),
                worldObj.getWorldVec3Pool().getVecFromPool(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ)) == null;
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        if (ticksFlying > 1 && isArrowHoming)
        {
            if (target == null || target.isDead || (target instanceof EntityLivingBase) && ((EntityLivingBase) target).deathTime != 0)
            {
                if (shooter instanceof EntityCreature)
                {
                    target = ((EntityCreature) shooter).getAITarget();
                }
                else
                {
                    target = getTarget(posX, posY, posZ, 16D);
                }
            }
            else if ((shooter instanceof EntityPlayer) && !isInSight(target))
            {
                target = getTarget(posX, posY, posZ, 16D);
            }
            if (target != null)
            {
                double diffX = (target.boundingBox.minX + (target.boundingBox.maxX - target.boundingBox.minX) / 2D) - posX;
                double diffY = (target.boundingBox.minY + (target.boundingBox.maxY - target.boundingBox.minY) / 2D) - posY;
                double diffZ = (target.boundingBox.minZ + (target.boundingBox.maxZ - target.boundingBox.minZ) / 2D) - posZ;
                setThrowableHeading(diffX, diffY, diffZ, speed, precision);
            }
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !isDead && !inGround && ticksFlying >= TICKS_BEFORE_COLLIDABLE;
    }

    @Override
    public boolean canBeShot(Entity entity)
    {
        return !(entity instanceof EntityArrow303) && super.canBeShot(entity);
    }

    protected Entity getTarget(double xPos, double yPos, double zPos, double boxSize)
    {
        float nearestDist = -1F;
        Entity entity = null;
        @SuppressWarnings("rawtypes")
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(boxSize, boxSize, boxSize));
        for (int i = 0; i < list.size(); i++)
        {
            Entity targetEnt = (Entity) list.get(i);
            if (!canTarget(targetEnt))
            {
                continue;
            }
            float curDist = targetEnt.getDistanceToEntity(this);
            if (nearestDist == -1F || curDist < nearestDist)
            {
                nearestDist = curDist;
                entity = targetEnt;
            }
        }

        return entity;
    }

    public boolean canTarget(Entity entity)
    {
        return (entity instanceof EntityLivingBase) && entity != shooter && isInSight(entity);
    }

    public boolean tryToPlaceBlock(EntityPlayer shooter, int blockID)
    {
        int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(posY);
        int z = MathHelper.floor_double(posZ);
        boolean canPlace = false;
        int arrayLength = candidates.length;
        int index = 0;
        do
        {
            int candidateCoords[] = candidates[index];
            int ix = candidateCoords[0];
            int iy = candidateCoords[1];
            int iz = candidateCoords[2];
            if (worldObj.canPlaceEntityOnSide(blockID, x + ix, y + iy, z + iz, true, 1, null, item))
            {
                x += ix;
                y += iy;
                z += iz;
                canPlace = true;
                break;
            }
            index++;
        }
        while (index < arrayLength);
        
        if (!canPlace)
        {
            return false;
        }

        placeCoords[0] = x;
        placeCoords[1] = y;
        placeCoords[2] = z;

        if (!worldObj.isRemote)
        {
            int prevBlockID = worldObj.getBlockId(x, y, z);
            if (prevBlockID > 0 && shooter != null)
            {
                int prevBlockMeta = worldObj.getBlockMetadata(x, y, z);
                Block.blocksList[prevBlockID].harvestBlock(worldObj, shooter, x, y, z, prevBlockMeta);
            }
            worldObj.setBlock(x, y, z, blockID, 0, 3);
        }

        return true;
    }
}
