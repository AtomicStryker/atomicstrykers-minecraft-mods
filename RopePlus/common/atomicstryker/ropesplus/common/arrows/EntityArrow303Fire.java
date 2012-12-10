package atomicstryker.ropesplus.common.arrows;

import java.util.Random;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

public class EntityArrow303Fire extends EntityArrow303
{

    public EntityArrow303Fire(World world)
    {
        super(world);
    }

    public EntityArrow303Fire(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Fire Arrow";
        craftingResults = 4;
        itemId = Settings_RopePlus.itemIdArrowFire;
        tip = Item.coal;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 4;
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Block.fire.blockID))
        {
        	setDead();
        }
        return super.onHitBlock(x, y, z);
    }

    @Override
    public boolean onHitTarget(Entity entity)
    {
    	entity.setFire(300/20);
        return super.onHitTarget(entity);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("flame",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
}
