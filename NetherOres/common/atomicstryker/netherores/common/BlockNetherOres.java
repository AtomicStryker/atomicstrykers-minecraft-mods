package atomicstryker.netherores.common;

import java.util.List;
import java.util.Random;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class BlockNetherOres extends Block
{	
	private static int aggroRange = 32;
	
	public BlockNetherOres(int i, int j)
	{
		super(i, j, Block.netherrack.blockMaterial);
		setHardness(5.0F);
		setResistance(1.0F);
		setBlockName("blockNetherOres");
		setStepSound(soundStoneFootstep);
		setRequiresSelfNotify();
	}
	
	public int getBlockTextureFromSideAndMetadata(int i, int j)
	{
		return j & 0x07;
	}
	
	@Override
    public int damageDropped(int i)
	{
    	if((i & 0x08) != 0)
    	{
    		i = i & ~0x08;
    	}
		
		return i;
	}
	
	@Override
	public int quantityDropped(Random random)
	{
		return 1;
	}
	
	@Override
	public void harvestBlock(World world, EntityPlayer entityplayer, int i, int j, int k, int l)
	{
    	if((l & 0x08) != 0)
    	{
    		l = l & ~0x08;
    	}
		
		super.harvestBlock(world, entityplayer, i, j, k, l);
		angerPigmen(entityplayer, world, i, j, k);
	}
	
	@Override
	public boolean removeBlockByPlayer(World world, EntityPlayer player, int i, int j, int k)
	{
	    checkExplosionChances(world, i, j, k);
		return super.removeBlockByPlayer(world, player, i, j, k);
	}
	
	@Override
    public void onBlockDestroyedByExplosion(World world, int i, int j, int k)
    {
	    checkExplosionChances(world, i, j, k);
    }
	
	private void checkExplosionChances(World world, int x, int y, int z)
	{
        for(int n = 0; n < NetherOresCore.getExplosionChance(); n++)
        {
            int tx = x - 1 + world.rand.nextInt(3);
            int ty = y - 1 + world.rand.nextInt(3);
            int tz = z - 1 + world.rand.nextInt(3);
            if(tx != x && ty != y && tz != z && world.getBlockId(tx, ty, tz) == this.blockID)
            {
                world.setBlockMetadataWithNotify(tx, ty, tz, world.getBlockMetadata(tx, ty, tz) | 0x08);
                world.scheduleBlockUpdate(tx, ty, tz, this.blockID, 75);
                world.playSoundEffect(x+0.5D, y+0.5D, z+0.5D, "random.fuse", 1.0F, 1.0F);
                break;
            }
        }
	}

    public void randomDisplayTick(World world, int i, int j, int k, Random random)
    {
    	if((world.getBlockMetadata(i, j, k) & 0x08) == 0)
    	{
    		return;
    	}
        double d = 0.0625D;
        for(int l = 0; l < 6; l++)
        {
            double d1 = (float)i + random.nextFloat();
            double d2 = (float)j + random.nextFloat();
            double d3 = (float)k + random.nextFloat();
            if(l == 0 && !world.isBlockOpaqueCube(i, j + 1, k))
            {
                d2 = (double)(j + 1) + d;
            }
            if(l == 1 && !world.isBlockOpaqueCube(i, j - 1, k))
            {
                d2 = (double)(j + 0) - d;
            }
            if(l == 2 && !world.isBlockOpaqueCube(i, j, k + 1))
            {
                d3 = (double)(k + 1) + d;
            }
            if(l == 3 && !world.isBlockOpaqueCube(i, j, k - 1))
            {
                d3 = (double)(k + 0) - d;
            }
            if(l == 4 && !world.isBlockOpaqueCube(i + 1, j, k))
            {
                d1 = (double)(i + 1) + d;
            }
            if(l == 5 && !world.isBlockOpaqueCube(i - 1, j, k))
            {
                d1 = (double)(i + 0) - d;
            }
            if(d1 < (double)i || d1 > (double)(i + 1) || d2 < 0.0D || d2 > (double)(j + 1) || d3 < (double)k || d3 > (double)(k + 1))
            {
                world.spawnParticle("smoke", d1, d2, d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    @Override
    public void updateTick(World world, int x, int y, int z, Random random)
    {
    	if((world.getBlockMetadata(x, y, z) & 0x08) > 0)
    	{
    		world.newExplosion(null, x + 0.5F, y + 0.5F, z + 0.5F, NetherOresCore.getExplosionPower(), true, true);
    	}
    }

	@Override
	public String getTextureFile()
	{
		return NetherOresCore.terrainTexture;
	}
	
	private void angerPigmen(EntityPlayer player, World world, int x, int y, int z)
	{
        List<?> list = world.getEntitiesWithinAABB(EntityPigZombie.class,
        		AxisAlignedBB.getBoundingBox(x - aggroRange, y - aggroRange, z - aggroRange, x + aggroRange + 1, y + aggroRange + 1, z + aggroRange + 1));
	    for(int j = 0; j < list.size(); j++)
	    {
	        Entity entity1 = (Entity)list.get(j);
	        if(entity1 instanceof EntityPigZombie)
	        {
	            EntityPigZombie entitypigzombie = (EntityPigZombie)entity1;
	            entitypigzombie.attackEntityFrom(DamageSource.causePlayerDamage(player), 0);
	        }
	    }
	}
}
