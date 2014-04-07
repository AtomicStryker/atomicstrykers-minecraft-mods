package com.sirolf2009.necromancy.craftingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.entity.RegistryNecromancyEntities;
import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;
import com.sirolf2009.necromancy.recipes.ShapedRecipes4x4;
import com.sirolf2009.necromancy.recipes.ShapelessRecipes4x4;

public class CraftingManagerSewing
{

    private final List<IRecipe> recipes;

    public CraftingManagerSewing()
    {
        recipes = new ArrayList<IRecipe>();
        Iterator<?> itr = NecroEntityRegistry.registeredEntities.values().iterator();
        NecroEntityBase mob;
        while (itr.hasNext())
        {
            mob = (NecroEntityBase) itr.next();
            if (mob.hasHead && mob.headRecipe != null)
            {
                addRecipe(mob.headItem, mob.headRecipe);
            }
            if (mob.hasTorso && mob.torsoRecipe != null)
            {
                addRecipe(mob.torsoItem, mob.torsoRecipe);
            }
            if (mob.hasArms && mob.armRecipe != null)
            {
                addRecipe(mob.armItem, mob.armRecipe);
            }
            if (mob.hasLegs && mob.legRecipe != null)
            {
                addRecipe(mob.legItem, mob.legRecipe);
            }
        }
        addShapelessRecipe(new ItemStack(RegistryNecromancyItems.organs, 8, 4), new Object[] { Items.leather });
        addShapelessRecipe(new ItemStack(RegistryNecromancyItems.spawner, 1), new Object[] { Items.rotten_flesh, Items.rotten_flesh, Items.rotten_flesh,
                Items.rotten_flesh, Items.rotten_flesh, Items.ghast_tear, Items.ghast_tear,
                ItemGeneric.getItemStackFromName("Soul in a Jar").getItem(), new ItemStack(RegistryNecromancyItems.organs, 1, 1).getItem() });
        addRecipe(new ItemStack(Items.spawn_egg, 1, RegistryNecromancyEntities.entityIDTeddy), new Object[] { "LLLL", "LWWL", "LWWL", "LLLL", 'L', Items.leather,
                'W', Blocks.wool });
    }
    
    /*
    public void addHeadRecipe(ItemStack result, Object essence)
    {
        addRecipe(
                result,
                new Object[] { "LLLL", "LBXL", "LEEL", Character.valueOf('L'), new ItemStack(ItemNecromancy.genericItems, 1, 7),
                        Character.valueOf('E'), Items.spider_eye, Character.valueOf('X'), essence, Character.valueOf('B'),
                        new ItemStack(ItemNecromancy.genericItems, 1, 0) });
    }

    public void addTorsoRecipe(ItemStack result, Object essence)
    {
        addRecipe(result, new Object[] { " LL ", "BHUB", "LEEL", "BLLB", 'L', new ItemStack(ItemNecromancy.genericItems, 1, 7), 'E', essence, 'H',
                new ItemStack(ItemNecromancy.genericItems, 1, 1), 'U', new ItemStack(ItemNecromancy.genericItems, 1, 3), 'B', Items.bone });
    }

    public void addArmRecipe(ItemStack result, Object essence)
    {
        addRecipe(result, new Object[] { "LLLL", "BMEB", "LLLL", 'L', new ItemStack(ItemNecromancy.genericItems, 1, 7), 'E', essence, 'M',
                ItemGeneric.getItemStackFromName("Muscle"), 'B', Items.bone });
    }

    public void addLegRecipe(ItemStack result, Object essence)
    {
        addRecipe(result, new Object[] { "LBBL", "LMML", "LEEL", "LBBL", 'L', new ItemStack(ItemNecromancy.genericItems, 1, 7), 'E', essence, 'M',
                ItemGeneric.getItemStackFromName("Muscle"), 'B', Items.bone });
    }
    */

