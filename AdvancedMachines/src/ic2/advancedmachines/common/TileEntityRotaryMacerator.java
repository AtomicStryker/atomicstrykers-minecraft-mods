package ic2.advancedmachines.common;

import ic2.api.item.Items;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TileEntityRotaryMacerator extends TileEntityAdvancedMachine
{
    
    private final int SUPPLEMENT_SLOT_INDEX = 8;
    
	public int supplementedItemsLeft = 0;
	private int nextSupplementResultCount;
	
	private ItemStack idCopperOreCrushed;
	private ItemStack idTinOreCrushed;
	private ItemStack idCoalDust;
	private ItemStack idWaterCell;
	private ItemStack bronzeDust;
	private ItemStack hydratedCoalDust;
	private ItemStack twoQuartz;
	
    public TileEntityRotaryMacerator()
    {
        super("Rotary Macerator", "%5d RPM", 1, new int[] {1}, new int[] {2, 3});
        
        idCopperOreCrushed = Items.getItem("crushedCopperOre");
        idTinOreCrushed = Items.getItem("crushedTinOre");
        idCoalDust = Items.getItem("coalDust");
        idWaterCell = Items.getItem("waterCell");
        bronzeDust = Items.getItem("bronzeDust");
        hydratedCoalDust = Items.getItem("hydratedCoalDust");
        
        twoQuartz = new ItemStack(Item.netherQuartz, 2);
    }

    @Override
    public Container getGuiContainer(InventoryPlayer var1)
    {
        return new ContainerRotaryMacerator(var1, this);
    }

    @Override
    public List<ItemStack> getResultFor(ItemStack macerated, boolean removeInputs)
    {
        RecipeOutput output = Recipes.macerator.getOutputFor(macerated, removeInputs);
        ArrayList<ItemStack> results = output == null ? new ArrayList<ItemStack>() : new ArrayList<ItemStack>(output.items);
        
        ItemStack supplement = (inventory[SUPPLEMENT_SLOT_INDEX] != null) ? inventory[SUPPLEMENT_SLOT_INDEX].copy() : null;
        if(supplement != null)
        {
            ArrayList<ItemStack> additions = new ArrayList<ItemStack>();
            
            if (results.isEmpty())
            {
                additions.addAll(getSpecialResultFor(macerated, null, supplement, removeInputs));
                if (!additions.isEmpty() && supplementedItemsLeft == 0)
                {
                    supplementedItemsLeft = nextSupplementResultCount;
                }
            }
            else
            {
                ItemStack result = null;
                Iterator<ItemStack> iter = results.iterator();
                while (iter.hasNext())
                {
                    result = iter.next();
                    if (supplementedItemsLeft > 0)
                    {
                        additions.addAll(getSpecialResultFor(macerated, result, supplement, removeInputs));
                        iter.remove();
                    }
                    else if (getSpecialResultFor(macerated, result, supplement, removeInputs) != null)
                    {
                        additions.addAll(getSpecialResultFor(macerated, result, supplement, removeInputs));
                        supplementedItemsLeft = nextSupplementResultCount;
                        iter.remove();
                    }
                }
            }
            
            results.addAll(additions);
        }
        
        return results.isEmpty() ? null : results; // expects null not an empty list
    }
    
    @Override
    public void onFinishedProcessingItem()
    {
        super.onFinishedProcessingItem();
    	if (supplementedItemsLeft > 0)
    	{
    		if (supplementedItemsLeft == 1)
    		{
    			inventory[SUPPLEMENT_SLOT_INDEX].stackSize--;
    			if (inventory[SUPPLEMENT_SLOT_INDEX].stackSize == 0)
    			{
    				inventory[SUPPLEMENT_SLOT_INDEX] = null;
    			}
    		}
    		supplementedItemsLeft--;
    	}
    }
    
    private List<ItemStack> getSpecialResultFor(ItemStack original, ItemStack result, ItemStack supplement, boolean removeInputs)
    {
        ArrayList<ItemStack> results = new ArrayList<ItemStack>();
        
    	if(result != null && supplement != null)
    	{
    		List<ItemStack> supplementOutput = Recipes.macerator.getOutputFor(supplement, removeInputs).items;
    		
    		if (result.isItemEqual(idCopperOreCrushed) && supplementOutput != null)
    		{
    		    for (ItemStack i : supplementOutput)
    		    {
    		        if (i.isItemEqual(idTinOreCrushed))
    		        {
    		            nextSupplementResultCount = 4;
    	                results.add(new ItemStack(bronzeDust.getItem(), result.stackSize));
    	                break;
    		        }
    		    }
    		}
    		else if (result.isItemEqual(idCoalDust) && isWater(supplement))
    		{
    			nextSupplementResultCount = 8;
    			results.add(hydratedCoalDust);
    		}
    	}
    	else if (supplement != null)
    	{
    	    if (original.getItem().itemID == Block.oreNetherQuartz.blockID && supplement.itemID == Block.sand.blockID)
    	    {
    	        nextSupplementResultCount = 1;
    	        results.add(twoQuartz);
    	    }
    	    else if (original.getItem().itemID == Block.netherrack.blockID && supplement.itemID == Item.redstone.itemID)
            {
    	        if (original.stackSize > 7)
    	        {
    	            nextSupplementResultCount = 1;
                    results.add(new ItemStack(Item.glowstone, 1));
                    if (removeInputs)
                    {
                        original.stackSize -= 7;
                    }
    	        }
            }
    	    
    	    if (removeInputs)
    	    {
    	        if (--inventory[inputs[0]].stackSize < 1)
    	        {
    	            inventory[inputs[0]] = null;
    	        }
    	    }
    	}
    	
		return results;
	}
    
    private boolean isWater(ItemStack item)
    {
        return item.itemID == Item.bucketWater.itemID || item.isItemEqual(idWaterCell) || item.itemID == Block.ice.blockID;
    }

    @Override
    public int getUpgradeSlotsStartSlot()
    {
    	return 4;
    }

    @Override
	public String getStartSoundFile()
    {
    	return AdvancedMachines.advMaceSound;
    }

    @Override
    public String getInterruptSoundFile()
    {
    	return AdvancedMachines.interruptSound;
    }
}
