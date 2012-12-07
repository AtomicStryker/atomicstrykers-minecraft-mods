package atomicstryker.minions.common.entity;

import java.util.*;

import atomicstryker.minions.common.MinionsChunkManager;
import atomicstryker.minions.common.MinionsCore;
import atomicstryker.minions.common.jobmanager.BlockTask;
import atomicstryker.minions.common.pathfinding.AS_PathEntity;
import atomicstryker.minions.common.pathfinding.AStarNode;
import atomicstryker.minions.common.pathfinding.AStarPath;
import atomicstryker.minions.common.pathfinding.AStarStatic;
import atomicstryker.minions.common.pathfinding.IAStarPathedEntity;

import net.minecraft.src.Block;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityAISwimming;
import net.minecraft.src.EntityCreature;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.PathPoint;
import net.minecraft.src.Profiler;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraft.src.World;

/**
 * Minion Entity class, this is where the evil magic happens
 * 
 * 
 * @author AtomicStryker
 */

public class EntityMinion extends EntityCreature implements IAStarPathedEntity
{
	public EntityPlayer master;
	public String masterUsername;
	private ItemStack heldItem = new ItemStack(Item.pickaxeSteel, 1);
	public InventoryMinion inventory = new InventoryMinion(this);
	public boolean inventoryFull = false;
	public TileEntity returnChestOrInventory;
	public AStarPath pathPlanner;
	public EnumMinionState currentState = EnumMinionState.IDLE;
	public EnumMinionState lastOrderedState = EnumMinionState.IDLE;
	public EnumMinionState nextState = null;
	private AS_PathEntity pathToWalkInputCache;
	public ChunkCoordinates currentTarget;
	private final int pathingCooldownTicks = 30;
	private int currentPathNotFoundCooldownTick = 0;
	private int pathFindingFails = 0;
	private int currentPathingStopCooldownTick = 0;
	private BlockTask currentTask;
	public EntityLiving targetEntityToGrab;
	public float workSpeed = 1.0F;
	private long workBoostTime = 0L;
	public boolean isStripMining = false;
	private long timeLastSound;
	public boolean canPickUpItems = true;
	private long canPickUpItemsAgainAt = 0L;

