package ic2.advancedmachines.common;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

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
    public List<ItemStack> getResultFor(ItemStack input, boolean adjustOutput)
    {
        RecipeOutput output = Recipes.compressor.getOutputFor(input, adjustOutput);
        return output == null ? null : output.items;
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
