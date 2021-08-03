package atomicstryker.dynamiclights.server;

import atomicstryker.dynamiclights.server.blocks.BlockLitAir;
import atomicstryker.dynamiclights.server.blocks.BlockLitCaveAir;
import atomicstryker.dynamiclights.server.blocks.BlockLitWater;
import atomicstryker.dynamiclights.server.modules.DroppedItemsLightSource;
import atomicstryker.dynamiclights.server.modules.PlayerSelfLightSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author AtomicStryker
 * <p>
 * Rewritten and now-awesome Dynamic Lights Mod.
 * <p>
 * Instead of the crude base edits and inefficient giant loops of the
 * original, this Mod uses ASM transforming to hook into Minecraft with
 * style and has an API that does't suck. It also uses Forge events to
 * register dropped Items.
 */
@Mod(DynamicLights.MOD_ID)
public class DynamicLights {

    public static final String MOD_ID = "dynamiclights";
    private static final Logger LOGGER = LogManager.getLogger();
    private static DynamicLights instance;

    DynamicLightsConfig config = null;

    /**
     * This Map contains a List of DynamicLightSourceContainer for each World. Since
     * the client can only be in a single World, the other Lists just float idle
     * when unused.
     */
    private ConcurrentHashMap<Level, ConcurrentLinkedQueue<atomicstryker.dynamiclights.server.DynamicLightSourceContainer>> worldLightsMap;

    private PlayerSelfLightSource playerSelfLightSource;
    private DroppedItemsLightSource droppedItemsLightSource;

    public static final HashMap<Block, Block> vanillaBlocksToLitBlocksMap = new HashMap<>();

    public DynamicLights() {
        instance = this;
        worldLightsMap = new ConcurrentHashMap<>();

        playerSelfLightSource = new PlayerSelfLightSource();
        droppedItemsLightSource = new DroppedItemsLightSource();

        // this one is for RegistryEvent
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(DynamicLights.class);

        // this one is for FMLServerStartedEvent, WorldTickEvent
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onBlocksRegistration(final RegistryEvent.Register<Block> event) {
        Block litAirBlock = new BlockLitAir(BlockBehaviour.Properties.of(Material.AIR).noCollission().randomTicks().lightLevel((x) -> 15).noDrops().air()).setRegistryName(DynamicLights.MOD_ID, "lit_air");
        event.getRegistry().register(litAirBlock);
        Block litWaterBlock = new BlockLitWater(Fluids.WATER, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).lightLevel((x) -> 15).noDrops()).setRegistryName(DynamicLights.MOD_ID, "lit_water");
        event.getRegistry().register(litWaterBlock);
        Block litCaveAirBlock = new BlockLitCaveAir(BlockBehaviour.Properties.of(Material.AIR).noCollission().lightLevel((x) -> 15).noDrops().air()).setRegistryName(DynamicLights.MOD_ID, "lit_cave_air");
        vanillaBlocksToLitBlocksMap.put(Blocks.AIR, litAirBlock);
        vanillaBlocksToLitBlocksMap.put(Blocks.WATER, litWaterBlock);
        vanillaBlocksToLitBlocksMap.put(Blocks.CAVE_AIR, litCaveAirBlock);
    }

    @SubscribeEvent
    public void serverStarted(FMLServerStartedEvent evt) {
        // dedicated server starting point
        if (config == null) {
            initConfig();
        }
    }

    private void initConfig() {
        try {
            MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            File configFile = new File(server.getFile(""), File.separatorChar + "config" + File.separatorChar + "dynamiclights.cfg");
            config = GsonConfig.loadConfigWithDefault(DynamicLightsConfig.class, configFile, new DynamicLightsConfig());
        } catch (IOException e) {
            LOGGER.error("IOException parsing config", e);
        }
    }

