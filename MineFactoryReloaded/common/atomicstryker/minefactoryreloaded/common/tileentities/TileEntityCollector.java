package atomicstryker.minefactoryreloaded.common.tileentities;

import java.util.List;

import atomicstryker.minefactoryreloaded.common.core.Util;

import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class TileEntityCollector extends TileEntityFactory
{    
	@Override
	public boolean canRotate()
	{
		return false;
	}
	
	public void addToChests(EntityItem i)
	{
		ItemStack s = i.item;
		List<IInventory> chests = Util.findChests(worldObj, xCoord, yCoord, zCoord);
		for(IInventory inv : chests)
		{
			s.stackSize = Util.addToInventory(inv, s);
			if(s.stackSize == 0)
			{
				i.setDead();
				return;
			}
		}
	}
}
