package atomicstryker.ropesplus.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityZipLineAnchor extends TileEntity
{

    private int targetX;
    private int targetY;
    private int targetZ;
    private EntityFreeFormRope ropeEnt;
    private boolean errored;
    
    public TileEntityZipLineAnchor()
    {
        super();
        targetX = -1;
        targetY = -1;
        targetZ = -1;
        ropeEnt = null;
    }
    
    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("targetX", targetX);
        compound.setInteger("targetY", targetY);
        compound.setInteger("targetZ", targetZ);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        targetX = compound.getInteger("targetX");
        targetY = compound.getInteger("targetY");
        targetZ = compound.getInteger("targetZ");
    }
    
    @Override
    public void updateEntity()
    {
        super.updateEntity();
        checkRope();
    }
    
    @Override
    public void validate()
    {
        super.validate();
    }
    
    @Override
    public void invalidate()
    {
        if (ropeEnt != null)
        {
            ropeEnt.setDead();
            ropeEnt = null;
        }
        super.invalidate();
    }
    
    public boolean getHasZipLine()
    {
        return ropeEnt != null;
    }
    
    public EntityFreeFormRope getZipLineEntity()
    {
        return ropeEnt;
    }
    
    public void setTargetCoordinates(int x, int y, int z)
    {
        targetX = x;
        targetY = y;
        targetZ = z;
        trySpawningRope();
    }
    
    private void checkRope()
    {
        if (ropeEnt == null || (ropeEnt != null && ropeEnt.isDead))
        {
            ropeEnt = null;
            trySpawningRope();
        }
    }
    
    private void trySpawningRope()
    {
        if (targetY > 0)
        {
            if (worldObj.isBlockOpaqueCube(targetX, targetY, targetZ))
            {                
                ropeEnt = new EntityFreeFormRope(worldObj);
                ropeEnt.setStartCoordinates(xCoord+0.5D, yCoord, zCoord+0.5D);
                ropeEnt.setEndCoordinates(targetX+0.5D, targetY+1D, targetZ+0.5D);
                ropeEnt.setLoosening();
                worldObj.spawnEntityInWorld(ropeEnt);
            }
            else if (!errored)
            {
                System.out.printf("zipline target coords [%d|%d|%d] are not an opaque block!!\n", targetX, targetY, targetZ);
                errored = true;
            }
        }
    }
    
}