    /**
     * Exposed method to register active Dynamic Light Sources with. Does all the
     * necessary checks, prints errors if any occur, creates new World entries in
     * the worldLightsMap
     *
     * @param lightToAdd IDynamicLightSource to register
     */
    public static void addLightSource(IDynamicLightSource lightToAdd) {
        if (lightToAdd.getAttachmentEntity() != null) {
            String dimensionLocationPath = lightToAdd.getAttachmentEntity().level.dimension().location().getPath();
            LOGGER.info("Calling addLightSource on entity {}, dimensionLocationPath {}", lightToAdd.getAttachmentEntity(), dimensionLocationPath);
            if (lightToAdd.getAttachmentEntity().isAlive() && !instance.isBannedDimension(dimensionLocationPath)) {
                DynamicLightSourceContainer newLightContainer = new DynamicLightSourceContainer(lightToAdd);
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(lightToAdd.getAttachmentEntity().level);
                if (lightList != null) {
                    if (!lightList.contains(newLightContainer)) {
                        LOGGER.info("Successfully registered DynamicLight on Entity: {} in list {}", newLightContainer.getLightSource().getAttachmentEntity(), lightList);
                        lightList.add(newLightContainer);
                    } else {
                        LOGGER.info("Cannot add Dynamic Light: Attachment Entity is already registered!");
                    }
                } else {
                    lightList = new ConcurrentLinkedQueue<>();
                    lightList.add(newLightContainer);
                    instance.worldLightsMap.put(lightToAdd.getAttachmentEntity().level, lightList);
                }
            } else {
                LOGGER.error("Cannot add Dynamic Light: Attachment Entity {} is dead or in a banned dimension {}", lightToAdd.getAttachmentEntity(), lightToAdd.getAttachmentEntity().level.dimension().location().getPath());
            }
        } else {
            LOGGER.error("Cannot add Dynamic Light: Attachment Entity is null!");
        }
    }

    /**
     * Exposed method to remove active Dynamic Light sources with. If it fails for
     * whatever reason, it does so quietly.
     *
     * @param lightToRemove IDynamicLightSource you want removed.
     */
    public static void removeLightSource(IDynamicLightSource lightToRemove) {
        if (lightToRemove != null && lightToRemove.getAttachmentEntity() != null) {
            Level world = lightToRemove.getAttachmentEntity().level;
            if (world != null) {
                DynamicLightSourceContainer iterContainer = null;
                ConcurrentLinkedQueue<DynamicLightSourceContainer> lightList = instance.worldLightsMap.get(world);
                if (lightList != null) {
                    Iterator<DynamicLightSourceContainer> iter = lightList.iterator();
                    while (iter.hasNext()) {
                        iterContainer = iter.next();
                        if (iterContainer.getLightSource().equals(lightToRemove)) {
                            iter.remove();
                            break;
                        }
                    }

                    if (iterContainer != null) {
                        LOGGER.info("Removing Dynamic Light attached to {}", lightToRemove.getAttachmentEntity());
                        iterContainer.removeLight(world);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void serverWorldTick(TickEvent.WorldTickEvent event) {

        if (event.side != LogicalSide.SERVER) {
            return;
        }

        ConcurrentLinkedQueue<DynamicLightSourceContainer> worldLights = worldLightsMap.get(event.world);
        if (worldLights != null) {
            Iterator<DynamicLightSourceContainer> iter = worldLights.iterator();
            while (iter.hasNext()) {
                DynamicLightSourceContainer tickedLightContainer = iter.next();
                if (tickedLightContainer.onUpdate()) {
                    iter.remove();
                    tickedLightContainer.removeLight(event.world);
                    LOGGER.debug("Dynamic Lights killing off LightSource on dead Entity: " + tickedLightContainer.getLightSource().getAttachmentEntity());
                }
            }
        }
    }

    /**
     * is a given dimension id on the banned list and will not receive dynamic
     * lighting
     */
    public boolean isBannedDimension(String dimensionID) {
        return config.getBannedDimensions().contains(dimensionID);
    }

    /**
     * in order to cleanup orphaned dynamic light blocks, they tick randomly and kill themselves unless they are a known dynamic light source
     */
    public static boolean isKnownLitPosition(Level world, BlockPos blockPos) {
        ConcurrentLinkedQueue<DynamicLightSourceContainer> worldLights = instance.worldLightsMap.get(world);
        if (worldLights != null) {
            for (DynamicLightSourceContainer light : worldLights) {
                if (blockPos.equals(light.getPos())) {
                    return true;
                }
            }
        }
        return false;
    }
}
