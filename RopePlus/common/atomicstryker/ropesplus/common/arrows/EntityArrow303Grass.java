package atomicstryker.ropesplus.common.arrows;

import atomicstryker.ropesplus.common.Settings_RopePlus;
import net.minecraft.src.*;

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
        craftingResults = 1;
        itemId = Settings_RopePlus.itemIdArrowGrass;
        tip = Item.seeds;
        item = new ItemStack(itemId, 1, 0);
    }
    
    @Override
    public int getArrowIconIndex()
    {
        return 5;
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        int hitBlockID = worldObj.getBlockId(blockX, blockY, blockZ);
        if(hitBlockID == Block.dirt.blockID)
        {
            worldObj.setBlockWithNotify(blockX, blockY, blockZ, Block.grass.blockID);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.cobblestone.blockID)
        {
            worldObj.setBlockWithNotify(blockX, blockY, blockZ, Block.cobblestoneMossy.blockID);
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Block.tilledField.blockID && worldObj.getBlockId(blockX, blockY+1, blockZ) == 0)
        {
            worldObj.setBlockWithNotify(blockX, blockY+1, blockZ, Block.crops.blockID);
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
