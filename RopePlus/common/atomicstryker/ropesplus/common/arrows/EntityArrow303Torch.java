package atomicstryker.ropesplus.common.arrows;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;


public class EntityArrow303Torch extends EntityArrow303
{
    
    public EntityArrow303Torch(World world)
    {
        super(world);
    }

    public EntityArrow303Torch(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "TorchArrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowTorch;
        tip = Block.torchWood;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 11;
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Block.torchWood.blockID))
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