    private void addShapelessRecipe(ItemStack par1ItemStack, Object objArray[])
    {
        ArrayList<ItemStack> stackList = new ArrayList<ItemStack>();
        for (Object recipeObject : objArray)
        {
            if (recipeObject instanceof ItemStack)
            {
                stackList.add(((ItemStack) recipeObject).copy());
                continue;
            }
            if (recipeObject instanceof Item)
            {
                stackList.add(new ItemStack((Item) recipeObject));
                continue;
            }
            if (!(recipeObject instanceof Block))
                throw new RuntimeException("Necromancy: Invalid shapeless recipe, offending object: " + recipeObject);
            stackList.add(new ItemStack((Block) recipeObject));
        }
        recipes.add(new ShapelessRecipes4x4(par1ItemStack, stackList));
    }

    private void addRecipe(ItemStack itemstack, Object aobj[])
    {
        String s = "";
        int index = 0;
        int length = 0;
        int count = 0;
        if (aobj[index] instanceof String[])
        {
            String as[] = (String[]) aobj[index++];
            for (int l = 0; l < as.length; l++)
            {
                String s2 = as[l];
                count++;
                length = s2.length();
                s = new StringBuilder().append(s).append(s2).toString();
            }
        }
        else
        {
            while (aobj[index] instanceof String)
            {
                String s1 = (String) aobj[index++];
                count++;
                length = s1.length();
                s = new StringBuilder().append(s).append(s1).toString();
            }
        }
        HashMap<Character, ItemStack> hashmap = new HashMap<Character, ItemStack>();
        for (; index < aobj.length; index += 2)
        {
            Character character = (Character) aobj[index];
            ItemStack itemstack1 = null;
            if (aobj[index + 1] instanceof Item)
            {
                itemstack1 = new ItemStack((Item) aobj[index + 1]);
            }
            else if (aobj[index + 1] instanceof Block)
            {
                itemstack1 = new ItemStack((Block) aobj[index + 1], 1, -1);
            }
            else if (aobj[index + 1] instanceof ItemStack)
            {
                itemstack1 = (ItemStack) aobj[index + 1];
            }
            hashmap.put(character, itemstack1);
        }

        ItemStack aitemstack[] = new ItemStack[length * count];
        for (int i1 = 0; i1 < length * count; i1++)
        {
            char c = s.charAt(i1);
            if (hashmap.containsKey(Character.valueOf(c)))
            {
                aitemstack[i1] = hashmap.get(Character.valueOf(c)).copy();
            }
            else
            {
                aitemstack[i1] = null;
            }
        }

        recipes.add(new ShapedRecipes4x4(length, count, aitemstack, itemstack));
    }
    
    /**
     * called by ContainerSewing to determine what should happen to a crafting matrix
     */
    public ItemStack findMatchingRecipe(InventoryCrafting inventorycrafting, World world)
    {
        int i = 0;
        ItemStack itemstack = null;
        ItemStack itemstack1 = null;
        for (int j = 0; j < inventorycrafting.getSizeInventory(); j++)
        {
            ItemStack itemstack2 = inventorycrafting.getStackInSlot(j);
            if (itemstack2 == null)
            {
                continue;
            }
            if (i == 0)
            {
                itemstack = itemstack2;
            }
            if (i == 1)
            {
                itemstack1 = itemstack2;
            }
            i++;
        }

        if (i == 2 && itemstack == itemstack1 && itemstack.stackSize == 1 && itemstack1.stackSize == 1 && itemstack.getItem().isDamageable())
        {
            Item item = itemstack.getItem();
            int l = item.getMaxDamage() - itemstack.getItemDamageForDisplay();
            int i1 = item.getMaxDamage() - itemstack1.getItemDamageForDisplay();
            int j1 = l + i1 + item.getMaxDamage() * 10 / 100;
            int k1 = item.getMaxDamage() - j1;
            if (k1 < 0)
            {
                k1 = 0;
            }
            return new ItemStack(item, 1, k1);
        }

        for (int k = 0; k < recipes.size(); k++)
        {
            IRecipe irecipe = recipes.get(k);
            if (irecipe.matches(inventorycrafting, world))
                return irecipe.getCraftingResult(inventorycrafting);
        }

        return null;
    }

}
