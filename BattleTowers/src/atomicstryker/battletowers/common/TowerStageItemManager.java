package atomicstryker.battletowers.common;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TowerStageItemManager
{
	/*
	 *  Example setting string: 75-0-50-1-4;73-20-1-1
	 *  Spawns Redstone(75) Item Damage 0 with 50 percent chance, at least 1, max 4
	 *  Spawns Saddle(73) Item Damage 0 with 20 percent chance, at least 1, max 1
	 */
	
	private int[] itemID;
	private int[] itemDamage;
	private int[] chanceToSpawn;
	private int[] minAmount;
	private int[] maxAmount;
	private int curIndex = 0;
	
	/**
	 * @param configString see TowerStageItemManager in AS_BattleTowersCore for example String
	 */
	public TowerStageItemManager(String configString)
	{
		String[] elements = configString.split(";");
		itemID = new int[elements.length];
		itemDamage = new int[elements.length];
		chanceToSpawn = new int[elements.length];
		minAmount = new int[elements.length];
		maxAmount = new int[elements.length];
		
		ArrayList<Integer> validItemIndexes = new ArrayList<Integer>();
		for (int i = 0; i < elements.length; i++)
		{
			String[] settings = elements[i].trim().split("-");
			
			if (settings.length < 5)
			{
			    System.err.println("Battletowers skipping invalid entry ["+elements[i].trim()+"], fix config file!");
			}
			else
			{
	            itemID[i] = tryFindingItemID(settings[0]);
	            if (itemID[i] != 0)
	            {
	                validItemIndexes.add(i);
	            }
	            itemDamage[i] = Integer.parseInt(settings[1]);
	            chanceToSpawn[i] = Integer.parseInt(settings[2]);
	            minAmount[i] = Integer.parseInt(settings[3]);
	            maxAmount[i] = Integer.parseInt(settings[4]);
	            
	            if (itemID[i] != 0)
                {
	                System.out.println("Battletowers parsed Item/Block of ID "+itemID[i]+", damageValue: "+itemDamage[i]+" spawnChance: "+chanceToSpawn[i]+", min: "+minAmount[i]+", max: "+maxAmount[i]);
                }
	            else
	            {
	                System.out.println("Battletowers failed parsing or finding Item/Block "+settings[0]);
	            }
	            //System.out.println("Name of that Item: "+Item.itemsList[itemID[i]].getItemName());
			}
		}
		
		final int[] itemIDf = new int[validItemIndexes.size()];
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
	
    private int tryFindingItemID(String s)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            for (Item item : Item.itemsList)
            {
                if (item != null && item.getUnlocalizedName().equals(s))
                {
                    return item.itemID;
                }
            }
            for (Block block : Block.blocksList)
            {
                if (block != null && block.getUnlocalizedName().equals(s))
                {
                    return block.blockID;
                }
            }
            return 0;
        }
    }
	
	/**
	 * @param toCopy TowerStageItemManager you need an image of
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
	 * @return true if there is still Items configured to be put into the chest of the Managers floor
	 */
	public boolean floorHasItemsLeft()
	{
		return (curIndex < itemID.length);
	}
	
	/**
	 * @param rand your WorldGen Random
	 * @return ItemStack instance of the configured Block or Item with amount, or null
	 */
	public ItemStack getStageItem(Random rand)
	{
		ItemStack result = null;
		
		if (floorHasItemsLeft()
		&& rand.nextInt(100) < chanceToSpawn[curIndex])
		{
		    Block block = itemID[curIndex] < Block.blocksList.length ? Block.blocksList[itemID[curIndex]] : null;
		    Item item = itemID[curIndex] < Item.itemsList.length ? Item.itemsList[itemID[curIndex]] : null;
            if (item != null && item.getUnlocalizedName() != null)
            {
                //System.out.println("Stashed item "+item.getUnlocalizedName()+" of id "+itemID[curIndex]);
                result = new ItemStack(item, minAmount[curIndex]+rand.nextInt(maxAmount[curIndex]), itemDamage[curIndex]);
                //System.out.println("Stashed new damaged ItemStack, id "+itemID[curIndex]+", "+result.getItemName()+" in a BT chest.");
            }
            else if (block != null && block.getUnlocalizedName() != null && !block.getUnlocalizedName().equals("tile.ForgeFiller"))
		    {
		        //System.out.println("Stashed block "+block.getUnlocalizedName()+" of id "+itemID[curIndex]);
		        result = new ItemStack(block, minAmount[curIndex]+rand.nextInt(maxAmount[curIndex]), itemDamage[curIndex]);
		        //System.out.println("Stashed new damaged Block Stack, id "+itemID[curIndex]+", "+result.getItemName()+" in a BT chest.");
		    }
		}

		curIndex++;
		return result;
	}
}
