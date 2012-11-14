package atomicstryker.minefactoryreloaded.common.tileentities;

import atomicstryker.minefactoryreloaded.common.core.IRotateableTile;
import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class TileEntityConveyor extends TileEntity implements IRotateableTile
{
	@Override
	public void rotate()
	{
		int md = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(md == 0)
		{
			int nextBlockId = worldObj.getBlockId(xCoord + 1, yCoord, zCoord);
			int prevBlockId = worldObj.getBlockId(xCoord - 1, yCoord, zCoord);
			if(Block.blocksList[nextBlockId] != null && Block.blocksList[nextBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 4);
			}
			else if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 8);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 1);
			}
		}
		else if(md == 4)
		{
			int prevBlockId = worldObj.getBlockId(xCoord - 1, yCoord, zCoord);
			if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 8);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 1);
			}
		}
		else if(md == 8)
		{
			rotateTo(worldObj, xCoord, yCoord, zCoord, 1);
		}
		

		if(md == 1)
		{
			int nextBlockId = worldObj.getBlockId(xCoord, yCoord, zCoord + 1);
			int prevBlockId = worldObj.getBlockId(xCoord, yCoord, zCoord - 1);
			if(Block.blocksList[nextBlockId] != null && Block.blocksList[nextBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 5);
			}
			else if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 9);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 2);
			}
		}
		else if(md == 5)
		{
			int prevBlockId = worldObj.getBlockId(xCoord, yCoord, zCoord - 1);
			if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 9);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 2);
			}
		}
		else if(md == 9)
		{
			rotateTo(worldObj, xCoord, yCoord, zCoord, 2);
		}
		

		if(md == 2)
		{
			int nextBlockId = worldObj.getBlockId(xCoord - 1, yCoord, zCoord);
			int prevBlockId = worldObj.getBlockId(xCoord + 1, yCoord, zCoord);
			if(Block.blocksList[nextBlockId] != null && Block.blocksList[nextBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 6);
			}
			else if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 10);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 3);
			}
		}
		else if(md == 6)
		{
			int prevBlockId = worldObj.getBlockId(xCoord + 1, yCoord, zCoord);
			if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 10);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 3);
			}
		}
		else if(md == 10)
		{
			rotateTo(worldObj, xCoord, yCoord, zCoord, 3);
		}
		

		if(md == 3)
		{
			int nextBlockId = worldObj.getBlockId(xCoord, yCoord, zCoord - 1);
			int prevBlockId = worldObj.getBlockId(xCoord, yCoord, zCoord + 1);
			if(Block.blocksList[nextBlockId] != null && Block.blocksList[nextBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 7);
			}
			else if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 11);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 0);
			}
		}
		else if(md == 7)
		{
			int prevBlockId = worldObj.getBlockId(xCoord, yCoord, zCoord + 1);
			if(Block.blocksList[prevBlockId] != null && Block.blocksList[prevBlockId].isOpaqueCube())
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 11);
			}
			else
			{
				rotateTo(worldObj, xCoord, yCoord, zCoord, 0);
			}
		}
		else if(md == 11)
		{
			rotateTo(worldObj, xCoord, yCoord, zCoord, 0);
		}
	}
	
	private void rotateTo(World world, int xCoord, int yCoord, int zCoord, int newmd)
	{
		world.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newmd);
	}

	@Override
	public boolean canRotate()
	{
		return true;
	}
}
