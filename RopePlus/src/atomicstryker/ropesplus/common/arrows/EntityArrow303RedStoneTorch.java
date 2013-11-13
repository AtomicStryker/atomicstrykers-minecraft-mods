package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;


public class EntityArrow303RedStoneTorch extends EntityArrow303
{
    
    public EntityArrow303RedStoneTorch(World world)
    {
        super(world);
    }

    public EntityArrow303RedStoneTorch(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
    }

    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Redstonetorch Arrow";
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowRedStoneTorch;
        tip = Block.torchRedstoneActive;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:rstorcharrow";
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Block.torchRedstoneActive.blockID))
        {
        	setDead();
        }
        return super.onHitBlock(x, y, z);
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("reddust",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303RedStoneTorch entityarrow = new EntityArrow303RedStoneTorch(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
