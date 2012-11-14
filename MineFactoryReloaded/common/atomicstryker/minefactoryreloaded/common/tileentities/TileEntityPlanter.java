package atomicstryker.minefactoryreloaded.common.tileentities;

import java.util.HashMap;
import java.util.Map;

import atomicstryker.minefactoryreloaded.common.api.IFactoryPlantable;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class TileEntityPlanter extends TileEntityFactoryInventory
{
	private static Map<Integer, IFactoryPlantable> plantables = new HashMap<Integer, IFactoryPlantable>();
	
	public TileEntityPlanter() 
	{
	    super(1, 1);
	}
	
    public static void registerPlantable(IFactoryPlantable plantable)
    {
    	plantables.put(new Integer(plantable.getSourceId()), plantable);
    }

    public int getFirstStack()
    {
        for(int i = 0; i < inventory.length; i++)
        {
            if(inventory[i] != null)
            {
                return i;
            }
        }
        return -1;
    }

    public String getInvName()
    {
        return "Planter";
    }

    public void doWork()
    {
		if(!powerAvailable())
		{
			return;
		}
		
        int currentXoffset;
        int currentZoffset;
        
        for(currentXoffset = -1; currentXoffset <= 1; currentXoffset++)
        {
        	for(currentZoffset = -1; currentZoffset <=1; currentZoffset++)
        	{
        		for(int stackIndex = 0; stackIndex < getSizeInventory(); stackIndex++)
        		{
        			if(inventory[stackIndex] == null)
        			{
        				continue;
        			}
        			
	        		ItemStack availableStack = getStackInSlot(stackIndex);
        			if(!plantables.containsKey(new Integer(availableStack.itemID)))
        			{
        				continue;
        			}
	        		IFactoryPlantable plantable = plantables.get(new Integer(availableStack.itemID));
	
	    			if(!plantable.canBePlantedHere(worldObj, xCoord + currentXoffset, yCoord + 2, zCoord + currentZoffset, availableStack))
	    			{
	    				continue;
	    			}
	    			plantable.prePlant(worldObj, xCoord + currentXoffset, yCoord + 2, zCoord + currentZoffset, availableStack);
	    			worldObj.setBlockAndMetadataWithNotify(xCoord + currentXoffset, yCoord + 2, zCoord + currentZoffset,
	    					plantable.getPlantedBlockId(worldObj, xCoord + currentXoffset, yCoord + 2, zCoord + currentZoffset, availableStack),
	    					plantable.getPlantedBlockMetadata(worldObj, xCoord + currentXoffset, yCoord + 2, zCoord + currentZoffset, availableStack));
	                decrStackSize(stackIndex, 1);
	                plantable.postPlant(worldObj, xCoord + currentXoffset, yCoord + 2, zCoord + currentZoffset);
	                return;
        		}
        	}
        }
    }
    
}
