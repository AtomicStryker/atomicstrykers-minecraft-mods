package atomicstryker.ruins.common;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
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
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CommandParseTemplate());
        MinecraftForge.EVENT_BUS.register(new CommandUndoTemplate());
        LOGGER.info("Ruins instance built, events registered");
    }

    private static File getWorldSaveDir(IWorld iWorld) {

        if (iWorld instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) iWorld;
            try {
                for (Field declaredField : world.getChunkSource().getDataStorage().getClass().getDeclaredFields()) {
                    if (declaredField.getType().equals(File.class)) {
                        declaredField.setAccessible(true);
                        return (File) declaredField.get(world.getChunkSource().getDataStorage());
                    }
                }
                throw new RuntimeException("Ruins mod could not find field File DimensionSavedDataManager.dataFolder");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static File getMinecraftBaseDir() {
        return proxy.getBaseDir();
    }

    @SubscribeEvent
    public void onEnteringChunk(EntityEvent.EnteringChunk event) {
        /*
         * new concept of triggering Ruins generation: a player moving from one chunk to another shoots a "beam" several
         * chunks infront of them. at a certain minimum distance, this starts analyzing the chunk hit and its
         * surrounding chunks to try and spawn ruins in them. for performance, there can only be one such
         * beam executing at a time, it will never trigger worldgen, and we mark processed chunks by setting a block
         * in the bottom most/bedrock layer to some specific block
         */
        if (instance != null
                && event.getEntity() instanceof PlayerEntity
                && !event.getEntity().level.isClientSide()) {

            ServerWorld world;
            WorldHandle wh;
            if (event.getEntity().level instanceof ServerWorld) {
                world = (ServerWorld) event.getEntity().level;
                if (!world.structureFeatureManager().shouldGenerateFeatures()) {
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

            if (event.getNewChunkX() > event.getOldChunkX()) {
                xMove = 1;
            } else if (event.getNewChunkX() < event.getOldChunkX()) {
                xMove = -1;
            }
            if (event.getNewChunkZ() > event.getOldChunkZ()) {
                zMove = 1;
            } else if (event.getNewChunkZ() < event.getOldChunkZ()) {
                zMove = -1;
            }
            if (xMove == 0 && zMove == 0) {
                // no movement? how? ok, get outta here
                return;
            }

            // project the movement forward
            int projectedChunkX = event.getNewChunkX() + (xMove * 7);
            int projectedChunkZ = event.getNewChunkZ() + (zMove * 7);
            // iterate all the surrounding chunks from that
            for (int iterX = projectedChunkX - 2; iterX <= projectedChunkX + 2; iterX++) {
                for (int iterZ = projectedChunkZ - 2; iterZ <= projectedChunkZ + 2; iterZ++) {
                    // we dont want to spawn closer to the player, so do a simple min distance logic
                    int deltaX = iterX - event.getNewChunkX();
                    int deltaZ = iterZ - event.getNewChunkZ();
                    double euclidChunkDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                    if (euclidChunkDistance < 5) {
                        continue;
                    }
                    inspectChunk(world, new ChunkPos(iterX, iterZ), wh);
                }
            }
        }
    }

    private void inspectChunk(ServerWorld world, ChunkPos chunkPos, WorldHandle worldHandle) {

        if (!world.hasChunk(chunkPos.x, chunkPos.z)) {
            return;
        }

        // back in 1.16.5 minimum block height was simply zero
        BlockPos ruinsMarkerBlockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
        BlockState blockState = world.getBlockState(ruinsMarkerBlockPos);
        if (blockState.is(Blocks.BARRIER)) {
            return;
        }
        world.setBlock(ruinsMarkerBlockPos, Blocks.BARRIER.defaultBlockState(), 3);

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
        ConfigFolderPreparator.copyFromJarIfNotPresent(new File(getMinecraftBaseDir(), TEMPLATE_PATH_MC_EXTRACTED));
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