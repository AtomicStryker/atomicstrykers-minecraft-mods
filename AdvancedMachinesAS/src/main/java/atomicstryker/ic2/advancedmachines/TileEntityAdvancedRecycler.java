package atomicstryker.ic2.advancedmachines;

import ic2.api.recipe.RecipeOutput;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.tileentity.TileEntityRecycler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAdvancedRecycler extends TileEntityRecycler implements IAdvancedMachine
{
    
    private final CommonLogicAdvancedMachines advLogic;

    public TileEntityAdvancedRecycler()
    {
        super();
        advLogic = new CommonLogicAdvancedMachines("%5d cm3/s", 1);
        advLogic.getOutputSlots().add(outputSlot);
        advLogic.getOutputSlots().add(new InvSlotOutput(this, "outputextra1", 4, 1));
        advLogic.getOutputSlots().add(new InvSlotOutput(this, "outputextra2", 5, 1));
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
        return advLogic.getOutput(this);
    }

    @Override
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult)
    {
        advLogic.operateOnce(this, output, processResult);
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
