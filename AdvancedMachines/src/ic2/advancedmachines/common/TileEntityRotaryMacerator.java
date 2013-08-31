package ic2.advancedmachines.common;

import ic2.api.item.Items;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TileEntityRotaryMacerator extends TileEntityAdvancedMachine
{
	public int supplementedItemsLeft = 0;
	private int currentResultCount;
	
	private ItemStack idIronOreCrushed;
	private ItemStack idCopperOreCrushed;
	private ItemStack idTinOreCrushed;
	private ItemStack idCoalDust;
	private ItemStack idWaterCell;
	private ItemStack bronzeDust;
	private ItemStack hydratedCoalDust;
	
    public TileEntityRotaryMacerator()
    {
        super("Rotary Macerator", "%5d RPM", 1, new int[] {1}, new int[] {2, 3});
        
        idIronOreCrushed = Items.getItem("crushedIronOre");
        idCopperOreCrushed = Items.getItem("crushedCopperOre");
        idTinOreCrushed = Items.getItem("crushedTinOre");
        idCoalDust = Items.getItem("coalDust");
        idWaterCell = Items.getItem("waterCell");
        bronzeDust = Items.getItem("bronzeDust");
        hydratedCoalDust = Items.getItem("hydratedCoalDust");
    }

    @Override
    public Container getGuiContainer(InventoryPlayer var1)
    {
        return new ContainerRotaryMacerator(var1, this);
    }

    @Override
    public List<ItemStack> getResultFor(ItemStack macerated, boolean adjustOutput)
    {
        RecipeOutput output = Recipes.macerator.getOutputFor(macerated, adjustOutput);
        if (output == null)
        {
            return null;
        }
        ArrayList<ItemStack> results = new ArrayList<ItemStack>(output.items);
        
        ItemStack supplement = (inventory[8] != null) ? inventory[8].copy() : null;
        if(supplement != null)
        {
            ArrayList<ItemStack> additions = new ArrayList<ItemStack>();
            
            ItemStack result = null;
            Iterator<ItemStack> iter = results.iterator();
            while (iter.hasNext())
            {
                result = iter.next();
                if (supplementedItemsLeft > 0)
                {
                    additions.addAll(getSpecialResultFor(macerated, result, supplement, adjustOutput));
                    iter.remove();
                }
                else if (getSpecialResultFor(macerated, result, supplement, adjustOutput) != null)
                {
                    additions.addAll(getSpecialResultFor(macerated, result, supplement, adjustOutput));
                    supplementedItemsLeft = currentResultCount;
                    iter.remove();
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
    	if (supplementedItemsLeft != 0)
    	{
    		if (supplementedItemsLeft == 1)
    		{
    			inventory[8].stackSize--;
    			if (inventory[8].stackSize == 0)
    			{
    				inventory[8] = null;
    			}
    		}
    		supplementedItemsLeft--;
    	}
		
    	super.onFinishedProcessingItem();
    }
    
    private List<ItemStack> getSpecialResultFor(ItemStack original, ItemStack result, ItemStack supplement, boolean bool)
    {
        ArrayList<ItemStack> results = new ArrayList<ItemStack>();
        
    	if(result != null && supplement != null)
    	{
    		List<ItemStack> supplementOutput = Recipes.macerator.getOutputFor(supplement, bool).items;
    		
    		if (result.isItemEqual(idIronOreCrushed) && supplement.itemID == Item.coal.itemID)
    		{
    			currentResultCount = 128;
    			results.add(new ItemStack(AdvancedMachines.refinedIronDust, result.stackSize));
    		}
    		else if (result.isItemEqual(idCopperOreCrushed) && supplementOutput != null)
    		{
    		    for (ItemStack i : supplementOutput)
    		    {
    		        if (i.isItemEqual(idTinOreCrushed))
    		        {
    		            currentResultCount = 4;
    	                results.add(new ItemStack(bronzeDust.getItem(), result.stackSize));
    	                break;
    		        }
    		    }
    		}
    		else if (result.isItemEqual(idCoalDust) && supplement.isItemEqual(idWaterCell))
    		{
    			currentResultCount = 8;
    			results.add(hydratedCoalDust);
    		}
    	}
    	
		return results;
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
