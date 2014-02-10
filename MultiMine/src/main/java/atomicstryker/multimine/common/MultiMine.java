package atomicstryker.multimine.common;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import atomicstryker.multimine.client.ClientPacketHandler;
import atomicstryker.multimine.common.network.PacketDispatcher;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(modid = "AS_MultiMine", name = "Multi Mine", version = "1.3.5")
public class MultiMine
{
    @Instance("AS_MultiMine")
    private static MultiMine instance;
    
    private boolean blockRegenEnabled;
    private long initialBlockRegenDelay;
    private long blockRegenInterval;
    private String excludedBlocksString;
    private HashSet<Block> excludedBlockSet;
    private String excludedItemsString;
    private HashSet<Item> excludedItemSet;
    
    @SidedProxy(clientSide = "atomicstryker.multimine.client.ClientProxy", serverSide = "atomicstryker.multimine.common.CommonProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        PacketDispatcher.init("AS_MM", new ClientPacketHandler(), new ServerPacketHandler());
        
        Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        blockRegenEnabled = config.get("general", "Block Regeneration Enabled", true).getBoolean(true);
        initialBlockRegenDelay = config.get("general", "Initial Block Regen Delay in ms", 5000).getInt();
        blockRegenInterval = config.get("general", "Block 10 percent Regen Interval in ms", 1000).getInt();
        
        excludedBlocksString = config.get("general", "Excluded Block IDs", "sapling,tallgrass,yellow_flower,red_flower,brown_mushroom,red_mushroom,torch,fire,redstone_wire,wheat,wooden_door,lever,unlit_redstone_torch,redstone_torch,reeds,unpowered_repeater,powered_repeater,trapdoor,pumpkin_stem,melon_stem,waterlily,tripwire_hook,tripwire,carrots,potatoes").getString();
        excludedItemsString = config.get("general", "Excluded Item IDs", "wooden_hoe,stone_hoe,iron_hoe,diamond_hoe,golden_hoe,shears").getString();
        
        config.save();
        
        excludedBlockSet = new HashSet<Block>();
        excludedItemSet = new HashSet<Item>();
        setExcludedBlocksString(excludedBlocksString);
        
        proxy.onPreInit();
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.onLoad();
    }
    
    public static MultiMine instance()
    {
        return instance;
    }
    
    public boolean getBlockRegenEnabled()
    {
        return blockRegenEnabled;
    }

    public long getInitialBlockRegenDelay()
    {
        return initialBlockRegenDelay;
    }

    public long getBlockRegenInterval()
    {
        return blockRegenInterval;
    }
    
    public String getExcludedBlocksString()
    {
        return excludedBlocksString;
    }
    
    public String getExcludedItemssString()
    {
        return excludedItemsString;
    }
    
    /**
     * Updates the excluded Block ID String and causes the internal Set to update with it.
     * @param input String of Block IDs, seperated by a comma
     */
    public void setExcludedBlocksString(String input)
    {
        excludedBlocksString = input.trim();
        String[] numbers = excludedBlocksString.split(",");
        for (String s : numbers)
        {
            Block b = GameData.blockRegistry.getObject(s);
            if (b != Blocks.air)
            {
                excludedBlockSet.add(b);
            }
        }
    }
    
    /**
     * Updates the excluded Item ID String and causes the internal Set to update with it.
     * @param input String of Item IDs, seperated by a comma
     */
    public void setExcludedItemssString(String input)
    {
        excludedItemsString = input.trim();
        String[] numbers = excludedItemsString.split(",");
        for (String s : numbers)
        {
            Item it = GameData.itemRegistry.getObject(s);
            if (it != null)
            {
                excludedItemSet.add(it);
            }
        }
    }
    
    /**
     * @param itemStack Item to be checked
     * @return true if that Item is configured to be ignored by Multi Mine, false otherwise
     */
    public boolean getIsExcludedItem(ItemStack itemStack)
    {
        return itemStack != null && excludedItemSet.contains(itemStack.getItem());
    }
}
