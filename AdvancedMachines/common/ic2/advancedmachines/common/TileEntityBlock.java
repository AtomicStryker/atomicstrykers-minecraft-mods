package ic2.advancedmachines.common;

import java.util.*;

import net.minecraft.src.*;
import ic2.api.*;

public class TileEntityBlock extends TileEntity implements IWrenchable, INetworkDataProvider, INetworkTileEntityEventListener
{
    protected boolean created = false;
    public boolean active = false;
    public short facing = 5;
    public boolean prevActive = false;
    public short prevFacing = 0;
    public static List networkedFields;
    
    public TileEntityBlock()
    {
    	if (networkedFields == null)
    	{
            networkedFields = new ArrayList();
            networkedFields.add("active");
            networkedFields.add("facing");
    	}
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        this.prevFacing = this.facing = var1.getShort("facing");
    }

    @Override
    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        var1.setShort("facing", this.facing);
    }

    @Override
    public void updateEntity()
    {
        if (!this.created)
        {
            this.created = true;
            NetworkHelper.requestInitialData(this);
            NetworkHelper.announceBlockUpdate(worldObj, xCoord, yCoord, zCoord);
        }
    }

    public boolean getActive()
    {
        return this.active;
    }

    public void setActive(boolean flag)
    {
        active = flag;
        if(prevActive != active)
        	NetworkHelper.updateTileEntityField(this, "active");
        prevActive = flag;
    }
    
    public void setActiveWithoutNotify(boolean var1)
    {
        this.active = var1;
        this.prevActive = var1;
    }

    @Override
    public short getFacing()
    {
        return this.facing;
    }
    
    @Override
    public boolean wrenchCanSetFacing(EntityPlayer var1, int facingToSet)
    {
    	if (facingToSet < 2 // Top or Bottom
    	|| facingToSet == facing) // dismantle upon clicking the face
    	{
    		return false;
    	}
        return true;
    }

    @Override
    public void setFacing(short var1)
    {
        this.facing = var1;
        NetworkHelper.updateTileEntityField(this, "facing");
        this.prevFacing = var1;
        NetworkHelper.announceBlockUpdate(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer var1)
    {
        return true;
    }

    @Override
    public float getWrenchDropRate()
    {
        return 1.0F;
    }

	@Override
	public List<String> getNetworkedFields()
	{
		return networkedFields;
	}

	@Override
	public void onNetworkEvent(int event)
	{
	}
}
