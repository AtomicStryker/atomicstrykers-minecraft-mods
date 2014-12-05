package atomicstryker.ruins.common;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.biome.BiomeGenBase;
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
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "AS_Ruins", name = "Ruins Mod", version = RuinsMod.modversion, dependencies = "after:ExtraBiomes")
public class RuinsMod
{
    public static final String modversion = "14.5";
    
    public final static String TEMPLATE_EXT = "tml";
    public final static int DIR_NORTH = 0, DIR_EAST = 1, DIR_SOUTH = 2, DIR_WEST = 3;
    public static final int BIOME_NONE = 500;

    private ConcurrentHashMap<Integer, WorldHandle> generatorMap;
    private ConcurrentLinkedQueue<int[]> currentlyGenerating;
    
    @NetworkCheckHandler
    public boolean checkModLists(Map<String,String> modList, Side side)
    {
        return true;
    }
    
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        generatorMap = new ConcurrentHashMap<Integer, WorldHandle>();
        currentlyGenerating = new ConcurrentLinkedQueue<int[]>();
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
        ItemStack is = event.entityPlayer.getCurrentEquippedItem();
        if (is != null && is.getItem() == Items.stick && System.currentTimeMillis() > nextInfoTime)
        {
            nextInfoTime = System.currentTimeMillis() + 1000l;
            event.entityPlayer.addChatComponentMessage(new ChatComponentText(String.format("BlockName [%s], blockID [%s], metadata [%d]",
                    event.state.getBlock().getLocalizedName(), GameData.getBlockRegistry().getNameForObject(event.state.getBlock()), event.state.getBlock().getMetaFromState(event.state))));
        }
    }

    @SubscribeEvent
    public void onBreak(BreakEvent event)
    {
        if (event.getPlayer() != null && !(event.getPlayer() instanceof FakePlayer))
        {
            ItemStack is = event.getPlayer().getCurrentEquippedItem();
            if (is != null && is.getItem() == Items.stick && System.currentTimeMillis() > nextInfoTime)
            {
                nextInfoTime = System.currentTimeMillis() + 1000l;
                event.getPlayer().addChatComponentMessage(
                        new ChatComponentText(String.format("BlockName [%s], blockID [%s], metadata [%d]", event.state.getBlock().getLocalizedName(),
                                GameData.getBlockRegistry().getNameForObject(event.state.getBlock()), event.state.getBlock().getMetaFromState(event.state))));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void eventWorldSave(WorldEvent.Save evt)
    {
        WorldHandle wh = getWorldHandle(evt.world);
        if (wh != null)
        {
            wh.generator.flushPosFile(evt.world.getWorldInfo().getWorldName());
        }
    }
    
    @SubscribeEvent
    public void onEntityEnteringChunk(EntityEvent.EnteringChunk event)
    {
        if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote)
        {
            TileEntityCommandBlock tecb;
            final double x = event.entity.posX;
            final double y = event.entity.posY;
            final double z = event.entity.posZ;
            ArrayList<TileEntityCommandBlock> tecblistToDelete = new ArrayList<TileEntityCommandBlock>();
            @SuppressWarnings("unchecked")
            ArrayList<Object> telist = new ArrayList<Object>((List<Object>)event.entity.worldObj.loadedTileEntityList);
            for (Object teo : telist)
            {
                if (teo instanceof TileEntityCommandBlock)
                {
                    tecb = (TileEntityCommandBlock) teo;
                    if (tecb.getDistanceSq(x, y, z) < 4096.0) //square dist!
                    {
                        if (tecb.getCommandBlockLogic().getCustomName().startsWith("RUINSTRIGGER "))
                        {
                            // strip prefix from command
                            tecb.getCommandBlockLogic().setCommand((tecb.getCommandBlockLogic().getCustomName().substring(13)));
                            // call command block execution
                            tecb.getCommandBlockLogic().trigger(event.entity.worldObj);
                            tecblistToDelete.add(tecb);
                        }
                    }
                }
            }
            
            for (TileEntityCommandBlock tecb2 : tecblistToDelete)
            {
                // kill block
            	BlockPos pos = tecb2.getPos();
                System.out.printf("Ruins executed and killed Command Block at [%s]\n", pos);
                event.entity.worldObj.setBlockToAir(pos);
            }
        }
    }

    public class RuinsWorldGenerator implements IWorldGenerator
    {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
        {
            if (world.isRemote)
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
                    if (wh.fileHandle.allowsDimension(world.provider.getDimensionId()) && !getWorldHandle(world).chunkLogger.catchChunkBug(chunkX, chunkZ))
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
            for (; !wh.fileHandle.loaded; Thread.yield())
            {
            }
            wh.generator.generateNether(world, random, chunkX, 0, chunkZ);
        }
    }

    private void generateSurface(World world, Random random, int chunkX, int chunkZ)
    {
        WorldHandle wh = getWorldHandle(world);
        if (wh.fileHandle != null)
        {
            for (; !wh.fileHandle.loaded; Thread.yield())
            {
            }
            wh.generator.generateNormal(world, random, chunkX, 0, chunkZ);
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
            if (!generatorMap.containsKey(world.provider.getDimensionId()))
            {
                wh = new WorldHandle();
                initWorldHandle(wh, world);
                generatorMap.put(world.provider.getDimensionId(), wh);
            }
            else
            {
                wh = generatorMap.get(world.provider.getDimensionId());
            }
        }

        return wh;
    }

    public static File getWorldSaveDir(World world)
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
                        File saveLoc = (File) f.get(loader);
                        // System.out.println("Ruins mod determines World Save Dir to be at: "+saveLoc);
                        return saveLoc;
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
            return FMLClientHandler.instance().getClient().mcDataDir;
        }
        return FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
    }

    public static int getBiomeFromName(String name)
    {
        for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++)
        {
            if (BiomeGenBase.getBiomeGenArray()[i] != null && BiomeGenBase.getBiomeGenArray()[i].biomeName.equalsIgnoreCase(name))
            {
                return BiomeGenBase.getBiomeGenArray()[i].biomeID;
            }
        }

        return -1;
    }

    private void initWorldHandle(WorldHandle worldHandle, World world)
    {
        // load in defaults
        try
        {
            File worlddir = getWorldSaveDir(world);
            worldHandle.fileHandle = new FileHandler(worlddir);
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world);
            
            worldHandle.chunkLogger = (ChunkLoggerData) world.getPerWorldStorage().loadData(ChunkLoggerData.class, "ruinschunklogger");
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