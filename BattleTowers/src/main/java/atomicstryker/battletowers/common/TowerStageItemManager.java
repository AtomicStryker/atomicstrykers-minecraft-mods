package atomicstryker.battletowers.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class TowerStageItemManager
{

    private Object[] itemID;
    private int[] itemDamage;
    private int[] chanceToSpawn;
    private int[] minAmount;
    private int[] maxAmount;

    /**
     * @param configString
     *            see TowerStageItemManager in AS_BattleTowersCore for example
     *            String
     */
    public TowerStageItemManager(String configString)
    {
        String[] elements = configString.split(";");
        itemID = new Object[elements.length];
        itemDamage = new int[elements.length];
        chanceToSpawn = new int[elements.length];
        minAmount = new int[elements.length];
        maxAmount = new int[elements.length];

        ArrayList<Integer> validItemIndexes = new ArrayList<Integer>();
        for (int i = 0; i < elements.length; i++)
        {
            String[] settings = elements[i].trim().split("-");

            if (settings.length < 5 && !settings[0].startsWith("ChestGenHook:"))
            {
                System.err.println("Battletowers skipping invalid entry [" + elements[i].trim() + "], fix config file!");
            }
            else
            {
                itemID[i] = tryFindingObject(settings[0]);
                if (itemID[i] != null)
                {
                    validItemIndexes.add(i);
                    itemDamage[i] = Integer.parseInt(settings[1]);
                    chanceToSpawn[i] = Integer.parseInt(settings[2]);
                    minAmount[i] = Integer.parseInt(settings[3]);
                    maxAmount[i] = Integer.parseInt(settings[4]);
                }
                else if (settings[0].startsWith("ChestGenHook:"))
                {
                    validItemIndexes.add(i);
                    itemID[i] = settings[0];
                    chanceToSpawn[i] = 100;
                }

                if (itemID[i] != null)
                {
                    System.out.println("Battletowers parsed Item/Block/ChestGenHook " + itemID[i]);
                }
                else
                {
                    System.out.println("Battletowers failed parsing or finding Item/Block " + settings[0]);
                }
                // System.out.println("Name of that Item:
                // "+Item.itemsList[itemID[i]].getItemName());
            }
        }

        final Object[] itemIDf = new Object[validItemIndexes.size()];
        final int[] itemDamagef = new int[validItemIndexes.size()];
        final int[] chanceToSpawnf = new int[validItemIndexes.size()];
        final int[] minAmountf = new int[validItemIndexes.size()];
        final int[] maxAmountf = new int[validItemIndexes.size()];
        int assigned;
        for (int i = 0; i < validItemIndexes.size(); i++)
        {
            assigned = validItemIndexes.get(i);
            itemIDf[i] = itemID[assigned];
            itemDamagef[i] = itemDamage[assigned];
            chanceToSpawnf[i] = chanceToSpawn[assigned];
            minAmountf[i] = minAmount[assigned];
            maxAmountf[i] = maxAmount[assigned];
        }

        itemID = itemIDf;
        itemDamage = itemDamagef;
        chanceToSpawn = chanceToSpawnf;
        minAmount = minAmountf;
        maxAmount = maxAmountf;
    }

    private Object tryFindingObject(String s)
    {
        ResourceLocation rl = new ResourceLocation(s);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item != null)
        {
            return item;
        }

        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        if (block != Blocks.AIR)
        {
            return block;
        }
        return null;
    }

    /**
     * @param toCopy
     *            TowerStageItemManager you need an image of
     */
    public TowerStageItemManager(TowerStageItemManager toCopy)
    {
        itemID = toCopy.itemID.clone();
        itemDamage = toCopy.itemDamage.clone();
        chanceToSpawn = toCopy.chanceToSpawn.clone();
        minAmount = toCopy.minAmount.clone();
        maxAmount = toCopy.maxAmount.clone();
    }

    /**
     * @param rand
     *            your WorldGen Random
     * @param teChest
     *            chest tileentity to fill with loot
     * @param count
     *            how many slots worth of itemstacks are supposed to be
     *            generated
     * @return ItemStack instance of the configured Block or Item with amount,
     *         or null
     */
    public List<ItemStack> getStageItemStacks(World world, Random rand, TileEntityChest teChest, int count)
    {
        List<ItemStack> result = new ArrayList<>();
        int index = 0;
        while (result.size() < count && index < itemID.length)
        {
            if (itemID[index] instanceof Item && rand.nextInt(100) < chanceToSpawn[index])
            {
                result.add(new ItemStack((Item) itemID[index], minAmount[index] + rand.nextInt(maxAmount[index]), itemDamage[index]));
            }
            else if (itemID[index] instanceof Block && rand.nextInt(100) < chanceToSpawn[index])
            {
                result.add(new ItemStack((Block) itemID[index], minAmount[index] + rand.nextInt(maxAmount[index]), itemDamage[index]));
            }
            else if (itemID[index] instanceof String) // ChestGenHook:strongholdLibrary:5
            {
                String[] split = ((String) itemID[index]).split(":");
                LootTable loottable = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(split[1]));
                List<ItemStack> generatedItems = loottable.generateLootForPools(rand, new LootContext.Builder((WorldServer) world).build());

                if (split.length > 2)
                {
                    int number = Integer.valueOf(split[2]);
                    if (number > 0)
                    {
                        while (generatedItems.size() > number)
                        {
                            Collections.shuffle(generatedItems);
                            generatedItems.remove(generatedItems.size() - 1);
                        }
                        result.addAll(generatedItems);
                    }
                }
            }

            index++;
        }
        return result;
    }
}
