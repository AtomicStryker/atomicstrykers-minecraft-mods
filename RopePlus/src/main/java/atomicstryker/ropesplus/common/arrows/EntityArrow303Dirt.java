package atomicstryker.ropesplus.common.arrows;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303Dirt extends EntityArrow303
{
    
    public EntityArrow303Dirt(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Dirt(World world, EntityLivingBase ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "DirtArrow";
        craftingResults = 8;
        tip = Blocks.dirt;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:dirtarrow";
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Blocks.dirt))
        {
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        return false;
    }
    
    @Override
    public void tickFlying()
    {
        super.tickFlying();
        
        for (int i = 0; i < 4; ++i)
        {
            this.worldObj.spawnParticle("tilecrack_3_0",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Dirt entityarrow = new EntityArrow303Dirt(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
