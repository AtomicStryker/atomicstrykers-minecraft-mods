package atomicstryker.multimine.common;

import atomicstryker.multimine.common.network.NetworkHelper;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(modid = "multimine", name = "Multi Mine", version = "1.5.4port1710")
public class MultiMine
{
    @Instance("multimine")
    private static MultiMine instance;

    private boolean blockRegenEnabled;
    private long initialBlockRegenDelay;
    private long blockRegenInterval;

    private boolean debugMode;
    public Configuration config;

    @SidedProxy(clientSide = "atomicstryker.multimine.client.ClientProxy", serverSide = "atomicstryker.multimine.common.CommonProxy")
    public static CommonProxy proxy;

    public NetworkHelper networkHelper;

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        networkHelper = new NetworkHelper("AS_MM", PartialBlockPacket.class, PartialBlockRemovalPacket.class);

        config = new Configuration(evt.getSuggestedConfigurationFile());
        config.load();

        blockRegenEnabled = config.get("general", "Block Regeneration Enabled", true).getBoolean(true);
        initialBlockRegenDelay = config.get("general", "Initial Block Regen Delay in ms", 5000).getInt();
        blockRegenInterval = config.get("general", "Block 10 percent Regen Interval in ms", 1000).getInt();

        debugMode = config.get("general", "debugMode", false, "Tons of debug printing. Only enable if really needed.").getBoolean(false);

        config.save();

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

    public void debugPrint(String s)
    {
        if (debugMode)
        {
            System.out.println(s);
        }
    }
}
