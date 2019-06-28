package atomicstryker.multimine.common;

import atomicstryker.multimine.client.ClientProxy;
import atomicstryker.multimine.common.network.NetworkHelper;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(MultiMine.MOD_ID)
@Mod.EventBusSubscriber(modid = MultiMine.MOD_ID)
public class MultiMine {

    public static final String MOD_ID = "multimine";

    public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    private static MultiMine instance;
    private final Logger LOGGER = LogManager.getLogger();
    public MultiMineConfig config;
    public File configFile;
    public NetworkHelper networkHelper;

    public MultiMine() {
        instance = this;

        networkHelper = new NetworkHelper("asmultimine", PartialBlockPacket.class, PartialBlockRemovalPacket.class);

        loadOrDefaultConfig();

        proxy.onPreInit();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static MultiMine instance() {
        return instance;
    }

    private void loadOrDefaultConfig() {

        MultiMineConfig defaultConfig = new MultiMineConfig();
        defaultConfig.setBlockRegenEnabled(true);
        defaultConfig.setInitialBlockRegenDelay(5000);
        defaultConfig.setBlockRegenInterval(1000);
        defaultConfig.setDebugMode(false);
        defaultConfig.setBannedBlocks(new HashMap<>());
        defaultConfig.setBannedItems(new HashMap<>());

        configFile = proxy.getConfigFile();
        config = GsonConfig.loadConfigWithDefault(MultiMineConfig.class, configFile, defaultConfig);
    }

    public void commonSetup(FMLCommonSetupEvent evt) {
        proxy.onLoad();
    }

    public boolean getBlockRegenEnabled() {
        return config.isBlockRegenEnabled();
    }

    public long getInitialBlockRegenDelay() {
        return config.getInitialBlockRegenDelay();
    }

    public long getBlockRegenInterval() {
        return config.getBlockRegenInterval();
    }

    public void debugPrint(String s, Object... params) {
        if (config.isDebugMode()) {
            LOGGER.info(s, params);
        }
    }
}
