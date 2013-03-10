package atomicstryker.minions.common.jobmanager;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import atomicstryker.minions.common.entity.EntityMinion;

/**
 * Blocktask for destroying and stashing a single Block
 * 
 * 
 * @author AtomicStryker
 */

public class BlockTask_MineBlock extends BlockTask
{
    public Block targetBlock;
    private int blocksDropped;
    public int blockID;
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
    	blocksDropped = 0;
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
    	
    	this.blockID = worker.worldObj.getBlockId(posX, posY, posZ);
    	//if (blockID > 13) System.out.println("Reached Block["+blockID+"], name "+Block.blocksList[blockID].getBlockName());
    	
    	if (blockID == 0)
    	{
    		this.onFinishedTask();
    	}
    	else
    	{
        	this.blockmetadata = worker.worldObj.getBlockMetadata(posX, posY, posZ);
        	this.targetBlock = Block.blocksList[blockID];
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
    	
    	this.blockID = worker.worldObj.getBlockId(posX, posY, posZ); // check against interference mining
    	if (blockID != 0 && Block.blocksList[blockID].getBlockHardness(worker.worldObj, posX, posY, posZ) >= 0F)
    	{
    	    ArrayList<ItemStack> stackList = getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ);
    		if (worker.worldObj.setBlockAndMetadataWithNotify(posX, posY, posZ, 0, 0, 3))
    		{
    			putBlockHarvestInWorkerInventory(stackList);
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
    	int checkBlockID = worker.worldObj.getBlockId(x, y, z);
    	
    	if (checkBlockID > 0)
    	{
    		if (checkBlockID == Block.sand.blockID || checkBlockID == Block.gravel.blockID)
    		{
    			putBlockHarvestInWorkerInventory(getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ));
    			
            	this.worker.inventory.consumeInventoryItem(Block.dirt.blockID);
            	this.worker.worldObj.setBlockAndMetadataWithNotify(x, y, z, Block.dirt.blockID, 0, 3);
    		}
    	}
	}
    
    private void checkBlockForDanger(int x, int y, int z)
    {
    	this.checkBlockForDanger(x, y, z, false);
    }
    
    private void checkBlockForDanger(int x, int y, int z, boolean putFloor)
    {
    	int checkBlockID = worker.worldObj.getBlockId(x, y, z);
    	int meta = 0;
    	boolean replaceBlock = false;
    	
    	if (checkBlockID == 0)
    	{
    		if (putFloor)
    		{
    			replaceBlock = true;
    		}
    	}
    	else if (!Block.blocksList[checkBlockID].blockMaterial.isSolid() && checkBlockID != Block.torchWood.blockID)
    	{
    		meta = worker.worldObj.getBlockMetadata(x, y, z);
    		replaceBlock = true;
    	}
    	
    	if (replaceBlock)
    	{    		
        	if (checkBlockID != 0)
        	{
        	    ArrayList<ItemStack> stackList = getItemStacksFromWorldBlock(worker.worldObj, posX, posY, posZ);
        		if (this.worker.worldObj.setBlockAndMetadataWithNotify(x, y, z, 0, 0, 3))
        		{
        			putBlockHarvestInWorkerInventory(stackList);
        		}
        	}
        	
        	this.worker.inventory.consumeInventoryItem(Block.dirt.blockID);
        	this.worker.worldObj.setBlockAndMetadataWithNotify(x, y, z, Block.dirt.blockID, 0, 3);
    	}
    }
}
