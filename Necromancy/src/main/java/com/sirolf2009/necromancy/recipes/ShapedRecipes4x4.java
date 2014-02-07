package com.sirolf2009.necromancy.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class ShapedRecipes4x4 implements IRecipe
{

    public ShapedRecipes4x4(int i, int j, ItemStack aitemstack[], ItemStack itemstack)
    {
        recipeWidth = i;
        recipeHeight = j;
        recipeItems = aitemstack;
        recipeOutput = itemstack;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return recipeOutput;
    }

    @Override
    public boolean matches(InventoryCrafting inventorycrafting, World world)
    {
        for (int i = 0; i <= 4 - recipeWidth; i++)
        {
            for (int j = 0; j <= 4 - recipeHeight; j++)
            {
                if (matches(inventorycrafting, i, j, true))
                    return true;
                if (matches(inventorycrafting, i, j, false))
                    return true;
            }
        }

        return false;
    }

    private boolean matches(InventoryCrafting inventorycrafting, int i, int j, boolean flag)
    {
        for (int row = 0; row < 4; row++)
        {
            for (int column = 0; column < 4; column++)
            {
                int columnI = row - i;
                int rowI = column - j;
                ItemStack itemStackRecipe = null;
                if (columnI >= 0 && rowI >= 0 && columnI < recipeWidth && rowI < recipeHeight)
                {
                    if (flag)
                    {
                        itemStackRecipe = recipeItems[recipeWidth - columnI - 1 + rowI * recipeWidth];
                    }
                    else
                    {
                        itemStackRecipe = recipeItems[columnI + rowI * recipeWidth];
                    }
                }
                ItemStack itemStackActual = inventorycrafting.getStackInRowAndColumn(row, column);
                if (itemStackActual == null && itemStackRecipe == null)
                {
                    continue;
                }
                if ((itemStackActual == null && itemStackRecipe != null) || (itemStackActual != null && itemStackRecipe == null))
                {
                    return false;
                }

                if (!itemStackRecipe.isItemEqual(itemStackActual))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventorycrafting)
    {
        return new ItemStack(recipeOutput.getItem(), recipeOutput.stackSize, recipeOutput.getItemDamage());
    }

    @Override
    public int getRecipeSize()
    {
        return recipeWidth * recipeHeight;
    }

    private int recipeWidth;
    private int recipeHeight;
    private ItemStack recipeItems[];
    private ItemStack recipeOutput;
}
