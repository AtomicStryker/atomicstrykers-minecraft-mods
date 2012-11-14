package atomicstryker.ropesplus.common.arrows;

import java.util.Random;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Slime extends EntityArrow303
{

    public void entityInit()
    {
        super.entityInit();
        name = "SlimeArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowSlime;
        tip = Item.slimeBall;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 10;
    }

    public EntityArrow303Slime(World world)
    {
        super(world);
    }

    public EntityArrow303Slime(World world, EntityLiving entityliving)
    {
        super(world, entityliving);
    }

    public EntityLiving makeMob()
    {
        EntitySlime entityslime = new EntitySlime(worldObj);
        entityslime.heal(1 << rand.nextInt(4));
        return entityslime;
    }

    public boolean onHitBlock()
    {
        EntityLiving entityliving = makeMob();
        entityliving.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
        if(worldObj.spawnEntityInWorld(entityliving))
        {
            entityliving.spawnExplosionParticle();
            setDead();
        }
        return true;
    }

    public boolean onHitTarget(Entity entity)
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
        return true;
    }

    public void tickFlying()
    {
        super.tickFlying();
    }
}
