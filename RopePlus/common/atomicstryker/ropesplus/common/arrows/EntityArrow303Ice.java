package atomicstryker.ropesplus.common.arrows;

import java.util.List;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Ice extends EntityArrow303
{
    
    public EntityLiving victim;
    public float freezeFactor;
    public int freezeTimer;
    
    public EntityArrow303Ice(World world)
    {
        super(world);
    }

    public EntityArrow303Ice(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Frost Arrow";
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

    @Override
    public boolean onHitTarget(Entity entity)
    {
        if (!(entity instanceof EntityLiving) || victim != null)
        {
            return false;
        }
        
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, entity.boundingBox.expand(3D, 3D, 3D));
        for (int i = 0; i < list.size(); i++)
        {
            Entity entity1 = (Entity) list.get(i);
            if (!(entity1 instanceof EntityArrow303Ice))
            {
                continue;
            }
            EntityArrow303Ice entityarrow303ice = (EntityArrow303Ice) entity1;
            if (entityarrow303ice.victim == entity)
            {
                entityarrow303ice.freezeTimer += getFreezeTimer((EntityLiving) entity);
                entityarrow303ice.setDead();
                entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) shooter), 4);
                return super.onHitTarget(entity);
            }
        }

        entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) shooter), 4);
        freezeMob((EntityLiving) entity);
        return super.onHitTarget(entity);
    }

    private int getFreezeTimer(EntityLiving entityliving)
    {
        return ((entityliving instanceof EntityPlayer) ? 5 : 10 * 20);
    }

    private void freezeMob(EntityLiving entityliving)
    {
        victim = entityliving;
        freezeFactor = ((entityliving instanceof EntityPlayer) ? 0.5F : 0.1F);
        freezeTimer = getFreezeTimer(entityliving);
        motionX = motionY = motionZ = 0.0D;
    }

    private void unfreezeMob()
    {
        victim = null;
    }

    @Override
    public void setDead()
    {
        if (victim != null)
        {
            unfreezeMob();
        }
        super.setDead();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (victim != null)
        {
            if (victim.isDead || victim.deathTime > 0)
            {
                setDead();
                return;
            }
            isDead = false;
            inGround = false;
            posX = victim.posX;
            posY = victim.boundingBox.minY + (double) victim.height * 0.5D;
            posZ = victim.posZ;
            setPosition(posX, posY, posZ);
            victim.motionX *= freezeFactor;
            victim.motionY *= freezeFactor;
            victim.motionZ *= freezeFactor;
            freezeTimer--;
            if (freezeTimer <= 0)
            {
                setDead();
            }
        }
    }

    @Override
    public boolean onHitBlock(int curX, int curY, int curZ)
    {
        for (int iX = curX - 1; iX <= curX + 1; iX++)
        {
            for (int iY = curY - 1; iY <= curY + 1; iY++)
            {
                for (int iZ = curZ - 1; iZ <= curZ + 1; iZ++)
                {
                    if (worldObj.getBlockMaterial(iX, iY, iZ) == Material.water && worldObj.getBlockMetadata(iX, iY, iZ) == 0)
                    {
                        worldObj.setBlockWithNotify(iX, iY, iZ, Block.ice.blockID);
                        continue;
                    }
                    if (worldObj.getBlockMaterial(iX, iY, iZ) == Material.lava && worldObj.getBlockMetadata(iX, iY, iZ) == 0)
                    {
                        worldObj.setBlockWithNotify(iX, iY, iZ, Block.cobblestone.blockID);
                        continue;
                    }
                    if (worldObj.getBlockId(iX, iY, iZ) == Block.fire.blockID)
                    {
                        worldObj.setBlockWithNotify(iX, iY, iZ, 0);
                        continue;
                    }
                    if (worldObj.getBlockId(iX, iY, iZ) == Block.torchWood.blockID)
                    {
                        Block.blocksList[Block.torchWood.blockID].dropBlockAsItemWithChance(worldObj, iX, iY, iZ, worldObj.getBlockMetadata(iX, iY, iZ), 1.0F, 0);
                        worldObj.setBlockWithNotify(iX, iY, iZ, 0);
                        Block.blocksList[Block.torchWood.blockID].onBlockDestroyedByExplosion(worldObj, iX, iY, iZ);
                    }
                }
            }
        }

        return super.onHitBlock(curX, curY, curZ);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("snowballpoof",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }

}
