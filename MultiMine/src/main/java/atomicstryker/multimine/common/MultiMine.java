package atomicstryker.multimine.common;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.PartialBlockPacket;
import atomicstryker.multimine.common.network.PartialBlockRemovalPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * FML superclass causing all of the things to happen. Registers everything, causes the Mod parts
 * to load, keeps the common config file.
 */
@Mod(MultiMine.MOD_ID)
public class MultiMine {
    public static final String MOD_ID = "multimine";

    private static MultiMine instance;

    public static Logger LOGGER;

    private File configFile;
    private MultiMineConfig config;

    public MultiMine(IEventBus modEventBus) {
        instance = this;
        LOGGER = LogManager.getLogger();
        MultiMine.LOGGER.info("mod instantiated");
        NeoForge.EVENT_BUS.register(new MultiMineServer());

        modEventBus.addListener(this::registerNetworking);
    }

    private void registerNetworking(final RegisterPayloadHandlerEvent event) {

        // the optional method gives us a registrar that does non-mandatory packets
        // so clients having the mod can still connect to servers which dont have it
        final IPayloadRegistrar registrar = event.registrar(MOD_ID).optional();

        registrar.play(PartialBlockPacket.ID, PartialBlockPacket::new, handler -> handler
                .client(instance()::onPartialBlockForClient)
                .server(instance()::onPartialBlock));

        registrar.play(PartialBlockRemovalPacket.ID, PartialBlockRemovalPacket::new, handler -> handler
                .client(instance()::onPartialBlockRemovalForClient));
    }

    private void onPartialBlockForClient(PartialBlockPacket packet, PlayPayloadContext playPayloadContext) {
        playPayloadContext.workHandler().submitAsync(() -> MultiMineClient.instance().onServerSentPartialBlockData(packet.x(), packet.y(), packet.z(), packet.value(), packet.regenerating()));
    }

    private void onPartialBlock(PartialBlockPacket packet, PlayPayloadContext playPayloadContext) {
        ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(packet.user());
        if (p != null) {
            playPayloadContext.workHandler().submitAsync(() -> MultiMineServer.instance().onClientSentPartialBlockPacket(p, packet.x(), packet.y(), packet.z(), packet.value()));
        }
    }

    private void onPartialBlockRemovalForClient(PartialBlockRemovalPacket packet, PlayPayloadContext playPayloadContext) {
        playPayloadContext.workHandler().submitAsync(() -> MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(new BlockPos(packet.x(), packet.y(), packet.z())));
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
