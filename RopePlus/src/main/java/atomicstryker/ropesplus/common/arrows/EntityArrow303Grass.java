package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityArrow303Grass extends EntityArrow303
{

    public EntityArrow303Grass(World world)
    {
        super(world);
    }

    public EntityArrow303Grass(World world, EntityLivingBase entityLivingBase, float power)
    {
        super(world, entityLivingBase, power);
    }
    
    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Seed Arrow";
        craftingResults = 4;
        tip = Items.wheat_seeds;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:grassarrow";
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        Block hitBlockID = worldObj.func_147439_a(blockX, blockY, blockZ);
        if(hitBlockID == Blocks.dirt)
        {
            worldObj.func_147465_d(blockX, blockY, blockZ, Blocks.grass, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Blocks.grass && worldObj.func_147439_a(blockX, blockY+1, blockZ) == Blocks.air)
        {
            Block targetblock = Blocks.air;
            switch (rand.nextInt(3))
            {
                case 0:
                {
                    targetblock = Blocks.red_flower;
                    break;
                }
                case 1:
                {
                    targetblock = Blocks.yellow_flower;
                    break;
                }
                default:
                {
                    targetblock = Blocks.tallgrass;
                    break;
                }
            }
            worldObj.func_147465_d(blockX, blockY+1, blockZ, targetblock, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Blocks.cobblestone)
        {
            worldObj.func_147465_d(blockX, blockY, blockZ, Blocks.mossy_cobblestone, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Blocks.farmland && worldObj.func_147439_a(blockX, blockY+1, blockZ) == Blocks.air)
        {
            worldObj.func_147465_d(blockX, blockY+1, blockZ, Blocks.wheat, 0, 3);
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
            this.worldObj.spawnParticle("tilecrack_18_0",
                    this.posX + this.motionX * (double) i / 4.0D,
                    this.posY + this.motionY * (double) i / 4.0D,
                    this.posZ + this.motionZ * (double) i / 4.0D,
                    -this.motionX, -this.motionY + 0.2D, -this.motionZ);
        }
    }
    
    @Override
    public IProjectile getProjectileEntity(World par1World, IPosition par2IPosition)
    {
        EntityArrow303Grass entityarrow = new EntityArrow303Grass(par1World);
        entityarrow.setPosition(par2IPosition.getX(), par2IPosition.getY(), par2IPosition.getZ());
        return entityarrow;
    }
    
}
