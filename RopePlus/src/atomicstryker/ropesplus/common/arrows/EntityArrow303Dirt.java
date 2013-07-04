package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.Settings_RopePlus;

public class EntityArrow303Dirt extends EntityArrow303
{
    
    public EntityArrow303Dirt(World world)
    {
        super(world);
    }

    public EntityArrow303Dirt(World world, EntityLivingBase EntityLivingBase, float power)
    {
        super(world, EntityLivingBase, power);
    }
    
    @Override
    public void entityInit()
    {
        super.entityInit();
        name = "Dirt Arrow";
        craftingResults = 8;
        itemId = Settings_RopePlus.itemIdArrowDirt;
        tip = Block.dirt;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:dirtarrow";
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        if(tryToPlaceBlock((EntityPlayer)shooter, Block.dirt.blockID))
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
    
}
