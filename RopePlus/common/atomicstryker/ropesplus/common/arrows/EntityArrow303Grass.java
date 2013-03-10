package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
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

    public EntityArrow303Grass(World world, EntityLiving entityliving, float power)
    {
        super(world, entityliving, power);
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
            worldObj.setBlockAndMetadataWithNotify(blockX, blockY, blockZ, Block.grass.blockID, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.cobblestone.blockID)
        {
            worldObj.setBlockAndMetadataWithNotify(blockX, blockY, blockZ, Block.cobblestoneMossy.blockID, 0, 3);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.tilledField.blockID && worldObj.getBlockId(blockX, blockY+1, blockZ) == 0)
        {
            worldObj.setBlockAndMetadataWithNotify(blockX, blockY+1, blockZ, Block.crops.blockID, 0, 3);
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
    
}
