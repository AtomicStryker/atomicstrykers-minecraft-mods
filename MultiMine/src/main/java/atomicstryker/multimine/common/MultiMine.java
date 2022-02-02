package atomicstryker.multimine.common;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(MultiMine.MOD_ID)
@Mod.EventBusSubscriber(modid = MultiMine.MOD_ID)
public class MultiMine {
    public static final String MOD_ID = "multimine";

    private static MultiMine instance;

    public static Logger LOGGER;

    public NetworkHelper networkHelper;

    private File configFile;
    private MultiMineConfig config;

    public MultiMine() {
        instance = this;
        networkHelper = new NetworkHelper("as_mm", PartialBlockPacket.class, PartialBlockRemovalPacket.class);
        LOGGER = LogManager.getLogger();
        MultiMine.LOGGER.info("mod instantiated");
        MinecraftForge.EVENT_BUS.register(new MultiMineServer());
    }

    /**
     * is triggered either by server start or by client login event from InfernalMobsClient
     */
    public void initIfNeeded(Level world) {
        if (configFile == null) {
            File mcFolder;
            if (world.isClientSide()) {
                mcFolder = MultiMineClient.getMcFolder();
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                mcFolder = server.getFile("");
            }

            configFile = new File(mcFolder, File.separatorChar + "config" + File.separatorChar + "multimine.cfg");
            loadConfig();
        }
    }

    private void loadConfig() {
        MultiMineConfig defaultConfig = new MultiMineConfig();
        defaultConfig.setDisableForAllTileEntities(false);
        LOGGER.info("config loading now");
        config = GsonConfig.loadConfigWithDefault(MultiMineConfig.class, configFile, defaultConfig);
        LOGGER.info("config loaded successfully");
    }

    public static MultiMine instance() {
        return instance;
    }

    public boolean getBlockRegenEnabled() {
        return config.isBlockRegenerationEnabled();
    }

    public long getInitialBlockRegenDelay() {
        return config.getInitialBlockRegenDelayMillis();
    }

    public long getBlockRegenInterval() {
        return config.getBlockRegenIntervalMillis();
    }

    public MultiMineConfig getConfig() {
        return config;
    }

    public void saveConfig() {
        GsonConfig.saveConfig(config, configFile);
    }

    public void debugPrint(String s, Object... params) {
        if (config.isDebugMode()) {
            LOGGER.info(s, params);
        }
    }
}
