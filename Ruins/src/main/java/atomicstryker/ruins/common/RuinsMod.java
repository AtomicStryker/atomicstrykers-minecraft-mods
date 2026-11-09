package atomicstryker.ruins.common;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
    // MC now needs this for registry access all over the place, just buffer the latest one
    private Level lastLoadedLevel;

    public RuinsMod() {
        instance = this;
        generatorMap = new ConcurrentHashMap<>();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CommandParseTemplate());
        LOGGER.info("Ruins instance built, events registered");
    }

    public static RuinsMod getInstance() {
        return instance;
    }

    private static File getWorldSaveDir(Level iWorld) {

        if (iWorld instanceof ServerLevel) {
            ServerLevel world = (ServerLevel) iWorld;
            // the same folder ServerLevel gives its DimensionDataStorage; looking the field up by its name
            // ("dataFolder") only works in a dev environment, outside one the field name is obfuscated
            return DimensionType.getStorageFolder(world.dimension(), world.getServer().getWorldPath(LevelResource.ROOT))
                    .resolve("data").toFile();
        }
        return null;
    }

    /**
     * All biomes of the running server. Since 1.19 biomes are data pack entries, ForgeRegistries.BIOMES does not
     * contain them (not even vanilla's), so it can't be used to look up biome names.
     */
    public static Set<Map.Entry<ResourceKey<Biome>, Biome>> getBiomeEntries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            lastBiomeRegistry = server.registryAccess().registryOrThrow(Registries.BIOME);
        }
        return lastBiomeRegistry != null ? lastBiomeRegistry.entrySet() : Collections.emptySet();
    }

    private static volatile Registry<Biome> lastBiomeRegistry;

    public static File getMinecraftBaseDir() {
        // from FML, like 26.x: the server proxy asked the running server, which is null while it shuts down, so a
        // dimension whose Ruins data was first set up then lost its ruins.txt (NullPointerException, defaults used)
        return FMLPaths.GAMEDIR.get().toFile();
    }

    @SubscribeEvent
    public void onEnteringChunk(EntityEvent.EnteringSection event) {
        /*
         * new concept of triggering Ruins generation: a player moving from one chunk to another shoots a "beam" several
         * chunks infront of them. at a certain minimum distance, this starts analyzing the chunk hit and its
         * surrounding chunks to try and spawn ruins in them. for performance, there can only be one such
         * beam executing at a time, it will never trigger worldgen, and we mark processed chunks by setting a block
         * in the bottom most/bedrock layer to some specific block
         */
        if (instance != null
                && event.getEntity() instanceof Player
                && !event.getEntity().level().isClientSide()) {

            ServerLevel world;
            WorldHandle wh;
            if (event.getEntity().level() instanceof ServerLevel) {
                world = (ServerLevel) event.getEntity().level();
                if (!world.structureManager().shouldGenerateStructures()) {
                    return;
                }
                wh = instance.getWorldHandle(world);
                if (wh == null
                        || !wh.fileHandle.loaded
                        || !wh.fileHandle.allowsDimension(world.dimension().location().getPath())) {
                    return;
                }
            } else {
                return;
            }

            // determine direction of movement, round anything faster than a chunk down to one
            int xMove = 0;
            int zMove = 0;
            if (event.getNewPos().x() > event.getOldPos().x()) {
                xMove = 1;
            } else if (event.getNewPos().x() < event.getOldPos().x()) {
                xMove = -1;
            }
            if (event.getNewPos().z() > event.getOldPos().z()) {
                zMove = 1;
            } else if (event.getNewPos().z() < event.getOldPos().z()) {
                zMove = -1;
            }
            if (xMove == 0 && zMove == 0) {
                // no movement? how? ok, get outta here
                return;
            }

            // project the movement forward
            int projectedChunkX = event.getNewPos().x() + (xMove * 7);
            int projectedChunkZ = event.getNewPos().z() + (zMove * 7);
            // iterate all the surrounding chunks from that
            for (int iterX = projectedChunkX - 2; iterX <= projectedChunkX + 2; iterX++) {
                for (int iterZ = projectedChunkZ - 2; iterZ <= projectedChunkZ + 2; iterZ++) {
                    // we dont want to spawn closer to the player, so do a simple min distance logic
                    int deltaX = iterX - event.getNewPos().x();
                    int deltaZ = iterZ - event.getNewPos().z();
                    double euclidChunkDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    if (euclidChunkDistance < 5) {
                        continue;
                    }
                    inspectChunk(world, new ChunkPos(iterX, iterZ), wh);
                }
            }
        }
    }

    /** Ruins are centred on a random spot in the chunk, so a footprint up to ~52 blocks wide stays within 2 chunks. */
    private static final int CHUNK_READY_RADIUS = 2;

    private void inspectChunk(ServerLevel world, ChunkPos chunkPos, WorldHandle worldHandle) {

        // hasChunk is already true while a chunk is only queued to load, and reading a block from a chunk that isn't
        // generated yet makes the server thread generate it on the spot. The site search also reads the ground under
        // the whole ruin, which reaches into the chunks around this one. So wait until this chunk and the chunks
        // within CHUNK_READY_RADIUS of it are fully generated (getChunkNow never loads anything). The chunk isn't
        // marked, so it's looked at again a move or two later.
        for (int dx = -CHUNK_READY_RADIUS; dx <= CHUNK_READY_RADIUS; dx++) {
            for (int dz = -CHUNK_READY_RADIUS; dz <= CHUNK_READY_RADIUS; dz++) {
                if (world.getChunkSource().getChunkNow(chunkPos.x + dx, chunkPos.z + dz) == null) {
                    return;
                }
            }
        }

        BlockPos ruinsMarkerBlockPos = new BlockPos(chunkPos.getMinBlockX(), world.getMinBuildHeight(), chunkPos.getMinBlockZ());
        BlockState blockState = world.getBlockState(ruinsMarkerBlockPos);
        if (blockState.is(Blocks.BARRIER)) {
            return;
        }
        // UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE: no neighbour updates. With 3, updating the neighbours to the west and
        // north reached into the next chunks, and if those weren't generated yet the server thread stopped and
        // waited for them to generate. The marker is on the bottom layer, so nothing around it needs updating.
        world.setBlock(ruinsMarkerBlockPos, Blocks.BARRIER.defaultBlockState(), 2 | 16);

        LOGGER.trace("Ruins generation for chunk {}", chunkPos);
        if (world.dimension().location().getPath().equals("the_nether")) {
            worldHandle.generator.generateNether(world, world.random, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        } else
        // normal world
        {
            worldHandle.generator.generateNormal(world, world.random, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent evt) {
        LOGGER.info("Ruins registerCommands");
        evt.getDispatcher().register(CommandParseTemplate.BUILDER);
        evt.getDispatcher().register(CommandTestTemplate.BUILDER);
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().level() instanceof ServerLevel) {
            WorldHandle wh = getWorldHandle((ServerLevel) event.getEntity().level());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getEntity().getMainHandItem();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    BlockEntity te = event.getEntity().level().getBlockEntity(event.getPosition().get());
                    event.getEntity().sendSystemMessage(Component.literal(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)));
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && event.getLevel() instanceof ServerLevel) {
            WorldHandle wh = getWorldHandle((ServerLevel) event.getLevel());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getPlayer().getMainHandItem();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    BlockEntity te = event.getPlayer().level().getBlockEntity(event.getPos());
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
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide) {
            executeCommandBlockLogic(event);
        }
    }

    public Level getLastLoadedLevel() {
        return lastLoadedLevel;
    }

    private void executeCommandBlockLogic(EntityEvent.EnteringSection event) {
        CommandBlockEntity tecb;
        ArrayList<CommandBlockEntity> tecblist = new ArrayList<>();

        for (int xoffset = -4; xoffset <= 4; xoffset++) {
            for (int zoffset = -4; zoffset <= 4; zoffset++) {
                if (event.getEntity().level().hasChunk(event.getNewPos().x() + xoffset, event.getNewPos().z() + zoffset)) {
                    for (BlockEntity teo : event.getEntity().level().getChunk(event.getNewPos().x() + xoffset, event.getNewPos().z() + zoffset).getBlockEntities().values()) {
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
            tecb2.getCommandBlock().performCommand(event.getEntity().level());
            // kill block
            BlockPos pos = tecb2.getBlockPos();
            LOGGER.info("Ruins executed and killed Command Block at [{}]", pos);
            event.getEntity().level().removeBlock(pos, false);
        }
    }

    private WorldHandle getWorldHandle(ServerLevel world) {
        WorldHandle wh = null;
        if (!world.isClientSide()) {
            if (!generatorMap.containsKey(world.dimension().location())) {
                wh = new WorldHandle();
                ConfigFolderPreparator.copyFromJarIfNotPresent(new File(getMinecraftBaseDir(), TEMPLATE_PATH_MC_EXTRACTED));
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
            // set before FileHandler starts its loader thread: parsing the templates' block states needs it, and
            // a thread that got there first read every block as air (or failed on null)
            lastLoadedLevel = world;
            worldHandle.fileHandle = new FileHandler(worlddir, world.dimension().location());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world);

        } catch (Exception e) {
            LOGGER.error("There was a problem loading the ruins mod:");
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private class WorldHandle {
        FileHandler fileHandle;
        RuinGenerator generator;
    }

}