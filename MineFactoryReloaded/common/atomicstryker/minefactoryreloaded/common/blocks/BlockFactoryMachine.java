package atomicstryker.minefactoryreloaded.common.blocks;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore;
import atomicstryker.minefactoryreloaded.common.MineFactoryReloadedCore.Machine;
import atomicstryker.minefactoryreloaded.common.core.Util;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityBlockBreaker;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityCollector;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactory;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactoryInventory;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactoryPowered;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFertilizer;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFisher;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityHarvester;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityPlanter;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityRancher;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityVet;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityWeather;
import buildcraft.api.transport.IPipeConnection;

public class BlockFactoryMachine extends BlockContainer implements IPipeConnection
{
	public static int[][] textures = new int[16][6];
	
	public BlockFactoryMachine(int i, int j)
	{
		super(i, j, Material.clay);
		setBlockName("blockFactoryMachine");
		setHardness(0.5F);
		setStepSound(soundMetalFootstep);
	}

	@Override
    public int getBlockTexture(IBlockAccess iblockaccess, int x, int y, int z, int side)
    {
    	int md = iblockaccess.getBlockMetadata(x, y, z);
    	TileEntity te = iblockaccess.getBlockTileEntity(x, y, z);
    	if(te instanceof TileEntityFactory)
    	{
    		return textures[md][((TileEntityFactory)te).getRotatedSide(side)];
    	}
    	return textures[md][side];
    }

	@Override
    public int getBlockTextureFromSideAndMetadata(int i, int j)
    {
        return textures[j][i];
    }

	@Override
    public int damageDropped(int i)
    {
        return i;
    }

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return null;
	}
	
	@Override
    public TileEntity createNewTileEntity(World world, int md)
	{
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Planter)) return new TileEntityPlanter();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fisher)) return new TileEntityFisher();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Harvester)) return new TileEntityHarvester();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Rancher)) return new TileEntityRancher();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Fertilizer)) return new TileEntityFertilizer();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Vet)) return new TileEntityVet();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Collector)) return new TileEntityCollector();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Breaker)) return new TileEntityBlockBreaker();
	    if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Weather)) return new TileEntityWeather();
	    return null;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		TileEntity te = world.getBlockTileEntity(i, j, k);
		if(Util.isHoldingWrench(entityplayer))
		{
			if(te instanceof TileEntityFactory && ((TileEntityFactory)te).canRotate())
			{
				((TileEntityFactory)te).rotate();
				world.markBlockForUpdate(i, j, k);
			}
		}
		else if(te != null && te instanceof TileEntityFactoryInventory && ((TileEntityFactoryInventory)te).getSizeInventory() == 27)
		{
			entityplayer.displayGUIChest((TileEntityFactoryInventory)te);
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, int par5, int par6)
	{
		TileEntity te = world.getBlockTileEntity(i, j, k);
		if(te == null || !(te instanceof IInventory))
		{
			return;
		}
		
		IInventory inventory = ((IInventory)te);
label0:
		for(int l = 0; l < inventory.getSizeInventory(); l++)
		{
			ItemStack itemstack = inventory.getStackInSlot(l);
			if(itemstack == null)
			{
				continue;
			}
			float f = world.rand.nextFloat() * 0.8F + 0.1F;
			float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
			float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
			do
			{
				if(itemstack.stackSize <= 0)
				{
					continue label0;
				}
				int i1 = world.rand.nextInt(21) + 10;
				if(i1 > itemstack.stackSize)
				{
					i1 = itemstack.stackSize;
				}
				itemstack.stackSize -= i1;
				EntityItem entityitem = new EntityItem(world, (float)i + f, (float)j + f1, (float)k + f2, new ItemStack(itemstack.itemID, i1, itemstack.getItemDamage()));
				float f3 = 0.05F;
				entityitem.motionX = (float)world.rand.nextGaussian() * f3;
				entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float)world.rand.nextGaussian() * f3;
				world.spawnEntityInWorld(entityitem);
			} while(true);
		}

		super.breakBlock(world, i, j, k, par5, par6);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l)
	{
		TileEntity te = world.getBlockTileEntity(i, j, k);
		if(te != null && te instanceof TileEntityFactoryPowered)
		{
			((TileEntityFactoryPowered)te).neighborBlockChanged();
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityCollector)
		{
			float shrinkAmount = 0.125F;
			return AxisAlignedBB.getBoundingBox(x + shrinkAmount, y + shrinkAmount, z + shrinkAmount,
					x + 1 - shrinkAmount, y + 1 - shrinkAmount, z + 1 - shrinkAmount);
		}
		else
		{
			return super.getCollisionBoundingBoxFromPool(world, x, y, z);
		}
	}

	@Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
    {
		int md = world.getBlockMetadata(x, y, z);
		if(md == MineFactoryReloadedCore.machineMetadataMappings.get(Machine.Collector))
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if(te != null && te instanceof TileEntityCollector && entity instanceof EntityItem)
			{
				((TileEntityCollector)te).addToChests((EntityItem)entity);
			}
		}
		super.onEntityCollidedWithBlock(world, x, y, z, entity);
    }

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

    @Override
    public boolean isPipeConnected(ForgeDirection with)
    {
        return true;
    }

	@Override
	public String getTextureFile()
	{
        return MineFactoryReloadedCore.terrainTexture;
	}
	
}
