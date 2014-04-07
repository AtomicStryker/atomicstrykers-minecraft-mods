package com.sirolf2009.necromancy.recipes;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class ShapelessRecipes4x4 implements IRecipe
{
    
    private final ItemStack recipeOutput;
    private final ArrayList<ItemStack> recipeItems;

    public ShapelessRecipes4x4(ItemStack par1ItemStack, ArrayList<ItemStack> par2List)
    {
        recipeOutput = par1ItemStack;
        recipeItems = par2List;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return recipeOutput;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting par1InventoryCrafting)
    {
        return recipeOutput.copy();
    }

    @Override
    public int getRecipeSize()
    {
        return recipeItems.size();
    }

    @Override
    public boolean matches(InventoryCrafting invCrafting, World world)
    {
        ArrayList<ItemStack> recipeItemsList = new ArrayList<ItemStack>(recipeItems);
        for (int column = 0; column < 4; column++)
        {
            for (int row = 0; row < 4; row++)
            {
                ItemStack itemStack = invCrafting.getStackInRowAndColumn(row, column);

                if (itemStack != null)
                {
                    for (Iterator<ItemStack> iter = recipeItemsList.iterator(); iter.hasNext();)
                    {
                        ItemStack itemStackRecipe = iter.next();
                        if (itemStack.isItemEqual(itemStackRecipe))
                        {
                            iter.remove();
                        }
                    }
                }
            }
        }
        return recipeItemsList.isEmpty();
    }
}
