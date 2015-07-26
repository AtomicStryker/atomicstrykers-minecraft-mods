package atomicstryker.ic2.advancedmachines;

import ic2.api.recipe.RecipeOutput;
import ic2.core.block.TileEntityLiquidTankStandardMaschine;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.item.ItemUpgradeModule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CommonLogicAdvancedMachines implements IAdvancedMachine
{

    private ModAdvancedMachines mod;
    private final ArrayList<InvSlotOutput> outSlotList;

    private boolean runningEmpty;
    private int speed;
    private final String dataFormat;
    private final int dataScaling;
    private final RecipeOutput dummyOut = new RecipeOutput(new NBTTagCompound(), new ItemStack(Blocks.dirt));

    private static boolean failed;
    private static Field progressField;

    public CommonLogicAdvancedMachines(String dF, int dS)
    {
        mod = ModAdvancedMachines.instance;
        outSlotList = new ArrayList<InvSlotOutput>();
        speed = 0;
        dataFormat = dF;
        dataScaling = dS;

        if (progressField == null && !failed)
        {
            try
            {
                progressField = TileEntityStandardMachine.class.getDeclaredField("progress");
                progressField.setAccessible(true);
            }
            catch (Exception e)
            {
                System.out.println("Advanced Machines failed hacking into IC2 TileEntityStandardMachine");
                e.printStackTrace();
                failed = true;
            }
        }
    }

    @Override
    public String printFormattedData()
    {
        return String.format(this.dataFormat, new Object[] { Integer.valueOf(speed * dataScaling) });
    }

    @Override
    public int getSpeed()
    {
        return speed;
    }

    @Override
    public void setClientSpeed(int value)
    {
        speed = value;
    }

    public void readFromNBT(NBTTagCompound nbtt)
    {
        try
        {
            speed = nbtt.getInteger("speed");
        }
        catch (Exception e)
        {
        }
    }

    public void writeToNBT(NBTTagCompound nbtt)
    {
        nbtt.setInteger("speed", speed);
    }

    public RecipeOutput getOutput(TileEntityStandardMachine te)
    {
        RecipeOutput ic2Output = null;
        boolean canStash = false;
        if (!te.inputSlot.isEmpty())
        {
            ic2Output = te.inputSlot.process();
            if (ic2Output != null)
            {
                for (int index = 0; index < outSlotList.size(); index++)
                {
                    if (outSlotList.get(index).canAdd(ic2Output.items))
                    {
                        canStash = true;
                        break;
                    }
                }
            }
        }
        
        if (!canStash)
        {
            ic2Output = null;
        }
        
        if (ic2Output != null && te instanceof TileEntityLiquidTankStandardMaschine)
        {
            if (ic2Output.metadata != null
            && ic2Output.metadata.getInteger("amount") > ((TileEntityLiquidTankStandardMaschine) te).getFluidTank().getFluidAmount())
            {
                ic2Output = null;
            }
        }

        if (ic2Output != null)
        {
            runningEmpty = false;
            return ic2Output;
        }

        if (te.hasWorldObj() && te.getWorldObj().isBlockIndirectlyGettingPowered(te.xCoord, te.yCoord, te.zCoord))
        {
            runningEmpty = true;
            return dummyOut;
        }

        return null;
    }
    
    public void operateOnce(TileEntityStandardMachine te, RecipeOutput output, List<ItemStack> processResult)
    {
        if (!te.inputSlot.isEmpty())
        {
            te.inputSlot.consume();
            for (InvSlotOutput slot : getOutputSlots())
            {
                if (slot.canAdd(processResult))
                {
                    slot.add(processResult);
                    break;
                }
            }
        }
    }

    public void updateEntity(TileEntityStandardMachine te)
    {
        try
        {
            if (progressField != null && !te.getWorldObj().isRemote)
            {
                if (te.getActive())
                {
                    speed = Math.min(mod.maxMachineSpeedUpTicks, speed + 1);
                }
                else
                {
                    speed = Math.max(0, speed - 1);
                }

                if (runningEmpty)
                {
                    progressField.set(te, (short) 0);
                }
                else
                {
                    // progress typically goes from 0(start) to
                    // 400,500(operation end) and gets incremented by 1 every
                    // tick
                    // additionally, setOverClockRates may mess with it and
                    // further increment it
                    // the goal here is to increase the base processing speed to
                    // up to N*100% by calculating the speed
                    short extraprogress = (short) Math.round(speed / (mod.maxMachineSpeedUpTicks / (mod.maxMachineSpeedUpFactor - 1)));
                    // this yields 0-n extra progress ticks
                    progressField.set(te, (short) (progressField.getShort(te) + extraprogress));
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Advanced Machines screwed up hacking into IC2 TileEntityStandardMachine");
            e.printStackTrace();
            progressField = null;
        }
    }

    public void setOverclockRates(TileEntityStandardMachine te)
    {
        te.energyConsume *= mod.machinePowerDrawFactor;
        
        int overclockers = 0;
        for (int i = 0; i < te.upgradeSlot.size(); i++)
        {
            ItemStack stack = te.upgradeSlot.get(i);
            if (stack != null)
            {
                if (stack.getItem() instanceof ItemUpgradeModule && stack.getItemDamage() == 0)
                {
                    overclockers += stack.stackSize;
                }
            }
        }
        if (overclockers > 6)
        {
            te.getWorldObj().createExplosion(null, te.xCoord+0.5f, te.yCoord, te.zCoord+0.5f, 3f, true);
        }
    }

    @Override
    public ArrayList<InvSlotOutput> getOutputSlots()
    {
        return outSlotList;
    }

}
