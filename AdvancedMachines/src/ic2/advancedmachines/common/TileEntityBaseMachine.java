package ic2.advancedmachines.common;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.Items;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public abstract class TileEntityBaseMachine extends TileEntityMachine implements IEnergySink
{
    protected static final int MAX_PROGRESS = 4000;
    protected static final int MAX_ENERGY = 5000;
    protected static final int MAX_SPEED = 7500;
    protected static final int MAX_INPUT = 32;
    
    protected int energyConsume = 2;
    protected int acceleration = 1;
    protected int maxSpeed;
    protected int ejectors;
    
    public int energy;
    public int fuelslot;
    public int maxEnergy;
    public int maxInput;
    public int tier;
    public boolean addedToEnergyNet;
    
    private ItemStack suBattery;
    
    public TileEntityBaseMachine(int inventorySize, int maxEnergy, int maxInput)
    {
        super(inventorySize);
        this.fuelslot = 0;
        this.maxEnergy = maxEnergy;
        this.maxInput = maxInput;
        this.tier = 1;
        
        energy = 0;
        addedToEnergyNet = false;
        
        suBattery = Items.getItem("suBattery");
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        this.energy = var1.getInteger("energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        var1.setInteger("energy", this.energy);
    }

    @Override
    public void updateEntity()
    {
        if (IC2.platform.isSimulating() && !addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
        super.updateEntity();
    }
    
    @Override
    public void invalidate()
    {
        if (IC2.platform.isSimulating() && addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        super.invalidate();
    }
    
    @Override
    public void onChunkUnload()
    {
        if (IC2.platform.isSimulating() && addedToEnergyNet)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        super.onChunkUnload();
    }
    
    @Override
    public double demandedEnergyUnits()
    {
        return maxEnergy - energy;
    }

    @Override
    public double injectEnergyUnits(ForgeDirection dir, double amount)
    {
        setOverclockRates();
        if (amount > this.maxInput)
        {
        	if (!AdvancedMachines.explodeMachineAt(worldObj, xCoord, yCoord, zCoord))
        	{
        		worldObj.createExplosion(null, xCoord, yCoord, zCoord, 2.0F, true);
        	}
        	invalidate();
            return 0;
        }
        else
        {
            if (this.energy > this.maxEnergy)
            {
                return amount;
            }
            this.energy += amount;
        }
        return 0;
    }
    
    @Override
    public int getMaxSafeInput()
    {
        return maxInput;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity var1, ForgeDirection var2)
    {
        return demandedEnergyUnits() > 1;
    }

    public boolean isRedstonePowered()
    {
        return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
    }
    
    protected boolean getPowerFromFuelSlot()
    {
        if (inventory[fuelslot] == null)
        {
            return false;
        }
        else
        {
            if (inventory[fuelslot].getItem() instanceof IElectricItem)
            {
                if (((IElectricItem)inventory[fuelslot].getItem()).canProvideEnergy(inventory[fuelslot]))
                {
                    int charge = ElectricItem.manager.discharge(inventory[fuelslot], maxEnergy - energy, tier, false, false);
                    energy += charge;
                    return (charge > 0);
                }
            }
            else if (inventory[fuelslot].itemID == Item.redstone.itemID)
            {
                energy += maxEnergy;
                --inventory[fuelslot].stackSize;
                if (inventory[fuelslot].stackSize <= 0)
                {
                    inventory[fuelslot] = null;
                }

                return true;
            }
            else if (inventory[fuelslot].isItemEqual(suBattery))
            {
                energy += 1000;
                --inventory[fuelslot].stackSize;
                if (inventory[fuelslot].stackSize <= 0)
                {
                    inventory[fuelslot] = null;
                }

                return true;
            }
            return false;
        }
    }
    
    public abstract int getUpgradeSlotsStartSlot();
    
    protected void setOverclockRates()
    {
        int overclockerUpgradeCount = 0;
        int transformerUpgradeCount = 0;
        int energyStorageUpgradeCount = 0;
        ejectors = 0;

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
