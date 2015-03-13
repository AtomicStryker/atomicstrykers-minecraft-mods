package atomicstryker.ropesplus.common.arrows;

import net.minecraft.block.Block;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityArrow303Grass extends EntityArrow303
{

    public EntityArrow303Grass(World world)
    {
        super(world);
        init();
    }
    
    public EntityArrow303Grass(World world, EntityPlayer ent, float power)
    {
        super(world, ent, power);
        init();
    }
    
    private void init()
    {
        name = "SeedArrow";
        craftingResults = 4;
        tip = Items.wheat_seeds;
        item = new ItemStack(itemId, 1, 0);
        icon = "ropesplus:grassarrow";
    }

    @Override
    public boolean onHitBlock(int blockX, int blockY, int blockZ)
    {
        Block hitBlockID = worldObj.getBlockState(new BlockPos(blockX, blockY, blockZ)).getBlock();
        if(hitBlockID == Blocks.dirt)
        {
            worldObj.setBlockState(new BlockPos(blockX,  blockY,  blockZ),  Blocks.grass.getStateFromMeta( 0));
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Blocks.grass && worldObj.getBlockState(new BlockPos(blockX, blockY+1, blockZ)).getBlock() == Blocks.air)
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
            worldObj.setBlockState(new BlockPos(blockX,  blockY+1,  blockZ),  targetblock.getStateFromMeta( 0));
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Blocks.cobblestone)
        {
            worldObj.setBlockState(new BlockPos(blockX,  blockY,  blockZ),  Blocks.mossy_cobblestone.getStateFromMeta( 0));
            setDead();
            return super.onHitBlock(blockX, blockY, blockZ);
        }
        else if(hitBlockID == Blocks.farmland && worldObj.getBlockState(new BlockPos(blockX, blockY+1, blockZ)).getBlock() == Blocks.air)
        {
            worldObj.setBlockState(new BlockPos(blockX,  blockY+1,  blockZ),  Blocks.wheat.getStateFromMeta( 0));
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
            this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
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
