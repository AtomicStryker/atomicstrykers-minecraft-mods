package atomicstryker.ropesplus.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

public class TileEntityZipLineAnchor extends TileEntity implements IUpdatePlayerListBox
{

    private int targetX;
    private int targetY;
    private int targetZ;
    private EntityFreeFormRope ropeEnt;
    
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
    public void update()
    {
        checkRope();
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
        if (!isInvalid())
        {
            if (ropeEnt == null || (ropeEnt != null && ropeEnt.isDead))
            {
                ropeEnt = null;
                trySpawningRope();
            }
        }
        else if (ropeEnt != null)
        {
            ropeEnt.setDead();
            ropeEnt = null;
        }
    }
    
    private void trySpawningRope()
    {
        if (targetY > 0)
        {
            if (worldObj.getBlockState(new BlockPos(targetX, targetY, targetZ)).getBlock().isNormalCube())
            {                
                ropeEnt = new EntityFreeFormRope(worldObj);
                ropeEnt.setStartCoordinates(pos.getX()+0.5D, pos.getY(), pos.getZ()+0.5D);
                ropeEnt.setEndCoordinates(targetX+0.5D, targetY, targetZ+0.5D);
                ropeEnt.setLoosening();
                worldObj.spawnEntityInWorld(ropeEnt);
            }
            else if (!isInvalid())
            {
                System.err.printf("zipline target coords [%d|%d|%d] not normal cube: %s\n", targetX, targetY, targetZ, worldObj.getBlockState(new BlockPos(targetX, targetY, targetZ)).getBlock());
                targetY = -1;
                if (ropeEnt != null)
                {
                    ropeEnt.setDead();
                    ropeEnt = null;
                }
            }
        }
    }
    
}
