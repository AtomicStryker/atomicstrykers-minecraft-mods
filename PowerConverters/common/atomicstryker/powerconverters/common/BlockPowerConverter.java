package atomicstryker.powerconverters.common;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockPowerConverter extends BlockContainer
{
	// 0 - engine gen lv
	// 1 - engine gen mv
	// 2 - engine gen hv
	// 3 - oil fab
	// 4 - energy link
	// 5 - lava fab
	// 6 - geo mk2
	// 7 - water strainer
	
	public BlockPowerConverter(int i)
	{
		super(i, 0, Material.clay);
		setHardness(1.0F);
		setBlockName("powerConverter");
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j)
	{
		if(j == 0) return PowerConverterCore.textureOffsetEngineGeneratorLV + i;
		if(j == 1) return PowerConverterCore.textureOffsetEngineGeneratorMV + i;
		if(j == 2) return PowerConverterCore.textureOffsetEngineGeneratorHV + i;
		if(j == 3) return PowerConverterCore.textureOffsetOilFabricator + i;
		if(j == 4) return PowerConverterCore.textureOffsetEnergyLinkDisconnected + i;
		if(j == 5) return PowerConverterCore.textureOffsetLavaFabricator + i;
		if(j == 6) return PowerConverterCore.textureOffsetGeomk2OffDisconnected + i;
		if(j == 7) return PowerConverterCore.textureOffsetWaterStrainerOffDisconnected + i;
		return blockIndexInTexture;
	}
	
    public int getBlockTexture(IBlockAccess iblockaccess, int x, int y, int z, int side)
    {
    	int meta = iblockaccess.getBlockMetadata(x, y, z);
		TileEntity te = iblockaccess.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityEnergyLink)
		{
			if(((TileEntityEnergyLink)te).isConnected(side))
			{
				return PowerConverterCore.textureOffsetEnergyLinkConnected + side;
			}
			else
			{
				return PowerConverterCore.textureOffsetEnergyLinkDisconnected + side;
			}
		}
		else if(te != null && te instanceof TileEntityGeoMk2)
		{
			TileEntityGeoMk2 geo = ((TileEntityGeoMk2)te);
			if(geo.isActive() && geo.isConnected(side))
			{
				return PowerConverterCore.textureOffsetGeomk2OnConnected + side;
			}
			else if(geo.isActive())
			{
				return PowerConverterCore.textureOffsetGeomk2OnDisconnected + side;
			}
			else if(!geo.isActive() && geo.isConnected(side))
			{
				return PowerConverterCore.textureOffsetGeomk2OffConnected + side;
			}
			else
			{
				return PowerConverterCore.textureOffsetGeomk2OffDisconnected + side;
			}
		}
		else if(te != null && te instanceof TileEntityWaterStrainer)
		{
			TileEntityWaterStrainer water = ((TileEntityWaterStrainer)te);
			if(water.isActive() && water.isConnected(side))
			{
				return PowerConverterCore.textureOffsetWaterStrainerOnConnected + side;
			}
			else if(water.isActive())
			{
				return PowerConverterCore.textureOffsetWaterStrainerOnDisconnected + side;
			}
			else if(!water.isActive() && water.isConnected(side))
			{
				return PowerConverterCore.textureOffsetWaterStrainerOffConnected + side;
			}
			else
			{
				return PowerConverterCore.textureOffsetWaterStrainerOffDisconnected + side;
			}
		}
		else
		{
			return getBlockTextureFromSideAndMetadata(side, meta);
		}
    }
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te == null)
		{
			return;
		}
		else if(te instanceof TileEntityEngineGenerator)
		{
			((TileEntityEngineGenerator)te).resetEnergyNetwork();
		}
	}

    @Override
    public TileEntity createNewTileEntity(World world, int md)
    {
		if(md == 0) return new TileEntityEngineGenerator(30, 100);
		if(md == 1) return new TileEntityEngineGenerator(120, 1000);
		if(md == 2) return new TileEntityEngineGenerator(510, 10000);
		if(md == 3) return new TileEntityOilFabricator();
		if(md == 4) return new TileEntityEnergyLink();
		if(md == 5) return new TileEntityLavaFabricator();
		if(md == 6) return new TileEntityGeoMk2();
		if(md == 7) return new TileEntityWaterStrainer();
		return createNewTileEntity(world);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}

	@Override
    public int damageDropped(int i)
	{
		return i;
	}

	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta == 6)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if(te == null || !(te instanceof TileEntityGeoMk2) || !((TileEntityGeoMk2)te).isActive())
			{
				return;
			}
			
			float xOffset = random.nextFloat() * (10.0F/16.0F) + (3.0F/16.0F);
			float zOffset = random.nextFloat() * (10.0F/16.0F) + (3.0F/16.0F);
			
			world.spawnParticle("smoke", x + xOffset, y + 1.1, z + zOffset, 0.0D, 0.0D, 0.0D);
			world.spawnParticle("flame", x + xOffset, y + 1, z + zOffset, 0.0D, 0.0D, 0.0D);
		}
		else if(meta == 7)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if(te == null || !(te instanceof TileEntityWaterStrainer) || !((TileEntityWaterStrainer)te).isActive())
			{
				return;
			}
			
			float xOffset = random.nextFloat() * (10.0F/16.0F) + (3.0F/16.0F);
			float zOffset = random.nextFloat() * (10.0F/16.0F) + (3.0F/16.0F);
			
			world.spawnParticle("splash", x + xOffset, y + 1.1, z + zOffset, 0.0D, 0.0D, 0.0D);
		}
	}
	
	@Override
	public boolean canProvidePower()
	{
		return true;
	}
	
	@Override
	public String getTextureFile()
	{
		return PowerConverterCore.terrainTexture;
	}
}
