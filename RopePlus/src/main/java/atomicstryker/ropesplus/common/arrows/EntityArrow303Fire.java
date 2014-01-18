package atomicstryker.ropesplus.common.arrows;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303Fire extends EntityArrow303
{

    public EntityArrow303Fire(World world)
    {
        super(world);
    }

    public EntityArrow303Fire(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Fire Arrow";
        craftingResults = 4;
        tip = Items.coal;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:fiarrow";
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Blocks.fire))
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
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Fire entityarrow = new EntityArrow303Fire(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
