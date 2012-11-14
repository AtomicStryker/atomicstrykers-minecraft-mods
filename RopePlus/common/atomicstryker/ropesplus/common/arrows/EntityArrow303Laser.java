package atomicstryker.ropesplus.common.arrows;

import java.util.*;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Laser extends EntityArrow303
{

    public void entityInit()
    {
        super.entityInit();
        name = "LaserArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowLaser;
        tip = Item.redstone;
        curvature = 0.0F;
        slowdown = 1.3F;
        precision = 0.0F;
        speed = 2.0F;
        pierced = false;
        piercedMobs = new HashSet();
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 7;
    }

    public EntityArrow303Laser(World world)
    {
        super(world);
    }

    public EntityArrow303Laser(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHitTarget(Entity entity)
    {
        entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)shooter), 8);
        pierced = true;
        piercedMobs.add(entity);
        target = null;
        worldObj.playSoundAtEntity(this, sound, 1.0F, ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
        return false;
    }

    public boolean isInSight(Entity entity)
    {
        return canSee(this, entity) && canSee(entity, this);
    }

    public boolean canSee(Entity entity, Entity entity1)
    {
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(((Vec3)null).createVectorHelper(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ), ((Vec3)null).createVectorHelper(entity1.posX, entity1.posY + (double)entity1.getEyeHeight(), entity1.posZ));
        return movingobjectposition == null || movingobjectposition.typeOfHit == EnumMovingObjectType.TILE && isTransparent(worldObj.getBlockId(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ));
    }

    public boolean onHitBlock()
    {
        if(!isTransparent(inTile))
        {
            if(pierced)
            {
            	setDead();
            }
            return true;
        } else
        {
            return false;
        }
    }

    public boolean isTransparent(int i)
    {
        return Block.lightOpacity[i] != 255;
    }

    public void tickFlying()
    {
        super.tickFlying();
    }

    public boolean canTarget(Entity entity)
    {
        return !piercedMobs.contains(entity) && super.canTarget(entity);
    }

    public boolean pierced;
    public Set piercedMobs;
    public static String sound = "damage.fallbig";

}
