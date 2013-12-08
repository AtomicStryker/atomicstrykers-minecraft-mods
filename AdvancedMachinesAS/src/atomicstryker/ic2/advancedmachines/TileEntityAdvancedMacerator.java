package atomicstryker.ic2.advancedmachines;

import ic2.api.item.Items;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.tileentity.TileEntityMacerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAdvancedMacerator extends TileEntityMacerator implements IAdvancedMachine
{
    public static final int SUPPLEMENT_SLOT_INDEX = 8;
    
    private final CommonLogicAdvancedMachines advLogic;
    
    public int supplementedItemsLeft = 0;
    private int nextSupplementResultCount;
    
    private final ItemStack idCopperOreCrushed;
    private final ItemStack idTinOreCrushed;
    private final ItemStack idCoalDust;
    private final ItemStack idWaterCell;
    private final ItemStack bronzeDust;
    private final ItemStack hydratedCoalDust;
    private final ItemStack twoQuartz;
    
    private final InvSlot supplementSlot;

    public TileEntityAdvancedMacerator()
    {
        super();
        advLogic = new CommonLogicAdvancedMachines("%5d RPM", 1);
        advLogic.getOutputSlots().add(outputSlot);
        advLogic.getOutputSlots().add(new InvSlotOutput(this, "outputextra1", 4, 1));
        supplementSlot = new InvSlot(this, "supplement", SUPPLEMENT_SLOT_INDEX, InvSlot.Access.I, 1);
        
        idCopperOreCrushed = Items.getItem("crushedCopperOre");
        idTinOreCrushed = Items.getItem("crushedTinOre");
        idCoalDust = Items.getItem("coalDust");
        idWaterCell = Items.getItem("waterCell");
        bronzeDust = Items.getItem("bronzeDust");
        hydratedCoalDust = Items.getItem("hydratedCoalDust");
        twoQuartz = new ItemStack(Item.netherQuartz, 2);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtt)
    {
        super.readFromNBT(nbtt);
        advLogic.readFromNBT(nbtt);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbtt)
    {
        super.writeToNBT(nbtt);
        advLogic.writeToNBT(nbtt);
    }
    
    @Override
    public void updateEntity()
    {
        super.updateEntity();
        advLogic.updateEntity(this);
    }
    
    @Override
    public void setOverclockRates()
    {
        super.setOverclockRates();
        advLogic.setOverclockRates(this);
    }
    
    @Override
    public RecipeOutput getOutput()
    {
        RecipeOutput output = advLogic.getOutput(this);
        if (!inputSlot.isEmpty())
        {
            if (output == null)
            {
                output = new RecipeOutput(new NBTTagCompound(), new ArrayList<ItemStack>());
            }
            ArrayList<ItemStack> outItems = new ArrayList<ItemStack>(output.items);
            runSupplementLogic(outItems, false);
            if (outItems.isEmpty())
            {
                output = null;
            }
        }
        return output;
    }
    
    private void runSupplementLogic(List<ItemStack> items, boolean consumeInput)
    {
        ItemStack supplement = (supplementSlot.get() != null) ? supplementSlot.get().copy() : null;
        if(supplement != null)
        {
            ArrayList<ItemStack> additions = new ArrayList<ItemStack>();
            if (items.isEmpty())
            {
                additions.addAll(getSpecialResultFor(inputSlot.get(), null, supplement, consumeInput));
                if (!additions.isEmpty() && supplementedItemsLeft == 0)
                {
                    supplementedItemsLeft = nextSupplementResultCount;
                }
            }
            else
            {
                ItemStack result = null;
                Iterator<ItemStack> iter = items.iterator();
                while (iter.hasNext())
                {
                    result = iter.next();
                    if (supplementedItemsLeft > 0)
                    {
                        additions.addAll(getSpecialResultFor(inputSlot.get(), result, supplement, consumeInput));
                        iter.remove();
                    }
                    else if (getSpecialResultFor(inputSlot.get(), result, supplement, consumeInput) != null)
                    {
                        additions.addAll(getSpecialResultFor(inputSlot.get(), result, supplement, consumeInput));
                        supplementedItemsLeft = nextSupplementResultCount;
                        iter.remove();
                    }
                }
            }
            
            items.addAll(additions);
        }
    }

    private List<ItemStack> getSpecialResultFor(ItemStack original, ItemStack result, ItemStack supplement, boolean removeInputs)
    {
        ArrayList<ItemStack> results = new ArrayList<ItemStack>();
        
        if(result != null && supplement != null)
        {
            if (result.isItemEqual(idCopperOreCrushed) && Recipes.macerator.getOutputFor(supplement, removeInputs) != null)
            {
                for (ItemStack i : Recipes.macerator.getOutputFor(supplement, removeInputs).items)
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
        }
        
        return results;
    }
    
    private boolean isWater(ItemStack item)
    {
        return item.itemID == Item.bucketWater.itemID || item.isItemEqual(idWaterCell) || item.itemID == Block.ice.blockID;
    }

    @Override
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult)
    {        
        advLogic.operateOnce(this, output, processResult);
        
        if (supplementedItemsLeft > 0)
        {
            if (supplementedItemsLeft == 1)
            {
                if (--supplementSlot.get().stackSize == 0)
                {
                    supplementSlot.clear();
                }
            }
            supplementedItemsLeft--;
        }
    }
    
    @Override
    public String printFormattedData()
    {
        return advLogic.printFormattedData();
    }

    @Override
    public int getSpeed()
    {
        return advLogic.getSpeed();
    }

    @Override
    public void setClientSpeed(int value)
    {
        advLogic.setClientSpeed(value);
    }

    @Override
    public ArrayList<InvSlotOutput> getOutputSlots()
    {
        return advLogic.getOutputSlots();
    }

}
