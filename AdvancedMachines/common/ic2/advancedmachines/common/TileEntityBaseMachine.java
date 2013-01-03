package ic2.advancedmachines.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.Items;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public abstract class TileEntityBaseMachine extends TileEntityMachine implements IEnergySink
{
    public int energy;
    public int fuelslot;
    public int maxEnergy;
    public int maxInput;
    public int tier;
    public boolean addedToEnergyNet;

    public TileEntityBaseMachine(int inventorySize, int maxEnergy, int maxInput)
    {
        super(inventorySize);
        this.fuelslot = 0;
        this.maxEnergy = maxEnergy;
        this.maxInput = maxInput;
        this.tier = 1;
        
        energy = 0;
        addedToEnergyNet = false;
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
        super.updateEntity();
        if (!this.addedToEnergyNet)
        {
            //EnergyNet.getForWorld(this.worldObj).addTileEntity(this);
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            this.addedToEnergyNet = true;
        }
    }

    @Override
    public void invalidate()
    {
        if (this.addedToEnergyNet)
        {
            //EnergyNet.getForWorld(this.worldObj).removeTileEntity(this);
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            this.addedToEnergyNet = false;
        }

        super.invalidate();
    }

    @Override
    public boolean isAddedToEnergyNet()
    {
        return this.addedToEnergyNet;
    }

    @Override
    public int demandsEnergy()
    {
        return maxEnergy - energy;
    }

    @Override
    public int injectEnergy(Direction var1, int var2)
    {
        if (var2 > this.maxInput)
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
            this.energy += var2;
            int var3 = 0;
            if (this.energy > this.maxEnergy)
            {
                var3 = this.energy - this.maxEnergy;
                this.energy = this.maxEnergy;
            }

            return var3;
        }
    }
    
    @Override
    public int getMaxSafeInput()
    {
        return maxInput;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity var1, Direction var2)
    {
        return true;
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
            int fuelID = inventory[fuelslot].itemID;
            if (Item.itemsList[fuelID] instanceof IElectricItem)
            {
                if (!((IElectricItem)Item.itemsList[fuelID]).canProvideEnergy())
                {
                    return false;
                }
                else
                {
                    int charge = ElectricItem.discharge(inventory[fuelslot], maxEnergy - energy, tier, false, false);
                    energy += charge;
                    return charge > 0;
                }
            }
            else if (fuelID == Item.redstone.itemID)
            {
                energy += maxEnergy;
                --inventory[fuelslot].stackSize;
                if (inventory[fuelslot].stackSize <= 0)
                {
                    inventory[fuelslot] = null;
                }

                return true;
            }
            else if (fuelID == Items.getItem("suBattery").itemID)
            {
                energy += 1000;
                --inventory[fuelslot].stackSize;
                if (inventory[fuelslot].stackSize <= 0)
                {
                    inventory[fuelslot] = null;
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
