package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;

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
        itemId = Settings_RopePlus.itemIdArrowGrass;
        tip = Item.seeds;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:grassarrow";
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        int hitBlockID = worldObj.getBlockId(blockX, blockY, blockZ);
        if(hitBlockID == Block.dirt.blockID)
        {
            worldObj.setBlock(blockX, blockY, blockZ, Block.grass.blockID, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.grass.blockID && worldObj.getBlockId(blockX, blockY+1, blockZ) == 0)
        {
            int targetblock = 0;
            switch (rand.nextInt(3))
            {
                case 0:
                {
                    targetblock = Block.plantRed.blockID;
                    break;
                }
                case 1:
                {
                    targetblock = Block.plantYellow.blockID;
                    break;
                }
                default:
                {
                    targetblock = Block.tallGrass.blockID;
                    break;
                }
            }
            worldObj.setBlock(blockX, blockY+1, blockZ, targetblock, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.cobblestone.blockID)
        {
            worldObj.setBlock(blockX, blockY, blockZ, Block.cobblestoneMossy.blockID, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.tilledField.blockID && worldObj.getBlockId(blockX, blockY+1, blockZ) == 0)
        {
            worldObj.setBlock(blockX, blockY+1, blockZ, Block.crops.blockID, 0, 3);
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
