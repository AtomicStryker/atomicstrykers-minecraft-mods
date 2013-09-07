package ic2.advancedmachines.common;

import ic2.api.Direction;
import ic2.api.item.IElectricItem;
import ic2.api.network.NetworkHelper;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityAdvancedMachine extends TileEntityBaseMachine implements ISidedInventory
{
    private static final int MAX_PROGRESS = 4000;
    private static final int MAX_ENERGY = 5000;
    private static final int MAX_SPEED = 7500;
    private static final int MAX_INPUT = 32;
    private String inventoryName;
    private int[] inputs;
    private int[] outputs;
    private String dataFormat;
    private int dataScaling;

    private IC2AudioSource audioSource;
    private static final int EventStart = 0;
    private static final int EventInterrupt = 1;
    private static final int EventStop = 2;

    private int energyConsume = 2;
    private int acceleration = 1;
    private int maxSpeed;
    private int ejectors;

    public int speed;
    public short progress;

    public TileEntityAdvancedMachine(String invName, String dataForm, int dataScale, int[] inputSlots, int[] outputSlots)
    {
        super(inputSlots.length + outputSlots.length + 6, MAX_ENERGY, MAX_INPUT);
        this.inventoryName = invName;
        this.dataFormat = dataForm;
        this.dataScaling = dataScale;
        this.inputs = inputSlots;
        this.outputs = outputSlots;
        this.speed = 0;
        this.progress = 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

        try
        {
            speed = var1.getInteger("speed");
            progress = var1.getShort("progress");
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        var1.setInteger("speed", this.speed);
        var1.setShort("progress", this.progress);
    }

    @Override
    public String getInvName()
    {
        return this.inventoryName;
    }

    public int gaugeProgressScaled(int var1)
    {
        return var1 * this.progress / MAX_PROGRESS;
    }

    public int gaugeFuelScaled(int var1)
    {
        return var1 * this.energy / this.maxEnergy;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (worldObj.isRemote)
        {
            return;
        }

        boolean newItemProcessing = false;
        if (energy <= maxEnergy)
        {
            getPowerFromFuelSlot();
        }

        boolean isActive = getActive();
        if (this.progress >= MAX_PROGRESS)
        {
            this.placeResultItems();
            newItemProcessing = true;
            this.progress = 0;
            isActive = false;

            NetworkHelper.initiateTileEntityEvent(this, EventStop, true);
        }

        boolean bCanOperate = canOperate();
        if (energy > 0 && (bCanOperate || isRedstonePowered()))
        {
            setOverclockRates();

            if (speed < maxSpeed)
            {
                speed += acceleration;
                energy -= energyConsume;
            }
            else
            {
                speed = maxSpeed;
                energy -= AdvancedMachines.defaultEnergyConsume;
            }

            isActive = true;
            NetworkHelper.initiateTileEntityEvent(this, EventStart, true);
        }
        else
        {
            boolean wasWorking = speed != 0;
            speed = speed - Math.min(speed, 4);
            if (wasWorking && speed == 0)
            {
                NetworkHelper.initiateTileEntityEvent(this, EventInterrupt, true);
            }
        }

        if (isActive && progress != 0)
        {
            if (!bCanOperate || speed == 0)
            {
                if (!bCanOperate)
                {
                    progress = 0;
                }

                isActive = false;
            }
        }
        else if (bCanOperate)
        {
            if (speed != 0)
            {
                isActive = true;
            }
        }
        else
        {
            progress = 0;
        }

        if (isActive && bCanOperate)
        {
            progress = (short) (progress + speed / 30);
        }
        
        runEjectorLogic();

        if (newItemProcessing)
        {
            onInventoryChanged();
        }
        if (isActive != getActive())
        {
            worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
            setActive(isActive);
        }
    }

    private void runEjectorLogic()
    {
        int toEject = ejectors;
        TileEntity te;
        boolean done = false;
        while (toEject > 0 && energy > 20)
        {
            for (int index = 0; index < outputs.length; ++index)
            {                
                if (inventory[outputs[index]] != null)
                {
                    int amount = Math.min(inventory[outputs[index]].stackSize, energy / 20);
                    for (Direction dir : Direction.values())
                    {
                        te = dir.applyToTileEntity(this);
                        if (te != null && te instanceof IInventory)
                        {
                            amount = StackUtil.putInInventory((IInventory)te, StackUtil.copyWithSize(inventory[outputs[index]], amount), false);
                            inventory[outputs[index]].stackSize -= amount;
                            if (inventory[outputs[index]].stackSize < 1)
                            {
                                inventory[outputs[index]] = null;
                            }
                            energy -= 20 * amount;
                            done = true;
                            break;
                        }
                    }
                }
                
                if (done)
                {
                    break;
                }
            }
            
            toEject--;
        }
    }

    private void placeResultItems()
    {
        if (canOperate())
        {
            List<ItemStack> resultStacks = getResultFor(inventory[inputs[0]], true);
            if (resultStacks != null)
            {
                ItemStack itemstack;
                for (ItemStack is : resultStacks)
                {
                    itemstack = is.copy();                    
                    for (int index = 0; index < outputs.length; ++index)
                    {
                        if (inventory[outputs[index]] == null)
                        {
                            inventory[outputs[index]] = itemstack.copy();
                            break;
                        }
                        else if (inventory[outputs[index]].isItemEqual(itemstack))
                        {
                            int transfer = Math.min(itemstack.stackSize, inventory[outputs[index]].getMaxStackSize()-inventory[outputs[index]].stackSize);
                            
                            inventory[outputs[index]].stackSize += transfer;
                            itemstack.stackSize -= transfer;
                            
                            if (itemstack.stackSize < 1)
                            {
                                break;
                            }
                        }
                    }
                }
                onFinishedProcessingItem();
            }
            if (inventory[inputs[0]].stackSize <= 0)
            {
                inventory[inputs[0]] = null;
            }
        }
    }

    public void onFinishedProcessingItem()
    {
        
    }

    private boolean canOperate()
    {
        if (inventory[inputs[0]] == null)
        {
            return false;
        }
        else
        {
            List <ItemStack> resultStacks = getResultFor(inventory[inputs[0]], false);
            if (resultStacks != null)
            {
                for (ItemStack resultStack : resultStacks)
                {
                    int resultMaxStackSize = resultStack.getMaxStackSize();
                    int freeSpaceOutputSlots = 0;
                    for (int index = 0; index < outputs.length; ++index)
                    {
                        int curOutputSlot = outputs[index];
                        if (inventory[curOutputSlot] == null)
                        {
                            freeSpaceOutputSlots += resultMaxStackSize;
                        }
                        else if (inventory[curOutputSlot].isItemEqual(resultStack))
                        {
                            freeSpaceOutputSlots += (resultMaxStackSize - inventory[curOutputSlot].stackSize);
                        }
                    }

                    return freeSpaceOutputSlots >= resultStack.stackSize;
                }
            }
            return false;
        }
    }

    /**
     * Returns the ItemStack that results from processing whatever is in the
     * Input, or null
     * 
     * @param input
     *            ItemStack to be processed
     * @param adjustOutput
     *            if true, whatever was used as input will be taken from the
     *            input slot and destroyed, if false, the input Slots remain as
     *            they are
     * 
     * @return ItemStack List that results from processing the Input, or null if no
     *         processing is possible
     */
    public abstract List<ItemStack> getResultFor(ItemStack input, boolean adjustOutput);

    public abstract Container getGuiContainer(InventoryPlayer var1);

    // ISidedInventory Overrides

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        switch (side)
        {
        case 1: // UP
            return inputs;
        default:
            return outputs;
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        for (int ins : inputs)
        {
            if (i == ins)
            {
                return getResultFor(itemstack, false) != null;
            }
        }

        return false;
    }

    @Override
    public boolean canInsertItem(int slotSize, ItemStack itemstack, int blockSide)
    {
        if (blockSide == 0)
        {
            return itemstack.getItem() instanceof IElectricItem;
        }
        if (blockSide == 1)
        {
            return getResultFor(itemstack, false) != null;
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int blockSide)
    {
        for (int outputSlot : outputs)
        {
            if (slot == outputSlot)
            {
                return true;
            }
        }
        return false;
    }

    public String printFormattedData()
    {
        return String.format(this.dataFormat, new Object[] { Integer.valueOf(speed * this.dataScaling) });
    }

    @Override
    public void invalidate()
    {
        if (this.audioSource != null)
        {
            IC2AudioSource.removeSource(audioSource);
            this.audioSource = null;
        }
        super.invalidate();
    }

    protected String getStartSoundFile()
    {
        return null;
    }

    protected String getInterruptSoundFile()
    {
        return null;
    }

    @Override
    public void onNetworkEvent(int event)
    {
        super.onNetworkEvent(event);

        if (worldObj.isRemote)
        {
            if ((this.audioSource == null) && (getStartSoundFile() != null))
            {
                this.audioSource = new IC2AudioSource(this, getStartSoundFile());
            }

            switch (event)
            {
            case EventStart:
                this.setActiveWithoutNotify(true);
                if (this.audioSource == null)
                    break;
                this.audioSource.play();
                break;
            case EventInterrupt:
                this.setActiveWithoutNotify(false);
                if (this.audioSource == null)
                    break;
                this.audioSource.stop();
                if (getInterruptSoundFile() == null)
                    break;
                IC2AudioSource.playOnce(this, getInterruptSoundFile());
                break;
            case EventStop:
                this.setActiveWithoutNotify(false);
                if (this.audioSource == null)
                    break;
                this.audioSource.stop();
            }
        }

        NetworkHelper.announceBlockUpdate(worldObj, xCoord, yCoord, zCoord);
    }

    public abstract int getUpgradeSlotsStartSlot();

    private void setOverclockRates()
    {
    	int overclockerUpgradeCount = 0;
    	int transformerUpgradeCount = 0;
    	int energyStorageUpgradeCount = 0;

    	for (int i = 0; i < 4; i++) {
    		ItemStack itemStack = this.inventory[getUpgradeSlotsStartSlot() + i];

    		if (itemStack != null)
    		{
    			if (itemStack.isItemEqual(AdvancedMachines.overClockerStack))
    				overclockerUpgradeCount += itemStack.stackSize;
    			else if (itemStack.isItemEqual(AdvancedMachines.transformerStack))
    				transformerUpgradeCount += itemStack.stackSize;
    			else if (itemStack.isItemEqual(AdvancedMachines.energyStorageUpgradeStack))
    				energyStorageUpgradeCount += itemStack.stackSize;
    			else if (itemStack.isItemEqual(AdvancedMachines.ejectorUpgradeStack))
    			    ejectors += itemStack.stackSize;
    		}
    	}

    	if (overclockerUpgradeCount > 16) overclockerUpgradeCount = 16;
    	if (transformerUpgradeCount > 10) transformerUpgradeCount = 10;

    	this.energyConsume = (int)(AdvancedMachines.defaultEnergyConsume * Math.pow(AdvancedMachines.overClockEnergyRatio, overclockerUpgradeCount));
    	this.acceleration = (int)(((AdvancedMachines.defaultAcceleration) * Math.pow(AdvancedMachines.overClockAccelRatio, overclockerUpgradeCount)) /2);
    	this.maxSpeed = (MAX_SPEED + overclockerUpgradeCount * AdvancedMachines.overClockSpeedBonus);
    	this.maxInput = (MAX_INPUT * (int)Math.pow(AdvancedMachines.overLoadInputRatio, transformerUpgradeCount));
    	this.maxEnergy = (MAX_ENERGY + energyStorageUpgradeCount * MAX_ENERGY + this.maxInput - 1);
    	this.tier = 1 + transformerUpgradeCount;
    }
}
