package atomicstryker.minions.common;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.ChunkEvent;
import atomicstryker.minions.common.entity.EntityMinion;

public class MinionsChunkManager
{
	private static final int CHUNK_LENGTH = 16;
	private static final int LOAD_CHUNKS_IN_ALL_DIRECTIONS = 2;
	
	private static ConcurrentHashMap<Entity, Byte> loaderEntities;
	private static Set<ChunkCoordIntPair> loadedChunks;
	
	public MinionsChunkManager()
	{
		loaderEntities = new ConcurrentHashMap<Entity, Byte>();
		loadedChunks = new HashSet<ChunkCoordIntPair>();
	}
	
	public static void registerChunkLoaderEntity(Entity ent)
	{
		loaderEntities.put(ent, (byte) 0);
	}
	
	public static void unRegisterChunkLoaderEntity(Entity ent)
	{
		loaderEntities.remove(ent);
	}
	
	public static void updateLoadedChunks()
	{
		loadedChunks.clear();
		for (Entity ent : loaderEntities.keySet())
		{
			loadChunksAroundCoords(ent.worldObj, MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posZ));
		}
	}
	
	public static void onWorldUnloaded()
	{
	    loadedChunks.clear();
	    loaderEntities.clear();
	}
	
	private static void loadChunksAroundCoords(World world, int x, int z)
	{
	    if (world != null)
	    {
	        for (int xIter = -LOAD_CHUNKS_IN_ALL_DIRECTIONS; xIter <= LOAD_CHUNKS_IN_ALL_DIRECTIONS; xIter++)
	        {
	            for (int zIter = -LOAD_CHUNKS_IN_ALL_DIRECTIONS; zIter <= LOAD_CHUNKS_IN_ALL_DIRECTIONS; zIter++)
	            {
	                loadChunkAtCoords(world, x + (xIter*CHUNK_LENGTH), z + (zIter*CHUNK_LENGTH));
	            }
	        }
	    }
	}
	
	private static void loadChunkAtCoords(World world, int x, int z)
	{
		loadedChunks.add(world.getChunkFromBlockCoords(x, z).getChunkCoordIntPair());
	}
	
	@ForgeSubscribe
	public void canUnloadChunk(ChunkEvent.Load event)
	{
		if (loadedChunks.contains(event.getChunk().getChunkCoordIntPair()) && event.isCancelable())
		{
			event.setCanceled(true);
		}
	}

	@ForgeSubscribe
	public void canUpdateEntity(EntityEvent.CanUpdate event)
	{
		event.canUpdate = event.entity instanceof EntityMinion;
	}
}
