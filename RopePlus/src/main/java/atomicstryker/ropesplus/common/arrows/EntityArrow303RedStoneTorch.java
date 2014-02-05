package atomicstryker.ropesplus.common.arrows;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303RedStoneTorch extends EntityArrow303
{
    
    public EntityArrow303RedStoneTorch(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303RedStoneTorch(World world, EntityLivingBase ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "RedstonetorchArrow";
        craftingResults = 1;
        tip = Blocks.redstone_torch;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:rstorcharrow";
    }

    @Override
    public boolean onHitBlock(int x, int y, int z)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Blocks.redstone_torch))
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
