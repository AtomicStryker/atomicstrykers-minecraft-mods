package atomicstryker.multimine.common;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(MultiMine.MOD_ID)
public class MultiMine {
    public static final String MOD_ID = "multimine";

    private static MultiMine instance;
    public static ISidedProxy proxy;

    public static Logger LOGGER;

    private File configFile;
    private MultiMineConfig config;
    private MultiMineServer multiMineServer;

    public MultiMine(IEventBus modEventBus) {
        instance = this;
        LOGGER = LogManager.getLogger();
        MultiMine.LOGGER.info("mod instantiated");
        proxy = FMLEnvironment.dist.isClient() ? new MultiMineClient() : new MultiMineServer();
        proxy.commonSetup();

        modEventBus.addListener(this::registerNetworking);

        // even if we are on client, we build a multi mine server for local play
        // if we are in a dedicated server, its the proxy object already built
        multiMineServer = FMLEnvironment.dist.isClient() ? new MultiMineServer() : (MultiMineServer) proxy;
        NeoForge.EVENT_BUS.register(multiMineServer);
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {

        // the optional method gives us a registrar that does non-mandatory packets
        // so clients having the mod can still connect to servers which dont have it
        final PayloadRegistrar registrar = event.registrar(MOD_ID).optional();

        registrar.playBidirectional(PartialBlockPacket.TYPE, PartialBlockPacket.STREAM_CODEC,
                (payload, context) -> proxy.handlePartialBlockPacket(payload, context));

        registrar.playToClient(PartialBlockRemovalPacket.TYPE, PartialBlockRemovalPacket.STREAM_CODEC,
                (payload, context) -> proxy.handlePartialBlockRemovalPacket(payload, context));
    }

    /**
     * is triggered either by server start or by client login event from InfernalMobsClient
     */
    public void initIfNeeded(Level world) {
        if (configFile == null) {
            Path mcFolder;
            if (world.isClientSide()) {
                mcFolder = MultiMineClient.getMcFolder().toPath();
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                mcFolder = server.getServerDirectory();
            }

            configFile = mcFolder.resolve("config" + File.separatorChar + "multimine.cfg").toFile();
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

    public MultiMineServer getServer() {
        return multiMineServer;
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
