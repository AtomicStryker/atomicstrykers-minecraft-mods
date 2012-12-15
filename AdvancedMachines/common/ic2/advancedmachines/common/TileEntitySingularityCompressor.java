package ic2.advancedmachines.common;

import java.util.*;
import net.minecraft.src.*;
import ic2.api.*;

public class TileEntitySingularityCompressor extends TileEntityAdvancedMachine
{
    public TileEntitySingularityCompressor()
    {
        super("Singularity Compressor", "%6d PSI", 10, new int[] {1}, new int[] {2});
    }

    @Override
    public Container getGuiContainer(InventoryPlayer var1)
    {
        return new ContainerSingularityCompressor(var1, this);
    }

    @Override
    protected List getResultMap()
    {
        return Ic2Recipes.getCompressorRecipes();
    }

    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustOutput)
    {
        return Ic2Recipes.getCompressorOutputFor(input, adjustOutput);
    }
    
    @Override
    public int getUpgradeSlotsStartSlot()
    {
        return 3;
    }
    
    @Override
    public String getStartSoundFile()
    {
        return AdvancedMachines.advCompSound;
    }

    @Override
    public String getInterruptSoundFile()
    {
        return AdvancedMachines.interruptSound;
    }
}