	public EntityMinion(World var1)
	{
		super(var1);
		this.isImmuneToFire = true;
		
        this.moveSpeed = 0.35F;
        
        this.texture = "/atomicstryker/minions/client/textures/AS_EntityMinion.png";
        this.pathPlanner = new AStarPath(worldObj, this);
        
        this.getNavigator().setAvoidsWater(false);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new MinionAIStalkAndGrab(this, this.moveSpeed));
        this.tasks.addTask(2, new MinionAIFollowMaster(this, this.moveSpeed, 10.0F, 2.0F));
        this.tasks.addTask(3, new MinionAIWander(this, this.moveSpeed));
	}
	
	public void setMaster(EntityPlayer creator)
	{
        master = creator;
        masterUsername = master.username;
	}
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(12, new Integer(0)); // boolean isWorking for SwingProgress and Sounds, set by AS_BlockTask
        this.dataWatcher.addObject(13, new Integer(0)); // x blocktask
        this.dataWatcher.addObject(14, new Integer(0));	// y blocktask
        this.dataWatcher.addObject(15, new Integer(0)); // z blocktask
        
        MinionsChunkManager.registerChunkLoaderEntity(this);
    }
	
    /**
     * Returns true if the newer Entity AI code should be run
     */
    public boolean isAIEnabled()
    {
        return true;
    }
	
	public void giveTask(BlockTask input, boolean dontReturn)
	{
		if (dontReturn)
		{
			currentTask = input;
		}
		else
		{
			giveTask(input);
		}
	}
	
	public void giveTask(BlockTask input)
	{
		currentTask = input;
		
		if (currentTask == null)
		{
			//System.out.println("giveTask(null), minion returning goods!");
	    	swingProgress = 0F;
			currentState = EnumMinionState.RETURNING_GOODS;
			lastOrderedState = EnumMinionState.RETURNING_GOODS;
		}
	}
	
	public BlockTask getCurrentTask()
	{
		return currentTask;
	}
	
	public boolean hasTask()
	{
		return currentTask != null;
	}

	@Override
	public int getMaxHealth()
	{
		return 20;
	}
	
	@Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

	@Override
    public boolean canBePushed()
    {
        return true;
    }
    
	@Override
    protected boolean canDespawn()
    {		
        return false;
    }
	
	@Override
	public void setDead()
	{
		MinionsChunkManager.unRegisterChunkLoaderEntity(this);
		inventory.dropAllItems();
		super.setDead();
	}
	
	@Override
    public void writeEntityToNBT(NBTTagCompound var1)
    {
        super.writeEntityToNBT(var1);
        var1.setTag("MinionInventory", this.inventory.writeToNBT(new NBTTagList()));
        var1.setString("masterUsername", masterUsername);
    }
    
	@Override
    public void readEntityFromNBT(NBTTagCompound var1)
    {
        super.readEntityFromNBT(var1);
        NBTTagList var2 = var1.getTagList("MinionInventory");
        this.inventory.readFromNBT(var2);
        masterUsername = var1.getString("masterUsername");
        master = worldObj.getPlayerEntityByName(masterUsername);
        
        MinionsCore.minionLoadRegister(this);
    }
    
    private void performTeleportToTarget()
    {
    	this.setPositionAndUpdate(currentTarget.posX, currentTarget.posY, currentTarget.posZ);
    	MinionsCore.proxy.playSoundAtEntity(this, "random.pop", 0.5F, 1.0F);
    }
    
    public void performRecallTeleportToMaster()
    {
    	if (master != null)
    	{
    		this.setPositionAndUpdate(master.posX+1, master.posY, master.posZ+1);
    		MinionsCore.proxy.playSoundAtEntity(this, "random.pop", 0.5F, 1.0F);
    	}
    }
    
    public void orderMinionToMoveTo(int targetX, int targetY, int targetZ, boolean allowDropping)
    {
    	if (pathPlanner.isBusy())
    	{
    		pathPlanner.stopPathSearch();
    	}
    	
    	dataWatcher.updateObject(12, Integer.valueOf(0));
		currentTarget = new ChunkCoordinates(targetX, targetY, targetZ);
		pathPlanner.getPath(doubleToInt(this.posX), doubleToInt(this.posY)-1, doubleToInt(this.posZ), targetX, targetY, targetZ, allowDropping);
		//System.out.println("Minion ordered to move to ["+targetX+"|"+targetY+"|"+targetZ+"]");
    }
    
    @Override
    public void onUpdate()
    {
    	super.onUpdate();
    	
    	if (this.riddenByEntity != null && this.riddenByEntity.equals(master) && this.getNavigator().noPath())
    	{
    		this.rotationYaw = this.rotationPitch = 0;
    	}
    	
    	if (this.dataWatcher.getWatchableObjectInt(12) != 0)
    	{
    		swingProgress += (0.17F * 0.5 * workSpeed);
    		if (swingProgress > 1.0F)
    		{
    			swingProgress = 0;
    		}
    		
    		int x = this.dataWatcher.getWatchableObjectInt(13);
    		int y = this.dataWatcher.getWatchableObjectInt(14);
    		int z = this.dataWatcher.getWatchableObjectInt(15);
    		int blockID = worldObj.getBlockId(x, y, z);
    		
    		if (blockID > 0)
    		{
	    		long curTime = System.currentTimeMillis();
	    		if (curTime - timeLastSound > (500L/workSpeed))
	    		{
	    			Block soundBlock = Block.blocksList[blockID];
	    			worldObj.playSoundAtEntity(this, soundBlock.stepSound.getStepSound(), (soundBlock.stepSound.getVolume() + 1.0F) / 2.0F, soundBlock.stepSound.getPitch() * 0.8F);
	    			timeLastSound = curTime;
	    		}
	    		
	    		this.worldObj.spawnParticle(("tilecrack_"+blockID+"_"+worldObj.getBlockMetadata(x, y, z)), posX+((double)rand.nextFloat() - 0.5D), posY+1.5D, posZ+((double)rand.nextFloat() - 0.5D), 1, 1, 1);
    		}
    	}
    	else
    	{
    		swingProgress = 0;
    	}
    }
    
    @Override
    public void onEntityUpdate()
    {
    	super.onEntityUpdate();
    	
    	if (workBoostTime != 0L && System.currentTimeMillis() - workBoostTime > 30000L)
    	{
    		workBoostTime = 0L;
    		this.workSpeed = 1.0F;
    	}
    	
    	if (currentState == EnumMinionState.WALKING_TO_COORDS || currentState == EnumMinionState.THINKING)
    	{
    		if (hasReachedTarget())
    		{
    			getNavigator().setPath(null, this.moveSpeed);
    			if (nextState != null)
    			{
	    			//System.out.println("Path reached NULL state");
	    			if (nextState == EnumMinionState.WALKING_TO_COORDS)
	    			{
	    				nextState = lastOrderedState;
	    			}
	    			
	    			currentState = nextState;
    			}
    			else
    			{
        			//System.out.println("Path reached NULL state");
        			currentState = lastOrderedState;
    			}
    		}
    		else if(getNavigator().getPath() != null
    		     && getNavigator().getPath() instanceof AS_PathEntity
    		     && ((AS_PathEntity)getNavigator().getPath()).getTimeSinceLastPathIncrement() > 750L
    		     && !worldObj.isRemote)
    		{
    			currentPathingStopCooldownTick++;
    			if (currentPathingStopCooldownTick > pathingCooldownTicks)
    			{
    			    //System.out.println("server path follow failed trigger!");
    				currentPathingStopCooldownTick = 0;
    				
    				PathPoint nextUp = ((AS_PathEntity)getNavigator().getPath()).getCurrentTargetPathPoint();
    				if (nextUp != null)
    				{
    				    ((AS_PathEntity)getNavigator().getPath()).advancePathIndex();
    					this.setPositionAndUpdate(nextUp.xCoord+0.5, nextUp.yCoord+1.5, nextUp.zCoord+0.5);
    					this.motionX = 0;
    					this.motionZ = 0;
    					pathPlanner.getPath(doubleToInt(this.posX), doubleToInt(this.posY)-1, doubleToInt(this.posZ), currentTarget.posX, currentTarget.posY, currentTarget.posZ, false);
    				}
    				else
    				{
    					performTeleportToTarget();
    				}
    			}
    		}
    	}
    	else if ((currentState == EnumMinionState.RETURNING_GOODS && returnChestOrInventory == null)
    	&& master != null
    	&& !hasPath())
    	{
    		if (this.getDistanceToEntity(master) < 3F && currentState == EnumMinionState.RETURNING_GOODS && returnChestOrInventory == null && this.inventory.containsItems())
    		{
    			dropAllItemsToWorld();
    		}
    	}
    	else if ((currentState == EnumMinionState.RETURNING_GOODS)
    	&& returnChestOrInventory != null)
    	{
    		if (this.getDistanceToEntity(returnChestOrInventory) > 4D)
    		{
    			if (!hasPath() || pathPlanner.isBusy())
    			{
	    			if (currentPathNotFoundCooldownTick > 0)
	    			{
	    				currentPathNotFoundCooldownTick--;
	    			}
	    			else
	    			{
	    				AStarNode[] possibles = AStarStatic.getAccessNodesSorted(worldObj, doubleToInt(posX), doubleToInt(posY), doubleToInt(posZ), returnChestOrInventory.xCoord, returnChestOrInventory.yCoord, returnChestOrInventory.zCoord);
	    				if (possibles.length != 0)
	    				{
	    					this.orderMinionToMoveTo(possibles[0].x, possibles[0].y, possibles[0].z, false);
	    					currentTarget = new ChunkCoordinates(possibles[0].x, possibles[0].y, possibles[0].z);
	    				}
	    			}
    			}
    		}
    		else
    		{
    			if (this.inventory.containsItems() && checkReturnChestValidity())
    			{
	    			// System.out.println("Dropping Items into chest!");
    			    if (returnChestOrInventory instanceof TileEntityChest)
    			    {
    			        openChest((TileEntityChest) returnChestOrInventory);
    			    }
    				
	    			this.inventory.putAllItemsToInventory((IInventory)returnChestOrInventory);
    			}
    			this.currentState = EnumMinionState.IDLE;
    			this.orderMinionToMoveTo(doubleToInt(posX), doubleToInt(posY)-1, doubleToInt(posZ), false);
    		}
    	}
    	else if (currentState == EnumMinionState.MINING)
    	{
    		if (!hasTask())
    		{
    			currentState = EnumMinionState.AWAITING_JOB;
    		}
    	}
    }
    
    private boolean checkReturnChestValidity()
    {
    	TileEntity test = worldObj.getBlockTileEntity(returnChestOrInventory.xCoord, returnChestOrInventory.yCoord, returnChestOrInventory.zCoord);
    	if (test != null && test == returnChestOrInventory)
    	{
    		return true;
    	}
    	
    	returnChestOrInventory = null;
    	return false;
    }
    
    @Override
    public void onLivingUpdate()
    {
    	super.onLivingUpdate();
    	
    	if (canPickUpItems)
    	{
            List collidingEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(1.0D, 0.0D, 1.0D));

            if (collidingEntities != null && collidingEntities.size() > 0)
            {
                for (int i = collidingEntities.size()-1; i >= 0; i--)
                {
                    Entity ent = (Entity)collidingEntities.get(i);
                    if (!ent.isDead)
                    {
                    	onCollisionWithEntity(ent);
                    }
                }
            }
    	}
    	else if (System.currentTimeMillis() > canPickUpItemsAgainAt)
    	{
    		canPickUpItems = true;
    	}
    }
    
    private void onCollisionWithEntity(Entity collider)
    {
    	if (collider instanceof EntityItem && !worldObj.isRemote)
    	{
    		EntityItem itemEnt = (EntityItem)collider;
    		
    		if (itemEnt.item != null)
    		{
    			if (itemEnt.ticksExisted < 200)
    			{
    				return;
    			}
    			if (this.inventory.addItemStackToInventory(itemEnt.item))
    			{
    				collider.setDead();
    			}
    			else
    			{
        			this.inventoryFull = true;
        			this.worldObj.spawnEntityInWorld(new EntityItem(worldObj, this.posX, this.posY, this.posZ, itemEnt.item));
    			}
    		}
    	}
    }
    
    public void dropAllItemsToWorld()
    {
    	blockItemPickUp();
    	MinionsCore.proxy.sendSoundToClients(this, "mod_minions.foryou");
		if (master != null)
		{
			this.faceEntity(master, 180F, 180F);
		}
		blockItemPickUp();
		this.inventory.dropAllItems();
    }
    
    private void blockItemPickUp()
    {
    	canPickUpItems = false;
    	canPickUpItemsAgainAt = System.currentTimeMillis() + 3000L;
    }
    
    private void openChest(TileEntityChest chest)
    {
    	if (chest.adjacentChestXPos != null) chest.adjacentChestXPos.lidAngle = 1.0F;
    	else if (chest.adjacentChestXNeg != null) chest.adjacentChestXNeg.lidAngle = 1.0F;
    	else if (chest.adjacentChestZPosition != null) chest.adjacentChestZPosition.lidAngle = 1.0F;
    	else if (chest.adjacentChestZNeg != null) chest.adjacentChestZNeg.lidAngle = 1.0F;
    	chest.lidAngle = 1.0F;
    }
    
    private double getDistanceToEntity(TileEntity tileent)
    {
		return AStarStatic.getDistanceBetweenCoords(doubleToInt(this.posX), doubleToInt(this.posY), doubleToInt(this.posZ), tileent.xCoord, tileent.yCoord, tileent.zCoord);
	}

	public boolean hasReachedTarget()
    {
    	return (!hasPath()
    	        && currentTarget != null
    	        &&  AStarStatic.getDistanceBetweenCoords(doubleToInt(this.posX), doubleToInt(this.posY), doubleToInt(this.posZ), currentTarget.posX, currentTarget.posY, currentTarget.posZ) < 1.5D);
    }
    
	@Override
    public void updateAITasks()
    {
    	if (pathToWalkInputCache != null)
    	{
    		//System.out.println("server updateEntActionState: Path being input, state set to walking!");
    	    this.getNavigator().setPath(pathToWalkInputCache, this.moveSpeed);
    		//setPathToEntity(pathToWalkInputCache);
    		currentState = EnumMinionState.WALKING_TO_COORDS;
    		this.getNavigator().setPath(pathToWalkInputCache, this.moveSpeed);
    		pathToWalkInputCache = null;
    	}
    	
    	if (this.hasTask())
    	{
    		currentTask.onUpdate();
    	}
    	
    	super.updateAITasks();
    }
    
	private long timelastSqueak = 0L;
	private long timeSqueakIntervals = 1000L;
	
	@Override
    public boolean attackEntityFrom(DamageSource var1, int var2)
    {
    	if (var1.getEntity() != null
    	&& timelastSqueak+timeSqueakIntervals < System.currentTimeMillis())
    	{
    	    timelastSqueak = System.currentTimeMillis();
    		if (master != null && var1.getEntity().entityId == master.entityId)
    		{
        		workBoostTime = System.currentTimeMillis();
        		workSpeed = 2.0F;
        		
        		master.onCriticalHit(this);
        		MinionsCore.proxy.sendSoundToClients(this, "mod_minions.minionsqueak");
        		//worldObj.playSoundAtEntity(this, "mod_minions.minionsqueak", 1.0F, 1.0F);
        		
    			if (this.riddenByEntity != null)
    			{
    				this.riddenByEntity.mountEntity(null);
    				return true;
    			}
        		
        		return true;
    		}
    		else if (var1.getEntity() instanceof EntityPlayer)
    		{
    			if (this.riddenByEntity != null)
    			{
    				this.riddenByEntity.mountEntity(null);
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    public void faceBlock(int ix, int iy, int iz)
    {
        double diffX = ix - this.posX;
        double diffZ = iz - this.posZ;
        double diffY = iy - this.posY;

        double var14 = (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float var12 = (float)(Math.atan2(diffZ, diffX) * 180.0D / 3.1415927410125732D) - 90.0F;
        float var13 = (float)(-(Math.atan2(diffY, var14) * 180.0D / 3.1415927410125732D));
        this.rotationPitch = -var13;
        this.rotationYaw = var12;
    }

	@Override
	public void onFoundPath(ArrayList result)
	{
		currentPathNotFoundCooldownTick = pathingCooldownTicks;
		pathFindingFails = 0;
		
		pathToWalkInputCache = AStarStatic.translateAStarPathtoPathEntity(result);
		//System.out.println("Path found and translated!");
		
		nextState = currentState;
	}

	@Override
	public void onNoPathAvailable()
	{
		if (hasTask())
		{
			currentTask.onWorkerPathFailed();
		}
		
		currentPathNotFoundCooldownTick = pathingCooldownTicks;
		pathFindingFails++;
		
		if (pathFindingFails == 3)
		{
			performTeleportToTarget();
			pathFindingFails = 0;
		}
	}
	
	public String getDisplayName()
	{
		// return ""+(Math.sqrt((this.motionX * this.motionX) + (this.motionZ * this.motionZ)));
		//return ""+currentState+"/"+nextState;
		return null;
	}
	
	public ItemStack getHeldItem()
	{
		return this.heldItem;
	}
	
    public void dropMinionItemWithRandomChoice(ItemStack stack)
    {
        if (stack != null)
        {
            EntityItem itemEnt = new EntityItem(this.worldObj, this.posX, this.posY - 0.3D + (double)this.getEyeHeight(), this.posZ, stack);
            itemEnt.delayBeforeCanPickup = 40;
            float varFloatA = 0.1F;
            itemEnt.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * varFloatA);
            itemEnt.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * varFloatA);
            itemEnt.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * 3.1415927F) * varFloatA + 0.1F);
            float randomAngle = this.rand.nextFloat() * 3.1415927F * 2.0F;
            varFloatA = this.rand.nextFloat() * 0.02F;
            itemEnt.motionX += Math.cos((double)randomAngle) * (double)varFloatA;
            itemEnt.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            itemEnt.motionZ += Math.sin((double)randomAngle) * (double)varFloatA;
            this.worldObj.spawnEntityInWorld(itemEnt);
        }
    }
    
    public int doubleToInt(double input)
    {
    	return AStarStatic.getIntCoordFromDoubleCoord(input);
    }
}
