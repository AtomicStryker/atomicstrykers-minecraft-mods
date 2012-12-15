package ic2.advancedmachines.common;

import java.util.*;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import ic2.api.*;

public class TileEntityCentrifugeExtractor extends TileEntityAdvancedMachine
{
    public TileEntityCentrifugeExtractor()
    {
        super("Centrifuge Extractor", "%5d M/S", 1, new int[] {1}, new int[] {2, 3, 4});
    }
    
    @Override
    public Container getGuiContainer(InventoryPlayer var1)
    {
        return new ContainerCentrifugeExtractor(var1, this);
    }

    @Override
    protected List getResultMap()
    {
        return Ic2Recipes.getExtractorRecipes();
    }

    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustOutput)
    {
        return Ic2Recipes.getExtractorOutputFor(input, adjustOutput);
    }
    
    @Override
    public int getUpgradeSlotsStartSlot()
    {
        return 5;
    }
    
    @Override
    public String getStartSoundFile()
    {
        return AdvancedMachines.advExtcSound;
    }

    @Override
    public String getInterruptSoundFile()
    {
        return AdvancedMachines.interruptSound;
    }
}
