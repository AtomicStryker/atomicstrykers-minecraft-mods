package atomicstryker.ropesplus.common.arrows;

import java.util.List;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Ice extends EntityArrow303
{

    public void subscreen()
    {
    }

    public void setupConfig()
    {
    }

    public void entityInit()
    {
        super.entityInit();
        name = "IceArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowIce;
        tip = Item.snowball;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 6;
    }

    public EntityArrow303Ice(World world)
    {
        super(world);
    }

    public EntityArrow303Ice(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public boolean onHitBlock()
    {
        if(victim == null)
        {
            setEntityDead();
            return true;
        } else
        {
            return false;
        }
    }

    public boolean onHitTarget(Entity entity)
    {
        if(!(entity instanceof EntityLiving) || victim != null)
        {
            return false;
        }
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, entity.boundingBox.expand(3D, 3D, 3D));
        for(int i = 0; i < list.size(); i++)
        {
            Entity entity1 = (Entity)list.get(i);
            if(!(entity1 instanceof EntityArrow303Ice))
            {
                continue;
            }
            EntityArrow303Ice entityarrow303ice = (EntityArrow303Ice)entity1;
            if(entityarrow303ice.victim == entity)
            {
                entityarrow303ice.freezeTimer += getFreezeTimer((EntityLiving)entity);
                entityarrow303ice.isDead = false;
                entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)shooter), 4);
                setEntityDead();
                return false;
            }
        }

        entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)shooter), 4);
        freezeMob((EntityLiving)entity);
        return false;
    }

    public int getFreezeTimer(EntityLiving entityliving)
    {
        return ((entityliving instanceof EntityPlayer) ? 5 : 10 * 20);
    }

    public void freezeMob(EntityLiving entityliving)
    {
        victim = entityliving;
        freezeFactor = ((entityliving instanceof EntityPlayer) ? 0.5F : 0.1F);
        freezeTimer = getFreezeTimer(entityliving);
        motionX = motionY = motionZ = 0.0D;
    }

    public void unfreezeMob()
    {
        victim = null;
    }

    public void setEntityDead()
    {
        if(victim != null)
        {
            unfreezeMob();
        }
        super.setDead();
    }

    public void onUpdate()
    {
        super.onUpdate();
        if(victim != null)
        {
            if(victim.isDead || victim.deathTime > 0)
            {
                setEntityDead();
                return;
            }
            isDead = false;
            inGround = false;
            posX = victim.posX;
            posY = victim.boundingBox.minY + (double)victim.height * 0.5D;
            posZ = victim.posZ;
            setPosition(posX, posY, posZ);
            victim.motionX *= freezeFactor;
            victim.motionY *= freezeFactor;
            victim.motionZ *= freezeFactor;
            freezeTimer--;
            if(freezeTimer <= 0)
            {
                setEntityDead();
            }
        }
    }

    public boolean onHit()
    {
        if(victim != null)
        {
            return false;
        }
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(posY);
        int k = MathHelper.floor_double(posZ);
        for(int l = i - 1; l <= i + 1; l++)
        {
            for(int i1 = j - 1; i1 <= j + 1; i1++)
            {
                for(int j1 = k - 1; j1 <= k + 1; j1++)
                {
                    if(worldObj.getBlockMaterial(l, i1, j1) == Material.water && worldObj.getBlockMetadata(l, i1, j1) == 0)
                    {
                        worldObj.setBlockWithNotify(l, i1, j1, 79);
                        continue;
                    }
                    if(worldObj.getBlockMaterial(l, i1, j1) == Material.lava && worldObj.getBlockMetadata(l, i1, j1) == 0)
                    {
                        worldObj.setBlockWithNotify(l, i1, j1, 49);
                        continue;
                    }
                    if(worldObj.getBlockId(l, i1, j1) == 51)
                    {
                        worldObj.setBlockWithNotify(l, i1, j1, 0);
                        continue;
                    }
                    if(worldObj.getBlockId(l, i1, j1) == 50)
                    {
                        Block.blocksList[50].dropBlockAsItemWithChance(worldObj, l, i1, j1, worldObj.getBlockMetadata(l, i1, j1), 1.0F, 0);
                        worldObj.setBlockWithNotify(l, i1, j1, 0);
                        Block.blocksList[50].onBlockDestroyedByExplosion(worldObj, l, i1, j1);
                    }
                }

            }

        }

        return true;
    }

    public void tickFlying()
    {
        super.tickFlying();
    }

    public EntityLiving victim;
    public float freezeFactor;
    public int freezeTimer;
}
