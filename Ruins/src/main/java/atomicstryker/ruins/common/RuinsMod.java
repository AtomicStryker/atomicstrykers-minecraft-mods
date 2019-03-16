package atomicstryker.ruins.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod(RuinsMod.MOD_ID)
@Mod.EventBusSubscriber(modid = RuinsMod.MOD_ID, value = Dist.DEDICATED_SERVER)
public class RuinsMod {
    static final String MOD_ID = "ruins";

    static final String modversion = "17.2";

    public static final String TEMPLATE_PATH_MC_EXTRACTED = "config/ruins_config/";
    public static final String TEMPLATE_PATH_JAR = "ruins_config";

    public final static int DIR_NORTH = 0, DIR_EAST = 1, DIR_SOUTH = 2, DIR_WEST = 3;
    public static final String BIOME_ANY = "generic";

    private ConcurrentHashMap<Integer, WorldHandle> generatorMap;

    public RuinsMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::serverAboutToStart);
        modEventBus.addListener(this::serverStarted);
    }

    public void preInit(FMLCommonSetupEvent evt) {
        GameRegistry.registerWorldGenerator(new RuinsWorldGenerator(), 0);
        ConfigFolderPreparator.copyFromJarIfNotPresent(this, new File(getMinecraftBaseDir(), TEMPLATE_PATH_MC_EXTRACTED));
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent evt) {
        generatorMap = new ConcurrentHashMap<>();
    }

    public void serverStarted(FMLServerStartingEvent evt) {
        evt.getCommandDispatcher().register(CommandParseTemplate.BUILDER);
        evt.getCommandDispatcher().register(CommandTestTemplate.BUILDER);
        evt.getCommandDispatcher().register(CommandUndoTemplate.BUILDER);
    }

    private long nextInfoTime;

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        WorldHandle wh = getWorldHandle(event.getEntity().getEntityWorld());
        if (wh != null && wh.fileHandle.enableStick) {
            ItemStack is = event.getEntityPlayer().getHeldItemMainhand();
            if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                nextInfoTime = System.currentTimeMillis() + 1000L;
                event.getEntityPlayer().sendMessage(new TextComponentTranslation(String.format("BlockName [%s], blockID [%s], metadata [%d]", event.getState().getBlock().getUnlocalizedName(),
                        event.getState().getBlock().getRegistryName().getResourcePath(), event.getState().getBlock().getMetaFromState(event.getState()))));
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && !(event.getPlayer() instanceof FakePlayer)) {
            WorldHandle wh = getWorldHandle(event.getWorld());
            if (wh != null && wh.fileHandle.enableStick) {
                ItemStack is = event.getPlayer().getHeldItemMainhand();
                if (is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime) {
                    nextInfoTime = System.currentTimeMillis() + 1000L;
                    event.getPlayer().sendMessage(new TextComponentTranslation(String.format("BlockName [%s], blockID [%s], metadata [%d]", event.getState().getBlock().getUnlocalizedName(),
                            event.getState().getBlock().getRegistryName().getResourcePath(), event.getState().getBlock().getMetaFromState(event.getState()))));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void eventWorldSave(WorldEvent.Save evt) {
        WorldHandle wh = getWorldHandle(evt.getWorld());
        if (wh != null) {
            wh.generator.flushPosFile(evt.getWorld().getWorldInfo().getWorldName());
        }
    }

    @SubscribeEvent
    public void onEntityEnteringChunk(EntityEvent.EnteringChunk event) {
        if (event.getEntity() instanceof EntityPlayer && !event.getEntity().world.isRemote) {
            TileEntityCommandBlock tecb;
            ArrayList<TileEntityCommandBlock> tecblist = new ArrayList<>();

            for (int xoffset = -4; xoffset <= 4; xoffset++) {
                for (int zoffset = -4; zoffset <= 4; zoffset++) {
                    for (TileEntity teo : event.getEntity().world.getChunk(event.getNewChunkX() + xoffset, event.getNewChunkZ() + zoffset).getTileEntityMap().values()) {
                        if (teo instanceof TileEntityCommandBlock) {
                            tecb = (TileEntityCommandBlock) teo;
                            if (tecb.getCommandBlockLogic().getCommand().startsWith("RUINSTRIGGER ")) {
                                // strip prefix from command
                                tecb.getCommandBlockLogic().setCommand((tecb.getCommandBlockLogic().getCommand()).substring(13));
                                tecblist.add(tecb);
                            }
                        }
                    }
                }
            }

            for (TileEntityCommandBlock tecb2 : tecblist) {
                // call command block execution
                tecb2.getCommandBlockLogic().trigger(event.getEntity().world);
                // kill block
                BlockPos pos = tecb2.getPos();
                System.out.printf("Ruins executed and killed Command Block at [%s]\n", pos);
                event.getEntity().world.removeBlock(pos);
            }
        }
    }

    private class RuinsWorldGenerator implements IWorldGenerator {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            if (world.isRemote || !world.getWorldInfo().isMapFeaturesEnabled()) {
                return;
            }

            final WorldHandle wh = getWorldHandle(world);
            if (wh != null) {
                int[] tuple = {chunkX, chunkZ};
                if (wh.currentlyGenerating.contains(tuple)) {
                    System.err.printf("Ruins Mod caught recursive generator call at chunk [%d|%d]", chunkX, chunkZ);
                } else {
                    if (wh.fileHandle.allowsDimension(world.getWorldInfo().getDimension()) && !wh.chunkLogger.catchChunkBug(chunkX, chunkZ)) {
                        wh.currentlyGenerating.add(tuple);
                        if (world.getDimension().isNether()) {
                            generateNether(world, random, tuple[0] * 16, tuple[1] * 16);
                        } else
                        // normal world
                        {
                            generateSurface(world, random, tuple[0] * 16, tuple[1] * 16);
                        }
                        wh.currentlyGenerating.remove(tuple);
                    }
                }
            }
        }
    }

    private void generateNether(World world, Random random, int chunkX, int chunkZ) {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null) {
            while (!wh.fileHandle.loaded) {
                Thread.yield();
            }
            wh.generator.generateNether(world, random, chunkX, chunkZ);
        }
    }

    private void generateSurface(World world, Random random, int chunkX, int chunkZ) {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null) {
            while (!wh.fileHandle.loaded) {
                Thread.yield();
            }
            wh.generator.generateNormal(world, random, chunkX, chunkZ);
        }
    }

    private class WorldHandle {
        FileHandler fileHandle;
        RuinGenerator generator;
        ConcurrentLinkedQueue<int[]> currentlyGenerating;
        ChunkLoggerData chunkLogger;
    }

    private WorldHandle getWorldHandle(IWorld worldInterface) {
        WorldHandle wh = null;
        if (!worldInterface.getWorld().isRemote) {
            if (!generatorMap.containsKey(worldInterface.getWorldInfo().getDimension())) {
                wh = new WorldHandle();
                initWorldHandle(wh, worldInterface);
                generatorMap.put(worldInterface.getWorldInfo().getDimension(), wh);
            } else {
                wh = generatorMap.get(worldInterface.getWorldInfo().getDimension());
            }
        }

        return wh;
    }

    private static File getWorldSaveDir(IWorld world) {
        ISaveHandler worldsaver = world.getSaveHandler();

        if (worldsaver.getChunkLoader(world.getWorld().getDimension()) instanceof AnvilChunkLoader) {
            AnvilChunkLoader loader = (AnvilChunkLoader) worldsaver.getChunkLoader(world.getWorld().getDimension());

            for (Field f : loader.getClass().getDeclaredFields()) {
                if (f.getType().equals(File.class)) {
                    try {
                        f.setAccessible(true);
                        // System.out.println("Ruins mod determines World Save
                        // Dir to be at: "+saveLoc);
                        return (File) f.get(loader);
                    } catch (Exception e) {
                        System.out.println("Ruins mod failed trying to find World Save dir:");
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static File getMinecraftBaseDir() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            File file = Minecraft.getInstance().gameDir;
            String abspath = file.getAbsolutePath();
            if (abspath.endsWith(".")) {
                file = new File(abspath.substring(0, abspath.length() - 1));
            }
            return file;
        }
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getFile("");
    }

    private void initWorldHandle(WorldHandle worldHandle, IWorld world) {
        // load in defaults
        try {
            File worlddir = getWorldSaveDir(world);
            worldHandle.fileHandle = new FileHandler(worlddir, world.getWorldInfo().getDimension());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world.getWorld());
            worldHandle.currentlyGenerating = new ConcurrentLinkedQueue<>();

            if (world.getWorld().getSavedDataStorage() != null) {
                worldHandle.chunkLogger = world.getWorld().getSavedDataStorage().get(DimensionType.OVERWORLD, ChunkLoggerData::new, "ruinschunklogger");
            }
        } catch (Exception e) {
            System.err.println("There was a problem loading the ruins mod:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}