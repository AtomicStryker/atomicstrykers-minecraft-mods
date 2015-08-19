package atomicstryker.minions.common.jobmanager;

import atomicstryker.minions.common.entity.EntityMinion;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;

/**
 * Blocktask for destroying and stashing a single Block
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_MineBlock extends BlockTask
{
    public Block blockID;
    public int blockmetadata;
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
    	
    	this.blockID = worker.worldObj.getBlock(posX, posY, posZ);
    	//if (blockID > 13) System.out.println("Reached Block["+blockID+"], name "+Block.blocksList[blockID].getBlockName());
    	
    	if (blockID == Blocks.air)
    	{
    		this.onFinishedTask();
    	}
    	else
    	{
        	this.blockmetadata = worker.worldObj.getBlockMetadata(posX, posY, posZ);
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
    	
    	this.blockID = worker.worldObj.getBlock(posX, posY, posZ); // check against interference mining
    	if (blockID != Blocks.air && blockID.getBlockHardness(worker.worldObj, posX, posY, posZ) >= 0F)
    	{
    	    ArrayList<ItemStack> stackList = getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ);
    	    
            BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(worker.worldObj, worker.worldObj.getWorldInfo().getGameType(), 
                    (EntityPlayerMP) worker.master, posX, posY, posZ);
            if (!event.isCanceled())
            {
                if (worker.worldObj.setBlock(posX, posY, posZ, Blocks.air, 0, 3))
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
    	Block checkBlockID = worker.worldObj.getBlock(x, y, z);
        if (checkBlockID == Blocks.sand || checkBlockID == Blocks.gravel)
        {
            BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(worker.worldObj, worker.worldObj.getWorldInfo().getGameType(), 
                    (EntityPlayerMP) worker.master, x, y, z);
            if (!event.isCanceled())
            {
                putBlockHarvestInWorkerInventory(getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ));
                
                this.worker.inventory.consumeInventoryItem(Blocks.dirt);
                this.worker.worldObj.setBlock(x, y, z, Blocks.dirt, 0, 3);
            }
        }
	}
    
    private void checkBlockForDanger(int x, int y, int z)
    {
    	this.checkBlockForDanger(x, y, z, false);
    }
    
    private void checkBlockForDanger(int x, int y, int z, boolean putFloor)
    {
    	Block checkBlockID = worker.worldObj.getBlock(x, y, z);
    	boolean replaceBlock = false;
    	
    	if (checkBlockID == Blocks.air)
    	{
    		if (putFloor)
    		{
    			replaceBlock = true;
    		}
    	}
    	else if (!checkBlockID.getMaterial().isSolid() && checkBlockID != Blocks.torch)
    	{
    		worker.worldObj.getBlockMetadata(x, y, z);
    		replaceBlock = true;
    	}
    	
    	if (replaceBlock)
    	{
    	    BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(worker.worldObj, worker.worldObj.getWorldInfo().getGameType(), 
    	            (EntityPlayerMP) worker.master, x, y, z);
            if (!event.isCanceled())
            {
                if (checkBlockID != Blocks.air)
                {
                    ArrayList<ItemStack> stackList = getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ);
                    if (this.worker.worldObj.setBlock(x, y, z, Blocks.air, 0, 3))
                    {
                        putBlockHarvestInWorkerInventory(stackList);
                    }
                }
                
                this.worker.inventory.consumeInventoryItem(Blocks.dirt);
                this.worker.worldObj.setBlock(x, y, z, Blocks.dirt, 0, 3);
            }
    	}
    }
}
