package atomicstryker.ruins.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod(RuinsMod.MOD_ID)
@Mod.EventBusSubscriber(modid = RuinsMod.MOD_ID, value = Dist.DEDICATED_SERVER)
public class RuinsMod {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String TEMPLATE_PATH_MC_EXTRACTED = "config/ruins_config/";
    public static final String TEMPLATE_PATH_JAR = "ruins_config";
    public final static int DIR_NORTH = 0, DIR_EAST = 1, DIR_SOUTH = 2, DIR_WEST = 3;
    public static final String BIOME_ANY = "generic";
    static final String MOD_ID = "ruins";
    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new RuinsClient(), () -> () -> new RuinsServer());
    private static RuinsMod instance;
    private final ConcurrentHashMap<Dimension, WorldHandle> generatorMap;
    private long nextInfoTime;

    public RuinsMod() {
        instance = this;
        generatorMap = new ConcurrentHashMap<>();
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CommandParseTemplate());
        MinecraftForge.EVENT_BUS.register(new CommandUndoTemplate());
        LOGGER.info("Ruins instance built, events registered");
    }

    private static File getWorldSaveDir(IWorld iWorld) {

        if (iWorld instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) iWorld;
            return world.getSaveHandler().getWorldDirectory();
        }
        return null;
    }

    public static File getMinecraftBaseDir() {
        return proxy.getBaseDir();
    }

    /**
     * called by the coremod injection from ChunkGenerator.decorate
     */
    public static void decorateChunkHook(WorldGenRegion worldGenRegion) {

        if (worldGenRegion.getWorld().isRemote() || !worldGenRegion.getWorld().getWorldInfo().isMapFeaturesEnabled()) {
            return;
        }

        ServerWorld world = worldGenRegion.getWorld();
        int x = worldGenRegion.getMainChunkX();
        int z = worldGenRegion.getMainChunkZ();
        ChunkPos chunkPos = new ChunkPos(x, z);
        LOGGER.trace("Ruins chunk decoration [{}|{}]", x, z);
        final WorldHandle wh = instance.getWorldHandle(world);
        if (wh != null) {

            if (wh.currentlyGenerating.contains(chunkPos)) {
                LOGGER.error("Ruins Mod caught recursive generator call at chunk {}", chunkPos);
            } else {
                if (wh.fileHandle.allowsDimension(world.getDimension().getType().getId()) && (wh.chunkLogger == null || !wh.chunkLogger.catchChunkBug(chunkPos))) {
                    wh.currentlyGenerating.add(chunkPos);
                    // sigh. no proper event for this. lets try it like this
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            world.getServer().deferTask(() -> {
                                if (world.getDimension().isNether()) {
                                    instance.generateNether(world, world.rand, chunkPos.getXStart(), chunkPos.getZStart());
                                } else
                                // normal world
                                {
                                    instance.generateSurface(world, world.rand, chunkPos.getXStart(), chunkPos.getZStart());
                                }
                                wh.currentlyGenerating.remove(chunkPos);
                            });
                        }
                    }, 10000L);
                }
            }
        }
    }

    public void preInit(FMLCommonSetupEvent evt) {
        LOGGER.info("Ruins preInit");
        ConfigFolderPreparator.copyFromJarIfNotPresent(this, new File(getMinecraftBaseDir(), TEMPLATE_PATH_MC_EXTRACTED));
    }

    @SubscribeEvent
    public void serverStarted(FMLServerStartingEvent evt) {
        LOGGER.info("Ruins serverStarted");
        evt.getCommandDispatcher().register(CommandParseTemplate.BUILDER);
        evt.getCommandDispatcher().register(CommandTestTemplate.BUILDER);
        evt.getCommandDispatcher().register(CommandUndoTemplate.BUILDER);
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().getEntityWorld() instanceof ServerWorld) {
            WorldHandle wh = getWorldHandle((ServerWorld) event.getEntity().getEntityWorld());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getEntityPlayer().getHeldItemMainhand();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    TileEntity te = event.getEntityPlayer().world.getTileEntity(event.getPos());
                    event.getEntityPlayer().sendMessage(new TranslationTextComponent(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)));
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && !(event.getPlayer() instanceof FakePlayer) && event.getWorld() instanceof ServerWorld) {
            WorldHandle wh = getWorldHandle((ServerWorld) event.getWorld());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getPlayer().getHeldItemMainhand();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    TileEntity te = event.getPlayer().world.getTileEntity(event.getPos());
                    event.getPlayer().sendMessage(new TranslationTextComponent(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void eventWorldSave(WorldEvent.Save evt) {
        if (evt.getWorld() instanceof ServerWorld) {
            WorldHandle wh = getWorldHandle((ServerWorld) evt.getWorld());
            if (wh != null) {
                wh.generator.flushPosFile(evt.getWorld().getWorldInfo().getWorldName());
            }
        }
    }

    @SubscribeEvent
    public void onEntityEnteringChunk(EntityEvent.EnteringChunk event) {
        if (event.getEntity() instanceof PlayerEntity && !event.getEntity().world.isRemote) {
            CommandBlockTileEntity tecb;
            ArrayList<CommandBlockTileEntity> tecblist = new ArrayList<>();

            for (int xoffset = -4; xoffset <= 4; xoffset++) {
                for (int zoffset = -4; zoffset <= 4; zoffset++) {
                    for (TileEntity teo : event.getEntity().world.getChunk(event.getNewChunkX() + xoffset, event.getNewChunkZ() + zoffset).getTileEntityMap().values()) {
                        if (teo instanceof CommandBlockTileEntity) {
                            tecb = (CommandBlockTileEntity) teo;
                            if (tecb.getCommandBlockLogic().getCommand().startsWith("RUINSTRIGGER ")) {
                                // strip prefix from command
                                tecb.getCommandBlockLogic().setCommand((tecb.getCommandBlockLogic().getCommand()).substring(13));
                                tecblist.add(tecb);
                            }
                        }
                    }
                }
            }

            for (CommandBlockTileEntity tecb2 : tecblist) {
                // call command block execution
                tecb2.getCommandBlockLogic().trigger(event.getEntity().world);
                // kill block
                BlockPos pos = tecb2.getPos();
                LOGGER.info("Ruins executed and killed Command Block at [{}]", pos);
                event.getEntity().world.removeBlock(pos, false);
            }
        }
    }

    private void generateNether(ServerWorld world, Random random, int chunkX, int chunkZ) {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null) {
            while (!wh.fileHandle.loaded) {
                Thread.yield();
            }
            wh.generator.generateNether(world, random, chunkX, chunkZ);
        }
    }

    private void generateSurface(ServerWorld world, Random random, int chunkX, int chunkZ) {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null) {
            while (!wh.fileHandle.loaded) {
                Thread.yield();
            }
            wh.generator.generateNormal(world, random, chunkX, chunkZ);
        }
    }

    private WorldHandle getWorldHandle(ServerWorld world) {
        WorldHandle wh = null;
        if (!world.getWorld().isRemote) {
            Dimension dimension = world.dimension;
            if (!generatorMap.containsKey(dimension)) {
                wh = new WorldHandle();
                initWorldHandle(wh, world);
                generatorMap.put(dimension, wh);
            } else {
                wh = generatorMap.get(dimension);
            }
        }

        return wh;
    }

    private void initWorldHandle(WorldHandle worldHandle, ServerWorld world) {
        // load in defaults
        try {
            File worlddir = getWorldSaveDir(world);
            LOGGER.info("Ruins mod determines World Save Dir to be at: {}", worlddir);
            worldHandle.fileHandle = new FileHandler(worlddir, world.getDimension().getType());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world.getWorld());
            worldHandle.currentlyGenerating = new ConcurrentLinkedQueue<>();

            worldHandle.chunkLogger = world.getSavedData().get(() -> new ChunkLoggerData("ruinschunklogger"), "ruinschunklogger");

        } catch (Exception e) {
            LOGGER.error("There was a problem loading the ruins mod:");
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private class WorldHandle {
        FileHandler fileHandle;
        RuinGenerator generator;
        ConcurrentLinkedQueue<ChunkPos> currentlyGenerating;
        ChunkLoggerData chunkLogger;
    }

}