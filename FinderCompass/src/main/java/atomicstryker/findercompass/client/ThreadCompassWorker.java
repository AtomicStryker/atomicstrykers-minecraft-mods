package atomicstryker.findercompass.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import atomicstryker.findercompass.common.CompassTargetData.BlockData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChunkCoordinates;

/**
 * Runnable worker class for finding Blocks
 * 
 * @author AtomicStryker
 */

public class ThreadCompassWorker extends Thread
{
	private Minecraft mcinstance;
	private boolean isRunning = false;
	
	public ThreadCompassWorker(Minecraft mc, FinderCompassLogic creator)
	{
		mcinstance = mc;
	}
	
	private Set<BlockData> mBlocks;
	private int[] intArray;
	private String mOreDictName;
	
	public void setupValues(Set<BlockData> pBlocks, int[] configInts, String pOreDictName)
	{
	    mBlocks = pBlocks;
		intArray = configInts;
		mOreDictName = pOreDictName;
	}
	
	public boolean isWorking()
	{
		return isRunning;
	}

	@Override
	public void run()
	{
		isRunning = true;
		
		// search!
		ChunkCoordinates result = findNearestBlockChunkOfIDInRange(mBlocks, intArray[1], intArray[2], intArray[3], intArray[4], intArray[5], intArray[6], intArray[7]);
		
		if (result != null)
		{
		    FinderCompassClientTicker.instance.onFoundChunkCoordinates(result, mBlocks, mOreDictName);
		}
		
		isRunning = false;
	}
	
    private ChunkCoordinates findNearestBlockChunkOfIDInRange(Set<BlockData> blocks, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY)
    {
        List<ChunkCoordinates> blocksInRange = this.findBlocksOfIDInRange(blocks, playerX, playerY, playerZ, xzRange, yRange, minY, maxY);
        ChunkCoordinates playerCoords = new ChunkCoordinates(playerX, playerY, playerZ);
        ChunkCoordinates resultCoords = new ChunkCoordinates(0, 0, 0);
        double minDist = 9999.0D;

        for (int i = 0; i < blocksInRange.size(); ++i)
        {
            ChunkCoordinates coords = (ChunkCoordinates)blocksInRange.get(i);
            double localDist = this.getDistanceBetweenChunks(playerCoords, coords);
            if (localDist < minDist)
            {
                resultCoords = coords;
                minDist = localDist;
            }
        }
        
        //System.out.printf("Compassworker found stuff of id %s at [%d|%d|%d]\n", blockID, resultCoords.posX, resultCoords.posY, resultCoords.posZ);
        return resultCoords;
    }

    private List<ChunkCoordinates> findBlocksOfIDInRange(Set<BlockData> blocks, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY)
    {
        ArrayList<ChunkCoordinates> resultList = new ArrayList<ChunkCoordinates>();

        for (int yIter = playerY - yRange - 1; yIter <= playerY + yRange; ++yIter)
        {
            if (yIter >= minY && yIter <= maxY)
            {
                for (int zIter = playerZ - xzRange; zIter <= playerZ + xzRange; ++zIter)
                {
                    for (int xIter = playerX - xzRange; xIter <= playerX + xzRange; ++xIter)
                    {
                    	Block blockInWorld = this.mcinstance.theWorld.getBlock(xIter, yIter, zIter);
                    	for (BlockData blockData : blocks)
                    	{
                    		if (blockInWorld == blockData.getBlockID())
                    		{
                    			if (!blockData.useDamage() || mcinstance.theWorld.getBlockMetadata(xIter, yIter, zIter) == blockData.getDamage())
                    			{
                    				resultList.add(new ChunkCoordinates(xIter, yIter, zIter));
                    			}
                    		}
                    		Thread.yield();
                    	}
                    }
                }
            }
        }

        return resultList;
    }
    
    private double getDistanceBetweenChunks(ChunkCoordinates var1, ChunkCoordinates var2)
    {
        int xdiff = Math.abs(var1.posX - var2.posX);
        int ydiff = Math.abs(var1.posY - var2.posY);
        int zdiff = Math.abs(var1.posZ - var2.posZ);
        return Math.sqrt(Math.pow((double)xdiff, 2.0D) + Math.pow((double)ydiff, 2.0D) + Math.pow((double)zdiff, 2.0D));
    }
}