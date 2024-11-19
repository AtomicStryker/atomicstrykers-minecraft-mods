package atomicstryker.ruins.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
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
    private static RuinsMod instance = null;
    private final ConcurrentHashMap<ResourceLocation, WorldHandle> generatorMap;
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

        RUINS_PSEUDO_FEATURE.setRegistryName(MOD_ID, "pseudofeature");
        ForgeRegistries.FEATURES.register(RUINS_PSEUDO_FEATURE);
    }

    private Feature RUINS_PSEUDO_FEATURE = new Feature<NoFeatureConfig>(NoFeatureConfig.CODEC) {
        @Override
        public boolean place(ISeedReader iSeedReader, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoFeatureConfig noFeatureConfig) {
            if (iSeedReader instanceof WorldGenRegion) {
                decorateChunkHook((WorldGenRegion) iSeedReader, blockPos);
            }
            return false;
        }
    };

    @SubscribeEvent
    public void onBiomeLoading(BiomeLoadingEvent event) {
        event.getGeneration().addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION, RUINS_PSEUDO_FEATURE.configured(IFeatureConfig.NONE));
    }

    private static File getWorldSaveDir(IWorld iWorld) {

        if (iWorld instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) iWorld;
            try {
                Field declaredField = world.getChunkSource().getDataStorage().getClass().getDeclaredField("dataFolder");
                declaredField.setAccessible(true);
                return (File) declaredField.get(world.getChunkSource().getDataStorage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static File getMinecraftBaseDir() {
        return proxy.getBaseDir();
    }

    public static void decorateChunkHook(WorldGenRegion worldGenRegion, BlockPos blockPos) {

        if (worldGenRegion.isClientSide()
                || !worldGenRegion.getLevel().structureFeatureManager().shouldGenerateFeatures()
                || instance == null) {
            return;
        }

        @SuppressWarnings("deprecation")
        ServerWorld world = worldGenRegion.getLevel();
        int chunkX = MathHelper.floor(blockPos.getX() / 16.0D);
        int chunkY = MathHelper.floor(blockPos.getY() / 16.0D);
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkY);
        LOGGER.trace("Ruins chunk decoration [{}|{}]", chunkX, chunkY);
        final WorldHandle wh = instance.getWorldHandle(world);
        if (wh != null) {

            if (!wh.currentlyGenerating.contains(chunkPos)) {
                if (wh.fileHandle.allowsDimension(world.dimension().location().getPath()) && (wh.chunkLogger == null || !wh.chunkLogger.catchChunkBug(chunkPos))) {
                    wh.currentlyGenerating.add(chunkPos);
                    // sigh. no proper event for this. lets try it like this
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            world.getServer().addTickable(() -> {
                                if (world.dimension().getRegistryName().getPath().equals("the_nether")) {
                                    instance.generateNether(world, world.random, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
                                } else
                                // normal world
                                {
                                    int decoratorYCoordinate = blockPos.getY();
                                    instance.generateSurface(world, world.random, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
                                }
                                wh.currentlyGenerating.remove(chunkPos);
                            });
                        }
                    }, 15000L);
                }
            }
        }
    }

    public void preInit(FMLCommonSetupEvent evt) {
        LOGGER.info("Ruins preInit");
        ConfigFolderPreparator.copyFromJarIfNotPresent(this, new File(getMinecraftBaseDir(), TEMPLATE_PATH_MC_EXTRACTED));
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent evt) {
        LOGGER.info("Ruins registerCommands");
        evt.getDispatcher().register(CommandParseTemplate.BUILDER);
        evt.getDispatcher().register(CommandTestTemplate.BUILDER);
        evt.getDispatcher().register(CommandUndoTemplate.BUILDER);
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().level instanceof ServerWorld) {
            WorldHandle wh = getWorldHandle((ServerWorld) event.getEntity().level);
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getPlayer().getMainHandItem();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    TileEntity te = event.getPlayer().level.getBlockEntity(event.getPos());
                    event.getPlayer().sendMessage(new TranslationTextComponent(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)), Util.NIL_UUID);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && !(event.getPlayer() instanceof FakePlayer) && event.getWorld() instanceof ServerWorld) {
            WorldHandle wh = getWorldHandle((ServerWorld) event.getWorld());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getPlayer().getMainHandItem();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    TileEntity te = event.getPlayer().level.getBlockEntity(event.getPos());
                    event.getPlayer().sendMessage(new TranslationTextComponent(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)), Util.NIL_UUID);
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
                wh.generator.flushPosFile(((ServerWorld) evt.getWorld()).getServer().getWorldData().getLevelName());
            }
        }
    }

    @SubscribeEvent
    public void onEntityEnteringChunk(EntityEvent.EnteringChunk event) {
        if (event.getEntity() instanceof PlayerEntity && !event.getEntity().level.isClientSide) {
            event.getEntity().level.getServer().addTickable(() -> executeCommandBlockLogic(event));
        }
    }

    private void executeCommandBlockLogic(EntityEvent.EnteringChunk event) {
        CommandBlockTileEntity tecb;
        ArrayList<CommandBlockTileEntity> tecblist = new ArrayList<>();

        for (int xoffset = -4; xoffset <= 4; xoffset++) {
            for (int zoffset = -4; zoffset <= 4; zoffset++) {
                if (event.getEntity().level.hasChunk(event.getNewChunkX() + xoffset, event.getNewChunkZ() + zoffset)) {
                    for (TileEntity teo : event.getEntity().level.getChunk(event.getNewChunkX() + xoffset, event.getNewChunkZ() + zoffset).getBlockEntities().values()) {
                        if (teo instanceof CommandBlockTileEntity) {
                            tecb = (CommandBlockTileEntity) teo;
                            if (tecb.getCommandBlock().getCommand().startsWith("RUINSTRIGGER ")) {
                                // strip prefix from command
                                tecb.getCommandBlock().setCommand((tecb.getCommandBlock().getCommand()).substring(13));
                                tecblist.add(tecb);
                            }
                        }
                    }
                }
            }
        }

        for (CommandBlockTileEntity tecb2 : tecblist) {
            // call command block execution
            tecb2.getCommandBlock().performCommand(event.getEntity().level);
            // kill block
            BlockPos pos = tecb2.getBlockPos();
            LOGGER.info("Ruins executed and killed Command Block at [{}]", pos);
            event.getEntity().level.removeBlock(pos, false);
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
        if (!world.isClientSide()) {
            if (!generatorMap.containsKey(world.dimension().getRegistryName())) {
                wh = new WorldHandle();
                initWorldHandle(wh, world);
                generatorMap.put(world.dimension().getRegistryName(), wh);
            } else {
                wh = generatorMap.get(world.dimension().getRegistryName());
            }
        }

        return wh;
    }

    private void initWorldHandle(WorldHandle worldHandle, ServerWorld world) {
        // load in defaults
        try {
            File worlddir = getWorldSaveDir(world);
            LOGGER.info("Ruins mod determines World Save Dir to be at: {}", worlddir);
            worldHandle.fileHandle = new FileHandler(worlddir, world.dimension().getRegistryName());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world.getLevel());
            worldHandle.currentlyGenerating = new ConcurrentLinkedQueue<>();

            worldHandle.chunkLogger = world.getDataStorage().get(() -> new ChunkLoggerData("ruinschunklogger"), "ruinschunklogger");

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