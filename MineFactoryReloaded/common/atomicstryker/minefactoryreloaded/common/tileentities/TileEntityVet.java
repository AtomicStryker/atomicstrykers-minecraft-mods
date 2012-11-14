package atomicstryker.minefactoryreloaded.common.tileentities;

import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityMob;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemFood;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class TileEntityVet extends TileEntityFactoryInventory
{
	public TileEntityVet()
	{
		super(25, 25);
	}
	
	@Override
	protected int getHarvestRadius()
	{
		return 2;
	}
	
	@Override
	protected int getHarvestDistanceDown()
	{
		return 1;
	}
	
	@Override
	protected int getHarvestDistanceUp()
	{
		return 3;
	}

	@Override
	public String getInvName()
	{
		return "Veterinary";
	}

	@Override
	public void doWork()
	{
		if(!powerAvailable())
		{
			return;
		}
		
        List<?> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, getHarvestArea().toAxisAlignedBB());
        for(Object o : entities)
        {
            if(!(o instanceof EntityLiving) || o instanceof EntityPlayer || o instanceof EntityMob)
            {
                continue;
            }
            EntityLiving e = (EntityLiving)o;
            if (e.getHealth() < e.getMaxHealth())
            {
                for(int i = 0; i < getSizeInventory(); i++)
                {
                    ItemStack s = getStackInSlot(i);
                    int healAmount = getFeedHealthValue(s);
                    if(healAmount < 1)
                    {
                        continue;
                    }
                    
                    e.heal(healAmount);
                    decrStackSize(i, 1);
                    break;
                }
            }
        }
	}
	
	private int getFeedHealthValue(ItemStack stack)
	{
	    int result = 0;
	    
	    if (stack != null && stack.itemID < Item.itemsList.length)
	    {
	        Item item = Item.itemsList[stack.itemID];
	        if (item != null && item instanceof ItemFood)
	        {
	            result = ((ItemFood)item).getHealAmount();
	        }
	        else
	        {
	            if (stack.itemID == Item.wheat.shiftedIndex)
	            {
	                result = 2;
	            }
	            else if (stack.itemID == Block.carrot.blockID)
	            {
	                result = 2;
	            }
	            else if (stack.itemID == Block.potatoe.blockID)
	            {
	                result = 2;
	            }
	            else if (stack.itemID == Block.melon.blockID)
	            {
	                result = 3;
	            }
	            else if (stack.itemID == Block.pumpkin.blockID)
	            {
	                result = 6;
	            }
	        }
	    }
	    
	    return result;
	}

}
