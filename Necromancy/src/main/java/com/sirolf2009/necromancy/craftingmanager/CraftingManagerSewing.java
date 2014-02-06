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
import com.sirolf2009.necromancy.entity.EntityNecromancy;
import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.item.ItemNecromancy;
import com.sirolf2009.necromancy.recipes.ShapedRecipes4x4;
import com.sirolf2009.necromancy.recipes.ShapelessRecipes4x4;

public class CraftingManagerSewing
{

    public static final CraftingManagerSewing getInstance()
    {
        return instance;
    }

    private static final CraftingManagerSewing instance = new CraftingManagerSewing();
    private List<IRecipe> recipes;

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
        addShapelessRecipe(new ItemStack(ItemNecromancy.organs, 8, 4), new Object[] { Items.leather });
        addShapelessRecipe(new ItemStack(ItemNecromancy.spawner, 1), new Object[] { Items.rotten_flesh, Items.rotten_flesh, Items.rotten_flesh,
                Items.rotten_flesh, Items.rotten_flesh, Items.ghast_tear, Items.ghast_tear,
                ItemGeneric.getItemStackFromName("Soul in a Jar").getItem(), new ItemStack(ItemNecromancy.organs, 1, 1).getItem() });
        addRecipe(new ItemStack(Items.spawn_egg, 1, EntityNecromancy.TeddyID), new Object[] { "LLLL", "LWWL", "LWWL", "LLLL", 'L', Items.leather,
                'W', Blocks.wool });
    }

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

    public void log(Object obj)
    {
        System.out.println(obj);
    }

    public void addShapelessRecipe(ItemStack par1ItemStack, Object par2ArrayOfObj[])
    {
        ArrayList<ItemStack> var3 = new ArrayList<ItemStack>();
        Object var4[] = par2ArrayOfObj;
        int var5 = par2ArrayOfObj.length;
        for (int var6 = 0; var6 < var5; var6++)
        {
            Object var7 = var4[var6];
            if (var7 instanceof ItemStack)
            {
                var3.add(((ItemStack) var7).copy());
                continue;
            }
            if (var7 instanceof Item)
            {
                var3.add(new ItemStack((Item) var7));
                continue;
            }
            if (!(var7 instanceof Block))
                throw new RuntimeException("Invalid shapeless recipy!");
            var3.add(new ItemStack((Block) var7));
        }

        recipes.add(new ShapelessRecipes4x4(par1ItemStack, var3));
    }

    void addRecipe(ItemStack itemstack, Object aobj[])
    {
        String s = "";
        int i = 0;
        int j = 0;
        int k = 0;
        if (aobj[i] instanceof String[])
        {
            String as[] = (String[]) aobj[i++];
            for (int l = 0; l < as.length; l++)
            {
                String s2 = as[l];
                k++;
                j = s2.length();
                s = new StringBuilder().append(s).append(s2).toString();
            }

        }
        else
        {
            while (aobj[i] instanceof String)
            {
                String s1 = (String) aobj[i++];
                k++;
                j = s1.length();
                s = new StringBuilder().append(s).append(s1).toString();
            }
        }
        HashMap<Character, ItemStack> hashmap = new HashMap<Character, ItemStack>();
        for (; i < aobj.length; i += 2)
        {
            Character character = (Character) aobj[i];
            ItemStack itemstack1 = null;
            if (aobj[i + 1] instanceof Item)
            {
                itemstack1 = new ItemStack((Item) aobj[i + 1]);
            }
            else if (aobj[i + 1] instanceof Block)
            {
                itemstack1 = new ItemStack((Block) aobj[i + 1], 1, -1);
            }
            else if (aobj[i + 1] instanceof ItemStack)
            {
                itemstack1 = (ItemStack) aobj[i + 1];
            }
            hashmap.put(character, itemstack1);
        }

        ItemStack aitemstack[] = new ItemStack[j * k];
        for (int i1 = 0; i1 < j * k; i1++)
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

        recipes.add(new ShapedRecipes4x4(j, k, aitemstack, itemstack));
    }

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

    public List<IRecipe> getRecipeList()
    {
        return recipes;
    }

}
