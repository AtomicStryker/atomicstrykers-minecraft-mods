package atomicstryker.ropesplus.common.arrows;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;

public class EntityArrow303Slime extends EntityArrow303
{
 
    private boolean spawns;
    
    public EntityArrow303Slime(World world)
    {
        super(world);
        spawns = false;
    }

    public EntityArrow303Slime(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
        spawns = worldObj.rand.nextInt(4) == 0;
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Slime Arrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowSlime;
        tip = Item.slimeBall;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:slimearrow";
    }

    private EntityLiving makeMob()
    {
        EntitySlime entityslime = new EntitySlime(worldObj);
        entityslime.heal(1 << rand.nextInt(4));
        return entityslime;
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if (spawns)
        {
            EntityLiving entityliving = makeMob();
            entityliving.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            if(worldObj.spawnEntityInWorld(entityliving))
            {
                entityliving.spawnExplosionParticle();
                setDead();
            }
        }
        return super.onHitBlock(x, y, z);
    }

    @Override
    public boolean onHitTarget(Entity entity)
    {
        if (spawns)
        {
            EntityLiving entityliving = makeMob();
            entityliving.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            if(worldObj.spawnEntityInWorld(entityliving))
            {
                entityliving.spawnExplosionParticle();
                if(!(entity instanceof EntityPlayer))
                {
                    entity.setDead();
                }
            }
        }
        return super.onHitTarget(entity);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("slime",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
}
