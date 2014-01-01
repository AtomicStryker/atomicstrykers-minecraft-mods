package atomicstryker.multimine.common;

import java.util.HashSet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import atomicstryker.multimine.client.ClientPacketHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(modid = "AS_MultiMine", name = "Multi Mine", version = "1.3.4")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_MM"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = {"AS_MM"}, packetHandler = ServerPacketHandler.class),
connectionHandler = ConnectionHandler.class)
public class MultiMine
{
    private static MultiMine instance;
    private boolean blockRegenEnabled;
    private long initialBlockRegenDelay;
    private long blockRegenInterval;
    private String excludedBlocksString;
    private HashSet<Integer> excludedBlockSet;
    private String excludedItemsString;
    private HashSet<Integer> excludedItemSet;
    
    @SidedProxy(clientSide = "atomicstryker.multimine.client.ClientProxy", serverSide = "atomicstryker.multimine.common.CommonProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        instance = this;
        
        Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();
        
        blockRegenEnabled = config.get("general", "Block Regeneration Enabled", true).getBoolean(true);
        initialBlockRegenDelay = config.get("general", "Initial Block Regen Delay in ms", 5000).getInt();
        blockRegenInterval = config.get("general", "Block 10 percent Regen Interval in ms", 1000).getInt();
        
        excludedBlocksString = config.get("general", "Excluded Block IDs", "6,31,37,38,39,40,50,51,55,59,64,69,75,76,83,93,94,96,104,105,111,131,132,141,142").getString();
        excludedItemsString = config.get("general", "Excluded Item IDs", "290,291,292,293,294,359").getString();
        
        config.save();
        
        excludedBlockSet = new HashSet<Integer>();
        excludedItemSet = new HashSet<Integer>();
        setExcludedBlocksString(excludedBlocksString);
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.onLoad();
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        new MultiMineServer();
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
            excludedBlockSet.add(Integer.parseInt(s));
        }
    }
    
    /**
     * @param blockID Block ID to be checked
     * @return true if that Block ID is configured to be ignored by Multi Mine, false otherwise
     */
    public boolean getIsExcludedBlock(int blockID)
    {
        return excludedBlockSet.contains(blockID);
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
            excludedItemSet.add(Integer.parseInt(s));
        }
    }
    
    /**
     * @param itemStack Item to be checked
     * @return true if that Item is configured to be ignored by Multi Mine, false otherwise
     */
    public boolean getIsExcludedItem(ItemStack itemStack)
    {
        return itemStack != null && excludedItemSet.contains(itemStack.itemID);
    }
}
