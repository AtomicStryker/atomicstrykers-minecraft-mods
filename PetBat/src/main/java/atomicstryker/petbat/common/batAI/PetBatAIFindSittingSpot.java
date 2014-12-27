package atomicstryker.petbat.common.batAI;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import atomicstryker.petbat.common.EntityPetBat;

public class PetBatAIFindSittingSpot extends EntityAIBase
{
    private final double OWNER_DISTANCE_ALLOWED_SQ = 36D;
    private final int SEARCH_BELOW_RANGE = 1;
    private final int SEARCH_ABOVE_RANGE = 2;
    private final int SEARCH_XZ_RANGE = 4;
    private final long SEARCH_COOLDOWN_TIME = 5000L;
    
    private final EntityPetBat petBat;
    private boolean isSearching;
    private HangSpotSearch searchThread;
    private long nextSearchTime;
    
    public PetBatAIFindSittingSpot(EntityPetBat bat)
    {
        isSearching = false;
        searchThread = null;
        petBat = bat;
        nextSearchTime = 0L;
    }
    
    @Override
    public boolean shouldExecute()
    {
        return !isSearching
            && !petBat.getIsBatHanging()
            && petBat.getHangingSpot() == null
            && !petBat.getHasTarget()
            && checkOwnerNearby()
            && System.currentTimeMillis() > nextSearchTime;
    }
    
    private boolean checkOwnerNearby()
    {
        if (petBat.getOwnerEntity() != null
        && petBat.getDistanceSqToEntity(petBat.getOwnerEntity()) > OWNER_DISTANCE_ALLOWED_SQ)
        {
            return false;
        }
        
        return true;
    }

    @Override
    public void startExecuting()
    {
        if (searchThread == null || !searchThread.isAlive())
        {
            searchThread = null;
            startSearch();
        }
        
        super.startExecuting();
    }
    
    @Override
    public void resetTask()
    {        
        super.resetTask();
    }
    
    private synchronized void startSearch()
    {
        if (searchThread == null)
        {
            searchThread = new HangSpotSearch();
            isSearching = true;
            nextSearchTime = System.currentTimeMillis() + SEARCH_COOLDOWN_TIME;
            searchThread.setPriority(Thread.MIN_PRIORITY);
            searchThread.start();
        }
    }
    
    private class HangSpotSearch extends Thread
    {
        int minY;
        int maxY;
        int startX;
        int maxX;
        int startZ;
        int curX;
        int curZ;
        /**
         * 0 = north
         * 1 = east
         * 2 = south
         * 3 = west
         */
        int direction;
        /**
         * How many steps in each direction, rises by 1 every 2 moves
         */
        int stepLength;
        /**
         * Steps left to do in current direction
         */
        int stepsToDo;
        
        @Override
        public void run()
        {
            minY = (int)(petBat.posY+0.5D) - SEARCH_BELOW_RANGE;
            maxY = minY + SEARCH_ABOVE_RANGE*2;
            
            startX = (int)(petBat.posX+0.5D);
            maxX = startX + SEARCH_XZ_RANGE;
            
            startZ = (int)(petBat.posZ+0.5D);
            
            World w = petBat.worldObj;
            for (int y = minY; y <= maxY; y++)
            {
                curX = startX;
                curZ = startZ;
                direction = 0;
                stepLength = 1;
                stepsToDo = 1;
                
                for(;;)
                {
                    // arrived at top left corner of search grid, break out of this level
                    if (curX == maxX && direction == 0 && stepsToDo == 1)
                    {
                        // System.out.println("finished a level, debugCounter: "+debugCounter);
                        break;
                    }
                    
                    if (w.isAirBlock(new BlockPos(curX, y, curZ)) && w.isAirBlock(new BlockPos(curX, y-1, curZ)) && w.getBlockState(new BlockPos(curX, y+1, curZ)).getBlock().isNormalCube())
                    {
                        foundSpot(curX, y, curZ);
                        return;
                    }
                    
                    switch (direction)
                    {
                        case 0: // going north
                        {
                            if (stepsToDo > 0)
                            {
                                curX++;
                                stepsToDo--;
                            }
                            else // turning east, do first step right away
                            {
                                direction = 1;
                                curZ++;
                                stepsToDo = stepLength-1;
                            }
                            break;
                        }
                        case 1: // going east
                        {
                            if (stepsToDo > 0)
                            {
                                curZ++;
                                stepsToDo--;
                            }
                            else // turning south, do first step right away
                            {
                                direction = 2;
                                stepLength++;
                                curX--;
                                stepsToDo = stepLength-1;
                            }
                            break;
                        }
                        case 2: // going south
                        {
                            if (stepsToDo > 0)
                            {
                                curX--;
                                stepsToDo--;
                            }
                            else // turning west, do first step right away
                            {
                                direction = 3;
                                curZ--;
                                stepsToDo = stepLength-1;
                            }
                            break;
                        }
                        default: // going west
                        {
                            if (stepsToDo > 0)
                            {
                                curZ--;
                                stepsToDo--;
                            }
                            else // turning north, do first step right away
                            {
                                direction = 0;
                                stepLength++;
                                curX++;
                                stepsToDo = stepLength-1;
                            }
                            break;
                        }
                    }
                }
                Thread.yield();
                
                /* old artificial approach
                for (int x = minX; x <= maxX; x++)
                {
                    for (int z = minZ; z <= endZ; z++)
                    {
                        if (w.isAirBlock(x, y, z) && w.isAirBlock(x, y-1, z) && w.isBlockNormalCube(x, y+1, z))
                        {
                            foundSpot(x, y, z);
                            return;
                        }
                    }
                }
                */
            }
            
            isSearching = false;
        }
        
        private void foundSpot(int x, int y, int z)
        {
            isSearching = false;
            petBat.setHangingSpot(new BlockPos(x, y, z));
        }
    }
}
