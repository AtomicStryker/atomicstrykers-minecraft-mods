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
                if (func_21137_a(inventorycrafting, i, j, true))
                    return true;
                if (func_21137_a(inventorycrafting, i, j, false))
                    return true;
            }

        }

        return false;
    }

    private boolean func_21137_a(InventoryCrafting inventorycrafting, int i, int j, boolean flag)
    {
        for (int k = 0; k < 4; k++)
        {
            for (int l = 0; l < 4; l++)
            {
                int i1 = k - i;
                int j1 = l - j;
                ItemStack itemstack = null;
                if (i1 >= 0 && j1 >= 0 && i1 < recipeWidth && j1 < recipeHeight)
                    if (flag)
                    {
                        itemstack = recipeItems[recipeWidth - i1 - 1 + j1 * recipeWidth];
                    }
                    else
                    {
                        itemstack = recipeItems[i1 + j1 * recipeWidth];
                    }
                ItemStack itemstack1 = inventorycrafting.getStackInRowAndColumn(k, l);
                if (itemstack1 == null && itemstack == null)
                {
                    continue;
                }
                if (itemstack1 == null && itemstack != null || itemstack1 != null && itemstack == null)
                    return false;
                if (itemstack != itemstack1)
                    return false;
                if (itemstack.getItemDamage() != -1 && itemstack.getItemDamage() != itemstack1.getItemDamage())
                    return false;
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
