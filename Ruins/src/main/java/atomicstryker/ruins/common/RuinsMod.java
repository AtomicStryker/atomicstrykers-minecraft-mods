package atomicstryker.ruins.common;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Mod(RuinsMod.MOD_ID)
public class RuinsMod {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String TEMPLATE_PATH_MC_EXTRACTED = "config/ruins_config/";
    public static final String TEMPLATE_PATH_JAR = "ruins_config";
    public final static int DIR_NORTH = 0, DIR_EAST = 1, DIR_SOUTH = 2, DIR_WEST = 3;
    public static final String BIOME_ANY = "generic";
    static final String MOD_ID = "ruins";
    private static RuinsMod instance = null;
    private final ConcurrentHashMap<Identifier, WorldHandle> generatorMap;
    private long nextInfoTime;
    // MC now needs this for registry access all over the place, just buffer the latest one
    private Level lastLoadedLevel;

    public RuinsMod(IEventBus modEventBus) {
        instance = this;
        generatorMap = new ConcurrentHashMap<>();
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new CommandParseTemplate());
        LOGGER.info("Ruins instance built, events registered");
    }

    public static RuinsMod getInstance() {
        return instance;
    }

    private static File getWorldSaveDir(Level iWorld) {

        if (iWorld instanceof ServerLevel) {
            ServerLevel world = (ServerLevel) iWorld;
            try {
                for (Field declaredField : world.getChunkSource().getDataStorage().getClass().getDeclaredFields()) {
                    if (declaredField.getType().equals(Path.class)) {
                        declaredField.setAccessible(true);
                        Path path = (Path) declaredField.get(world.getChunkSource().getDataStorage());
                        return path.toFile();
                    }
                }
                throw new RuntimeException("Ruins mod could not find field File DimensionDataStorage.dataFolder");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static File getMinecraftBaseDir() {
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
                        || !wh.fileHandle.allowsDimension(world.dimension().identifier().getPath())) {
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

    private static void inspectChunk(ServerLevel world, ChunkPos chunkPos, WorldHandle worldHandle) {

        if (!world.hasChunk(chunkPos.x, chunkPos.z)) {
            return;
        }

        BlockPos ruinsMarkerBlockPos = new BlockPos(chunkPos.getMinBlockX(), world.getMinY(), chunkPos.getMinBlockZ());
        BlockState blockState = world.getBlockState(ruinsMarkerBlockPos);
        if (blockState.is(Blocks.BARRIER)) {
            return;
        }
        world.setBlock(ruinsMarkerBlockPos, Blocks.BARRIER.defaultBlockState(), 3);

        LOGGER.trace("Ruins generation for chunk {}", chunkPos);
        if (world.dimension().identifier().getPath().equals("the_nether")) {
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
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > instance.nextInfoTime) {
                    instance.nextInfoTime = System.currentTimeMillis() + 1000L;
                    BlockEntity te = event.getEntity().level().getBlockEntity(event.getPosition().get());
                    event.getEntity().displayClientMessage(Component.literal(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)), false);
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
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > instance.nextInfoTime) {
                    instance.nextInfoTime = System.currentTimeMillis() + 1000L;
                    BlockEntity te = event.getPlayer().level().getBlockEntity(event.getPos());
                    event.getPlayer().displayClientMessage(Component.literal(RuleStringNbtHelper.StringFromBlockState(event.getState(), te)), false);
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
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide()) {
            executeCommandBlockLogic(event);
        }
    }

    public Level getLastLoadedLevel() {
        return lastLoadedLevel;
    }

    private static void executeCommandBlockLogic(EntityEvent.EnteringSection event) {
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
            tecb2.getCommandBlock().performCommand((ServerLevel) event.getEntity().level());
            // kill block
            BlockPos pos = tecb2.getBlockPos();
            LOGGER.info("Ruins executed and killed Command Block at [{}]", pos);
            event.getEntity().level().removeBlock(pos, false);
        }
    }

    private static WorldHandle getWorldHandle(ServerLevel world) {
        WorldHandle wh = null;
        if (!world.isClientSide()) {
            if (!instance.generatorMap.containsKey(world.dimension().identifier())) {
                wh = new WorldHandle();
                ConfigFolderPreparator.copyFromJarIfNotPresent(new File(getMinecraftBaseDir(), TEMPLATE_PATH_MC_EXTRACTED));
                initWorldHandle(wh, world);
                instance.generatorMap.put(world.dimension().identifier(), wh);
            } else {
                wh = instance.generatorMap.get(world.dimension().identifier());
            }
        }

        return wh;
    }

    private static void initWorldHandle(WorldHandle worldHandle, ServerLevel world) {
        // load in defaults
        try {
            File worlddir = getWorldSaveDir(world);
            LOGGER.info("Ruins mod determines World Save Dir to be at: {}", worlddir);
            worldHandle.fileHandle = new FileHandler(worlddir, world.dimension().identifier());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world);
            instance.lastLoadedLevel = world;

        } catch (Exception e) {
            LOGGER.error("There was a problem loading the ruins mod:");
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static class WorldHandle {
        FileHandler fileHandle;
        RuinGenerator generator;
    }

}