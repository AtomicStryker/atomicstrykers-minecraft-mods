package atomicstryker.minions.common.jobmanager;

import java.util.List;

import atomicstryker.minions.common.entity.EntityMinion;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeHooks;

/**
 * Blocktask for destroying and stashing a single Block
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_MineBlock extends BlockTask
{
    public IBlockState blockState;
    public boolean disableDangerCheck;
	
    /**
     * @param boss Job Manager calling
     * @param input Minion to do it, can remain null while the Job is on wait
     * @param ix coordinate
     * @param iy coordinate
     * @param iz coordinate
     */
    public BlockTask_MineBlock(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz)
    {
    	super(boss, input, ix, iy, iz);
    	disableDangerCheck = false;
    }
    
    /**
     * @param boss Job Manager calling
     * @param input Minion to do it, can remain null while the Job is on wait
     * @param ix coordinate
     * @param iy coordinate
     * @param iz coordinate
     * @param noDangerCheck can disable putting Dirt into air holes and over liquids
     */
    public BlockTask_MineBlock(Minion_Job_Manager boss, EntityMinion input, int ix, int iy, int iz, boolean noDangerCheck)
    {
        this(boss, input, ix, iy, iz);
    	disableDangerCheck = noDangerCheck;
    }

    @Override
    public void onStartedTask()
    {
    	super.onStartedTask();
    }
    
    @Override
    public void onReachedTaskBlock()
    {
    	super.onReachedTaskBlock();
    	
    	blockState = worker.world.getBlockState(pos);
    	//if (blockID > 13) System.out.println("Reached Block["+blockID+"], name "+Block.blocksList[blockID].getBlockName());
    	
    	if (blockState.getBlock() == Blocks.AIR)
    	{
    		this.onFinishedTask();
    	}
    }
    
    @Override
    public void onUpdate()
    {
    	super.onUpdate();
    }
    
    @Override
    public void onFinishedTask()
    {
    	super.onFinishedTask();
    	
    	checkDangers();
    	
    	blockState = worker.world.getBlockState(pos); // check against interference mining
    	if (blockState.getBlock() != Blocks.AIR && blockState.getBlockHardness(worker.world, pos) >= 0F)
    	{
    	    List<ItemStack> stackList = getItemStacksFromWorldBlock(worker.world, posX, posY, posZ);
    	    
            int event = ForgeHooks.onBlockBreakEvent(worker.world, worker.world.getWorldInfo().getGameType(), 
                    (EntityPlayerMP) worker.master, pos);
            if (event != -1)
            {
                if (worker.world.setBlockToAir(pos))
                {
                    putBlockHarvestInWorkerInventory(stackList);
                }
            }
    	}
    }
    
    protected void checkDangers()
    {
    	if (!disableDangerCheck)
    	{
	    	// check adjacent blocks for fluids or holes, put safe blocks down
	    	checkBlockForDanger(posX, posY-1, posZ, true);
	    	//checkBlockForDanger(posX, posY+1, posZ);
	    	checkBlockForDanger(posX+1, posY, posZ);
	    	checkBlockForDanger(posX-1, posY, posZ);
	    	checkBlockForDanger(posX, posY, posZ+1);
	    	checkBlockForDanger(posX, posY, posZ-1);
    	}
    	
    	checkBlockForCaveIn(posX, posY+1, posZ);
    }
    
    private void checkBlockForCaveIn(int x, int y, int z)
    {
    	Block checkBlockID = worker.world.getBlockState(new BlockPos(x, y, z)).getBlock();
        if (checkBlockID == Blocks.SAND || checkBlockID == Blocks.GRAVEL)
        {
            int event = ForgeHooks.onBlockBreakEvent(worker.world, worker.world.getWorldInfo().getGameType(), 
                    (EntityPlayerMP) worker.master, pos);
            if (event != -1)
            {
                putBlockHarvestInWorkerInventory(getItemStacksFromWorldBlock(worker.world, posX, posY, posZ));
                
                this.worker.inventory.consumeInventoryItem(Blocks.DIRT);
                this.worker.world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
            }
        }
	}
    
    private void checkBlockForDanger(int x, int y, int z)
    {
    	this.checkBlockForDanger(x, y, z, false);
    }
    
    private void checkBlockForDanger(int x, int y, int z, boolean putFloor)
    {
        IBlockState is = worker.world.getBlockState(new BlockPos(x, y, z));
    	Block checkBlockID = is.getBlock();
    	boolean replaceBlock = false;
    	
    	if (checkBlockID == Blocks.AIR)
    	{
    		if (putFloor)
    		{
    			replaceBlock = true;
    		}
    	}
    	else if (!is.getMaterial().isSolid() && checkBlockID != Blocks.TORCH)
    	{
    		replaceBlock = true;
    	}
    	
    	if (replaceBlock)
    	{
    	    int event = ForgeHooks.onBlockBreakEvent(worker.world, worker.world.getWorldInfo().getGameType(), 
    	            (EntityPlayerMP) worker.master, pos);
            if (event != -1)
            {
                if (checkBlockID != Blocks.AIR)
                {
                    List<ItemStack> stackList = getItemStacksFromWorldBlock(worker.world, posX, posY, posZ);
                    if (this.worker.world.setBlockToAir(pos))
                    {
                        putBlockHarvestInWorkerInventory(stackList);
                    }
                }
                
                this.worker.inventory.consumeInventoryItem(Blocks.DIRT);
                this.worker.world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
            }
    	}
    }
}
