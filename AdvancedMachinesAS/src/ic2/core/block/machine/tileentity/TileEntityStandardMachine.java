package ic2.core.block.machine.tileentity;

import java.util.List;

import net.minecraft.item.ItemStack;
import ic2.api.recipe.RecipeOutput;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;

public abstract class TileEntityStandardMachine extends TileEntityElectricMachine
{
    
    public InvSlotProcessable inputSlot;
    public final InvSlotOutput outputSlot;
    public int energyConsume;
    
    public TileEntityStandardMachine(int energyPerTick, int length, int outputSlots)
    {
      this(energyPerTick, length, outputSlots, 1);
    }
    
    public TileEntityStandardMachine(int energyPerTick, int length, int outputSlots, int defaultTier)
    {
      super(null, null, energyPerTick * length, 1, 1);
      outputSlot = null;
    }

    @Override
    public String getInvName()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setOverclockRates()
    {
        
    }
    
    public RecipeOutput getOutput()
    {
        return null;
    }
    
    public void operateOnce(RecipeOutput output, List<ItemStack> processResult)
    {      
        
    }
    
    public float getProgress()
    {
        return 0;
    }

}
