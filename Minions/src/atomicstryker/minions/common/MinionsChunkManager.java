package atomicstryker.minions.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    private final int CHUNK_LENGTH = 16;
    private final int LOAD_CHUNKS_IN_ALL_DIRECTIONS = 2;

    private ArrayList<Entity> loaderEntities;
    private Set<ChunkCoordIntPair> loadedChunks;

    public MinionsChunkManager()
    {
        loaderEntities = new ArrayList<Entity>();
        loadedChunks = new HashSet<ChunkCoordIntPair>();
    }

    public void registerChunkLoaderEntity(Entity ent)
    {
        loaderEntities.add(ent);
    }

    public void updateLoadedChunks()
    {
        loadedChunks.clear();
        Iterator<Entity> iter = loaderEntities.iterator();
        while (iter.hasNext())
        {
            Entity ent = iter.next();
            if (ent.isDead)
            {
                iter.remove();
            }
            else
            {
                loadChunksAroundCoords(ent.worldObj, MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posZ));
            }
        }
    }

    public void onWorldUnloaded()
    {
        loadedChunks.clear();
        loaderEntities.clear();
    }

    private void loadChunksAroundCoords(World world, int x, int z)
    {
        if (world != null)
        {
            for (int xIter = -LOAD_CHUNKS_IN_ALL_DIRECTIONS; xIter <= LOAD_CHUNKS_IN_ALL_DIRECTIONS; xIter++)
            {
                for (int zIter = -LOAD_CHUNKS_IN_ALL_DIRECTIONS; zIter <= LOAD_CHUNKS_IN_ALL_DIRECTIONS; zIter++)
                {
                    loadChunkAtCoords(world, x + (xIter * CHUNK_LENGTH), z + (zIter * CHUNK_LENGTH));
                }
            }
        }
    }

    private void loadChunkAtCoords(World world, int x, int z)
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
