package atomicstryker.ruins.common;


import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
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

    public RuinsMod(FMLJavaModLoadingContext context) {
        instance = this;
        generatorMap = new ConcurrentHashMap<>();
        final IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::registration);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CommandParseTemplate());
        MinecraftForge.EVENT_BUS.register(new CommandUndoTemplate());
        LOGGER.info("Ruins instance built, events registered");

        // must register the feature, else it crashes on map load
        ForgeRegistries.FEATURES.register("ruins", RUINS_PSEUDO_FEATURE);

        final DeferredRegister<Codec<? extends BiomeModifier>> serializers = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
        serializers.register(modEventBus);
        serializers.register("gen_hook", RuinsBiomeModifier::makeCodec);
    }

    public void registration(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BIOME_MODIFIERS,
                helper -> helper.register("ruins_gen_hook", new RuinsBiomeModifier())
        );
    }

    protected static final Feature<NoneFeatureConfiguration> RUINS_PSEUDO_FEATURE = new RuinsFeature(NoneFeatureConfiguration.CODEC);

    protected static final Holder<PlacedFeature> PLACED_RUINS = PlacementUtils.register("ruins_placed", FeatureUtils.register("ruins_configured", RUINS_PSEUDO_FEATURE));

    protected static class RuinsFeature extends Feature<NoneFeatureConfiguration> {
        public RuinsFeature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
            WorldGenLevel worldgenlevel = featurePlaceContext.level();
            BlockPos blockpos = featurePlaceContext.origin();
            decorateChunkHook(worldgenlevel, blockpos);
            return true;
        }
    }

    public record RuinsBiomeModifier() implements BiomeModifier {

        private static final RegistryObject<Codec<? extends BiomeModifier>> SERIALIZER =
                RegistryObject.create(new ResourceLocation("ruins:gen_hook"), ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.AFTER_EVERYTHING) {
                builder.getGenerationSettings().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PLACED_RUINS);
                RuinsMod.LOGGER.trace("RuinsBiomeModifier.modify was executed for biome {}, feature added", biome.unwrapKey().get());
            }
        }

        public Codec<? extends BiomeModifier> codec() {
            return SERIALIZER.get();
        }

        public static Codec<RuinsBiomeModifier> makeCodec() {
            return Codec.unit(RuinsBiomeModifier::new);
        }
    }

    private static File getWorldSaveDir(Level iWorld) {

        if (iWorld instanceof ServerLevel) {
            ServerLevel world = (ServerLevel) iWorld;
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

    public static void decorateChunkHook(WorldGenLevel worldGenLevel, BlockPos blockPos) {

        LOGGER.trace("decorateChunkHook {}", blockPos);
        if (worldGenLevel.isClientSide()
                || !worldGenLevel.getLevel().structureManager().shouldGenerateStructures()
                || instance == null) {
            return;
        }

        @SuppressWarnings("deprecation")
        ServerLevel world = worldGenLevel.getLevel();
        int chunkX = (int) Math.floor(blockPos.getX() / 16.0D);
        int chunkY = (int) Math.floor(blockPos.getY() / 16.0D);
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
                                if (world.dimension().location().getPath().equals("the_nether")) {
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
        if (event.getEntity().level instanceof ServerLevel) {
            WorldHandle wh = getWorldHandle((ServerLevel) event.getEntity().level);
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getEntity().getMainHandItem();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    BlockEntity te = event.getEntity().level.getBlockEntity(event.getPosition().get());
                    event.getEntity().sendSystemMessage(Component.literal(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)));
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && !(event.getPlayer() instanceof FakePlayer) && event.getLevel() instanceof ServerLevel) {
            WorldHandle wh = getWorldHandle((ServerLevel) event.getLevel());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getPlayer().getMainHandItem();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    BlockEntity te = event.getPlayer().level.getBlockEntity(event.getPos());
                    event.getPlayer().sendSystemMessage(Component.literal(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void eventWorldSave(LevelEvent.Save evt) {
        if (evt.getLevel() instanceof ServerLevel) {
            WorldHandle wh = getWorldHandle((ServerLevel) evt.getLevel());
            if (wh != null) {
                wh.generator.flushPosFile(((ServerLevel) evt.getLevel()).getServer().getWorldData().getLevelName());
            }
        }
    }

    @SubscribeEvent
    public void onEntityEnteringChunk(EntityEvent.EnteringSection event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide) {
            event.getEntity().level.getServer().addTickable(() -> executeCommandBlockLogic(event));
        }
    }

    private void executeCommandBlockLogic(EntityEvent.EnteringSection event) {
        CommandBlockEntity tecb;
        ArrayList<CommandBlockEntity> tecblist = new ArrayList<>();

        for (int xoffset = -4; xoffset <= 4; xoffset++) {
            for (int zoffset = -4; zoffset <= 4; zoffset++) {
                if (event.getEntity().level.hasChunk(event.getNewPos().x() + xoffset, event.getNewPos().z() + zoffset)) {
                    for (BlockEntity teo : event.getEntity().level.getChunk(event.getNewPos().x() + xoffset, event.getNewPos().z() + zoffset).getBlockEntities().values()) {
                        if (teo instanceof CommandBlockEntity) {
                            tecb = (CommandBlockEntity) teo;
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

        for (CommandBlockEntity tecb2 : tecblist) {
            // call command block execution
            tecb2.getCommandBlock().performCommand(event.getEntity().level);
            // kill block
            BlockPos pos = tecb2.getBlockPos();
            LOGGER.info("Ruins executed and killed Command Block at [{}]", pos);
            event.getEntity().level.removeBlock(pos, false);
        }
    }

    private void generateNether(ServerLevel world, RandomSource random, int chunkX, int chunkZ) {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null) {
            while (!wh.fileHandle.loaded) {
                Thread.yield();
            }
            wh.generator.generateNether(world, random, chunkX, chunkZ);
        }
    }

    private void generateSurface(ServerLevel world, RandomSource random, int chunkX, int chunkZ) {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null) {
            while (!wh.fileHandle.loaded) {
                Thread.yield();
            }
            wh.generator.generateNormal(world, random, chunkX, chunkZ);
        }
    }

    private WorldHandle getWorldHandle(ServerLevel world) {
        WorldHandle wh = null;
        if (!world.isClientSide()) {
            if (!generatorMap.containsKey(world.dimension().location())) {
                wh = new WorldHandle();
                initWorldHandle(wh, world);
                generatorMap.put(world.dimension().location(), wh);
            } else {
                wh = generatorMap.get(world.dimension().location());
            }
        }

        return wh;
    }

    private void initWorldHandle(WorldHandle worldHandle, ServerLevel world) {
        // load in defaults
        try {
            File worlddir = getWorldSaveDir(world);
            LOGGER.info("Ruins mod determines World Save Dir to be at: {}", worlddir);
            worldHandle.fileHandle = new FileHandler(worlddir, world.dimension().location());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world.getLevel());
            worldHandle.currentlyGenerating = new ConcurrentLinkedQueue<>();

            worldHandle.chunkLogger = world.getDataStorage().computeIfAbsent(ChunkLoggerData::load, ChunkLoggerData::new, "ruinschunklogger");

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