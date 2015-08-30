package atomicstryker.ic2.advancedmachines;

import ic2.api.recipe.RecipeOutput;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAdvancedOreWasher extends TileEntityOreWashing implements IAdvancedMachine
{

    private final CommonLogicAdvancedMachines advLogic;

    public TileEntityAdvancedOreWasher()
    {
        super();
        advLogic = new CommonLogicAdvancedMachines("%5d RPM", 1);
        advLogic.getOutputSlots().add(outputSlot);
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
    public void updateEntityServer()
    {
        super.updateEntityServer();
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
        fluidTank.drain(output.metadata.getInteger("amount"), true);
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
