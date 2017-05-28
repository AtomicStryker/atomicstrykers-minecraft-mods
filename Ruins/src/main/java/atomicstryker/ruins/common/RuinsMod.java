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
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "AS_Ruins", name = "Ruins Mod", version = RuinsMod.modversion, dependencies = "after:ExtraBiomes")
public class RuinsMod
{
    public static final String modversion = "15.4.1";
    
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
    public void serverAboutToStarted(FMLServerAboutToStartEvent evt)
    {
    	//
        // Remove generators registered by a previous server run. If the user 
    	// starts a new single player session (e.g. in another save) the old 
    	// generators get invalid (since they point to the old save folder).
    	// 
    	// This cleanup has to be done before the first generate call (before 
    	// server start event), otherwise the spawn point block gets lost.
    	//
        generatorMap.clear();
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
                    event.block.getLocalizedName(), GameData.getBlockRegistry().getNameForObject(event.block), event.metadata)));
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
                        new ChatComponentText(String.format("BlockName [%s], blockID [%s], metadata [%d]", event.block.getLocalizedName(),
                                GameData.getBlockRegistry().getNameForObject(event.block), event.blockMetadata)));
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
                    if (tecb.getDistanceFrom(x, y, z) < 4096.0) //square dist!
                    {
                        if (tecb.func_145993_a().func_145753_i().startsWith("RUINSTRIGGER "))
                        {
                            // strip prefix from command
                            tecb.func_145993_a().func_145752_a(tecb.func_145993_a().func_145753_i().substring(13));
                            // call command block execution
                            tecb.func_145993_a().func_145755_a(event.entity.worldObj);
                            tecblistToDelete.add(tecb);
                        }
                    }
                }
            }
            
            for (TileEntityCommandBlock tecb2 : tecblistToDelete)
            {
                // kill block
                System.out.printf("Ruins executed and killed Command Block at [%d|%d|%d]\n", tecb2.xCoord, tecb2.yCoord, tecb2.zCoord);
                event.entity.worldObj.setBlockToAir(tecb2.xCoord, tecb2.yCoord, tecb2.zCoord);
            }
        }
    }

    public class RuinsWorldGenerator implements IWorldGenerator
    {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
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
                    if (wh.fileHandle.allowsDimension(world.provider.dimensionId) && !getWorldHandle(world).chunkLogger.catchChunkBug(chunkX, chunkZ))
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
    	try {
	        WorldHandle wh = getWorldHandle(world);
	        if (wh.fileHandle != null)
	        {
	        	wh.waitLoaded();
	        	wh.generator.generateNether(world, random, chunkX, 0, chunkZ);
	        }
    	} catch (InterruptedException e) {
    		// shutting down?
			e.printStackTrace();
    	}
    }

    private void generateSurface(World world, Random random, int chunkX, int chunkZ)
    {
    	try {
	        WorldHandle wh = getWorldHandle(world);
	        if (wh.fileHandle != null)
	        {
	        	wh.waitLoaded();
	            wh.generator.generateNormal(world, random, chunkX, 0, chunkZ);
	        }
    	} catch (InterruptedException e) {
    		// shutting down?
			e.printStackTrace();
    	}
    }

    private class WorldHandle
    {
		FileHandler fileHandle;
        RuinGenerator generator;
        ChunkLoggerData chunkLogger;
		int dimension;
		private boolean loaded = false;
		
		public void waitLoaded() throws InterruptedException {
			if (!loaded) {
				fileHandle.waitLoaded();
				generator.waitLoaded();
				loaded = true;
			}
		}
    }

    private WorldHandle getWorldHandle(World world)
    {
        WorldHandle wh = null;
        if (!world.isRemote)
        {
            if (!generatorMap.containsKey(world.provider.dimensionId))
            {
                wh = new WorldHandle();
                initWorldHandle(wh, world);
                generatorMap.put(world.provider.dimensionId, wh);
            }
            else
            {
                wh = generatorMap.get(world.provider.dimensionId);
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
        	worldHandle.dimension = world.provider.dimensionId;
            File worlddir = getWorldSaveDir(world);
            worldHandle.fileHandle = new FileHandler(worlddir);
            worldHandle.generator = new RuinGenerator(worldHandle.fileHandle, world);
            
            worldHandle.chunkLogger = (ChunkLoggerData) world.perWorldStorage.loadData(ChunkLoggerData.class, "ruinschunklogger");
            if (worldHandle.chunkLogger == null)
            {
                worldHandle.chunkLogger = new ChunkLoggerData("ruinschunklogger");
                world.perWorldStorage.setData("ruinschunklogger", worldHandle.chunkLogger);
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