package atomicstryker.ic2.advancedmachines;

import ic2.api.item.IC2Items;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.tileentity.TileEntityMacerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAdvancedMacerator extends TileEntityMacerator implements IAdvancedMachine
{
    public static final int MAIN_IN_SLOT_INDEX = 0;
    public static final int SUPPLEMENT_SLOT_INDEX = 8;
    public static final int EXTRA_OUT_SLOT_INDEX = 4;
    
    private final CommonLogicAdvancedMachines advLogic;
    
    public int supplementedItemsLeft = 0;
    private int nextSupplementResultCount;
    private int nextSupplementResourceDrain;
    
    private final ItemStack idCopperOreCrushed;
    private final ItemStack idTinOreCrushed;
    private final ItemStack idCoalDust;
    private final ItemStack idWaterCell;
    private final ItemStack bronzeDust;
    private final ItemStack hydratedCoalDust;
    private final ItemStack twoQuartz;
    
    private final ItemStack waterBucket;
    private final ItemStack quartzOre;
    private final ItemStack sand;
    private final ItemStack netherrack;
    private final ItemStack ice;
    private final ItemStack redstone;
    
    private final InvSlot supplementSlot;

    public TileEntityAdvancedMacerator()
    {
        super();
        advLogic = new CommonLogicAdvancedMachines("%5d RPM", 1);
        advLogic.getOutputSlots().add(outputSlot);
        advLogic.getOutputSlots().add(new InvSlotOutput(this, "outputextra1", EXTRA_OUT_SLOT_INDEX, 1));
        supplementSlot = new InvSlot(this, "supplement", SUPPLEMENT_SLOT_INDEX, InvSlot.Access.I, 1);
        
        idCopperOreCrushed = IC2Items.getItem("crushedCopperOre");
        idTinOreCrushed = IC2Items.getItem("crushedTinOre");
        idCoalDust = IC2Items.getItem("coalDust");
        idWaterCell = IC2Items.getItem("waterCell");
        bronzeDust = IC2Items.getItem("bronzeDust");
        hydratedCoalDust = IC2Items.getItem("hydratedCoalDust");
        twoQuartz = new ItemStack(Items.quartz, 2);
        
        waterBucket = new ItemStack(Items.water_bucket);
        quartzOre = new ItemStack(Blocks.quartz_ore);
        sand = new ItemStack(Blocks.sand);
        netherrack = new ItemStack(Blocks.netherrack);
        ice = new ItemStack(Blocks.ice);
        redstone = new ItemStack(Items.redstone);
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
        
        // is second output is not empty but primary is, swap output slot contents
        if (!advLogic.getOutputSlots().get(1).isEmpty() && advLogic.getOutputSlots().get(0).isEmpty())
        {
            advLogic.getOutputSlots().get(0).put(advLogic.getOutputSlots().get(1).get());
            advLogic.getOutputSlots().get(1).put(null);
        }
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
            runSupplementLogic(outItems);
            if (outItems.isEmpty())
            {
                output = null;
            }
            else
            {
                output = new RecipeOutput(new NBTTagCompound(), outItems);
            }
        }
        return output;
    }
    
    private void runSupplementLogic(List<ItemStack> items)
    {
        ItemStack supplement = (supplementSlot.get() != null) ? supplementSlot.get().copy() : null;
        if(supplement != null)
        {
            ArrayList<ItemStack> additions = new ArrayList<ItemStack>();
            if (items.isEmpty())
            {
                additions.addAll(getSpecialResultFor(inputSlot.get(), null, supplement));
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
                        additions.addAll(getSpecialResultFor(inputSlot.get(), result, supplement));
                        iter.remove();
                    }
                    else if (getSpecialResultFor(inputSlot.get(), result, supplement) != null)
                    {
                        additions.addAll(getSpecialResultFor(inputSlot.get(), result, supplement));
                        supplementedItemsLeft = nextSupplementResultCount;
                        iter.remove();
                    }
                }
            }
            
            items.addAll(additions);
        }
    }

    private List<ItemStack> getSpecialResultFor(ItemStack original, ItemStack result, ItemStack supplement)
    {
        ArrayList<ItemStack> results = new ArrayList<ItemStack>();
        
        if(result != null && supplement != null)
        {
            RecipeOutput suppOut = Recipes.macerator.getOutputFor(supplement, false);
            if (result.isItemEqual(idCopperOreCrushed) && suppOut != null)
            {
                for (ItemStack i : suppOut.items)
                {
                    if (i.isItemEqual(idTinOreCrushed))
                    {
                        nextSupplementResultCount = 4;
                        results.add(new ItemStack(bronzeDust.getItem(), result.stackSize));
                        break;
                    }
                }
            }
            else if (original.isItemEqual(quartzOre) && supplement.isItemEqual(sand))
            {
                nextSupplementResultCount = 1;
                results.add(twoQuartz);
            }
            else if (original.isItemEqual(netherrack) && supplement.isItemEqual(redstone))
            {
                if (original.stackSize > 7)
                {
                    nextSupplementResultCount = 1;
                    results.add(new ItemStack(Items.glowstone_dust, 1));
                    nextSupplementResourceDrain = 6; // to pull 6 additional netherrack for a total cost of 7
                }
            }
            else if (result.isItemEqual(idCoalDust) && isWater(supplement))
            {
                nextSupplementResultCount = 8;
                results.add(hydratedCoalDust);
            }
        }
        
        return results;
    }
    
    private boolean isWater(ItemStack item)
    {
        return item.isItemEqual(waterBucket) || item.isItemEqual(idWaterCell) || item.isItemEqual(ice);
    }

    @Override
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult)
    {        
        advLogic.operateOnce(this, output, processResult);
        
        if (supplementedItemsLeft > 0)
        {
            if (supplementSlot.get() == null)
            {
                supplementedItemsLeft = 0;
            }
            else
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
            
            if (nextSupplementResourceDrain > 0)
            {
                inputSlot.get().stackSize -= nextSupplementResourceDrain;
                nextSupplementResourceDrain = 0;
            }
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
    
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemStack)
    {
        if (slot == SUPPLEMENT_SLOT_INDEX)
        {
            return false;
        }
        return super.isItemValidForSlot(slot, itemStack);
    }
    
    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int blockSide)
    {
        if (slot == SUPPLEMENT_SLOT_INDEX)
        {
            return false;
        }
        else if (slot == EXTRA_OUT_SLOT_INDEX)
        {
            return true;
        }
        return super.canExtractItem(slot, itemstack, blockSide);
    }

}
