package ic2.advancedmachines.common;

import ic2.api.recipe.Recipes;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class TileEntityCentrifugeExtractor extends TileEntityAdvancedMachine
{
    public TileEntityCentrifugeExtractor()
    {
        super("Centrifuge Extractor", "%5d M/S", 1, new int[] {1}, new int[] {2, 3, 4});
    }
    
    @Override
    protected boolean isStackValidForSlot(int slotSize, ItemStack itemstack, int blockSide)
    {
        if (blockSide == 1)
        {
            return getResultFor(itemstack, false) != null;
        }
        return isStackValidForSlot(slotSize, itemstack);
    }
    
    @Override
    public Container getGuiContainer(InventoryPlayer var1)
    {
        return new ContainerCentrifugeExtractor(var1, this);
    }

    @Override
    public ItemStack getResultFor(ItemStack input, boolean adjustOutput)
    {
        return Recipes.extractor.getOutputFor(input, adjustOutput);
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
