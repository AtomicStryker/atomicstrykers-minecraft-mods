package atomicstryker.ruins.common;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "ruins", name = "Ruins Mod", version = RuinsMod.modversion, dependencies = "after:extrabiomes")
public class RuinsMod
{
    static final String modversion = "16.5";

    public final static int DIR_NORTH = 0, DIR_EAST = 1, DIR_SOUTH = 2, DIR_WEST = 3;
    public static final String BIOME_ANY = "generic";

    private ConcurrentHashMap<Integer, WorldHandle> generatorMap;
    private ConcurrentLinkedQueue<int[]> currentlyGenerating;

    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> modList, Side side)
    {
        return true;
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        generatorMap = new ConcurrentHashMap<>();
        currentlyGenerating = new ConcurrentLinkedQueue<>();
        GameRegistry.registerWorldGenerator(new RuinsWorldGenerator(), 0);
        MinecraftForge.EVENT_BUS.register(this);

        new CustomRotationMapping(new File(getMinecraftBaseDir(), "mods/resources/ruins"));
    }

    @EventHandler
    public void serverStarted(FMLServerStartingEvent evt)
    {
        evt.registerServerCommand(new CommandParseTemplate());
        evt.registerServerCommand(new CommandTestTemplate());
        evt.registerServerCommand(new CommandUndo());
    }

    private long nextInfoTime;

    @SubscribeEvent
    public void onBreakSpeed(BreakSpeed event)
    {
        ItemStack is = event.getEntityPlayer().getHeldItemMainhand();
        if (is != null && is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime)
        {
            nextInfoTime = System.currentTimeMillis() + 1000L;
            event.getEntityPlayer().sendMessage(new TextComponentTranslation(String.format("BlockName [%s], blockID [%s], metadata [%d]", event.getState().getBlock().getLocalizedName(),
                    event.getState().getBlock().getRegistryName().getResourcePath(), event.getState().getBlock().getMetaFromState(event.getState()))));
        }
    }

    @SubscribeEvent
    public void onBreak(BreakEvent event)
    {
        if (event.getPlayer() != null && !(event.getPlayer() instanceof FakePlayer))
        {
            ItemStack is = event.getPlayer().getHeldItemMainhand();
            if (is != null && is.getItem() == Items.STICK && System.currentTimeMillis() > nextInfoTime)
            {
                nextInfoTime = System.currentTimeMillis() + 1000L;
                event.getPlayer().sendMessage(new TextComponentTranslation(String.format("BlockName [%s], blockID [%s], metadata [%d]", event.getState().getBlock().getLocalizedName(),
                        event.getState().getBlock().getRegistryName().getResourcePath(), event.getState().getBlock().getMetaFromState(event.getState()))));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void eventWorldSave(WorldEvent.Save evt)
    {
        WorldHandle wh = getWorldHandle(evt.getWorld());
        if (wh != null)
        {
            wh.generator.flushPosFile(evt.getWorld().getWorldInfo().getWorldName());
        }
    }

    @SubscribeEvent
    public void onEntityEnteringChunk(EntityEvent.EnteringChunk event)
    {
        if (event.getEntity() instanceof EntityPlayer && !event.getEntity().world.isRemote)
        {
            TileEntityCommandBlock tecb;
            ArrayList<TileEntityCommandBlock> tecblist = new ArrayList<>();

            for (int xoffset = -4; xoffset <= 4; xoffset++)
            {
                for (int zoffset = -4; zoffset <= 4; zoffset++)
                {
                    for (TileEntity teo : event.getEntity().world.getChunkFromChunkCoords(event.getNewChunkX() + xoffset, event.getNewChunkZ() + zoffset).getTileEntityMap().values())
                    {
                        if (teo instanceof TileEntityCommandBlock)
                        {
                            tecb = (TileEntityCommandBlock) teo;
                            if (tecb.getCommandBlockLogic().getCommand().startsWith("RUINSTRIGGER "))
                            {
                                // strip prefix from command
                                tecb.getCommandBlockLogic().setCommand((tecb.getCommandBlockLogic().getCommand()).substring(13));
                                tecblist.add(tecb);
                            }
                        }
                    }
                }
            }

            for (TileEntityCommandBlock tecb2 : tecblist)
            {
                // call command block execution
                tecb2.getCommandBlockLogic().trigger(event.getEntity().world);
                // kill block
                BlockPos pos = tecb2.getPos();
                System.out.printf("Ruins executed and killed Command Block at [%s]\n", pos);
                event.getEntity().world.setBlockToAir(pos);
            }
        }
    }

    private class RuinsWorldGenerator implements IWorldGenerator
    {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
        {
            if (world.isRemote || !world.getWorldInfo().isMapFeaturesEnabled())
            {
                return;
            }

            int[] tuple = { chunkX, chunkZ };
            if (currentlyGenerating.contains(tuple))
            {
                System.err.printf("Ruins Mod caught recursive generator call at chunk [%d|%d]", chunkX, chunkZ);
            }
            else
            {
                WorldHandle wh = getWorldHandle(world);
                if (wh != null)
                {
                    if (wh.fileHandle.allowsDimension(world.provider.getDimension()) && !getWorldHandle(world).chunkLogger.catchChunkBug(chunkX, chunkZ))
                    {
                        currentlyGenerating.add(tuple);
                        if (world.provider instanceof WorldProviderHell)
                        {
                            generateNether(world, random, tuple[0] * 16, tuple[1] * 16);
                        }
                        else
                        // normal world
                        {
                            generateSurface(world, random, tuple[0] * 16, tuple[1] * 16);
                        }
                        currentlyGenerating.remove(tuple);
                    }
                }
            }
        }
    }

    private void generateNether(World world, Random random, int chunkX, int chunkZ)
    {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null)
        {
            while (!wh.fileHandle.loaded)
            {
                Thread.yield();
            }
            wh.generator.generateNether(world, random, chunkX, chunkZ);
        }
    }

    private void generateSurface(World world, Random random, int chunkX, int chunkZ)
    {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null)
        {
            while (!wh.fileHandle.loaded)
            {
                Thread.yield();
            }
            wh.generator.generateNormal(world, random, chunkX, chunkZ);
        }
    }

    private class WorldHandle
    {
        FileHandler fileHandle;
        RuinGenerator generator;
        ChunkLoggerData chunkLogger;
    }

    private WorldHandle getWorldHandle(World world)
    {
        WorldHandle wh = null;
        if (!world.isRemote)
        {
            if (!generatorMap.containsKey(world.provider.getDimension()))
            {
                wh = new WorldHandle();
                initWorldHandle(wh, world);
                generatorMap.put(world.provider.getDimension(), wh);
            }
            else
            {
                wh = generatorMap.get(world.provider.getDimension());
            }
        }

        return wh;
    }

    private static File getWorldSaveDir(World world)
    {
        ISaveHandler worldsaver = world.getSaveHandler();

        if (worldsaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader)
        {
            AnvilChunkLoader loader = (AnvilChunkLoader) worldsaver.getChunkLoader(world.provider);

            for (Field f : loader.getClass().getDeclaredFields())
            {
                if (f.getType().equals(File.class))
                {
                    try
                    {
                        f.setAccessible(true);
                        // System.out.println("Ruins mod determines World Save
                        // Dir to be at: "+saveLoc);
                        return (File) f.get(loader);
                    }
                    catch (Exception e)
                    {
                        System.out.println("Ruins mod failed trying to find World Save dir:");
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    public static File getMinecraftBaseDir()
    {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            File file = FMLClientHandler.instance().getClient().mcDataDir;
            String abspath = file.getAbsolutePath();
            if (abspath.endsWith("."))
            {
                file = new File(abspath.substring(0, abspath.length() - 1));
            }
            return file;
        }
        return FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
    }

    private void initWorldHandle(WorldHandle worldHandle, World world)
    {
        // load in defaults
        try
        {
            File worlddir = getWorldSaveDir(world);
            worldHandle.fileHandle = new FileHandler(worlddir, world.provider.getDimension());
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world);

            worldHandle.chunkLogger = (ChunkLoggerData) world.getPerWorldStorage().getOrLoadData(ChunkLoggerData.class, "ruinschunklogger");
            if (worldHandle.chunkLogger == null)
            {
                worldHandle.chunkLogger = new ChunkLoggerData("ruinschunklogger");
                world.getPerWorldStorage().setData("ruinschunklogger", worldHandle.chunkLogger);
            }
        }
        catch (Exception e)
        {
            System.err.println("There was a problem loading the ruins mod:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}