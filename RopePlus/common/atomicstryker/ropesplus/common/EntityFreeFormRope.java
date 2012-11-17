package atomicstryker.ropesplus.common;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityFreeFormRope extends Entity
{
    /**
     * Determines how long a piece of Rope must be for a segment to be rendered
     */
    private final double SEGMENT_LENGTH = 0.33D;
    
    private double startX;
    private double startY;
    private double startZ;
    private double endX;
    private double endY;
    private double endZ;
    
    /**
     * Determines how much the rope eases in and out. A POW of 2 lets it have quadratic starts and endings.
     */
    private double ropePOWvalue;
    
    
    public EntityFreeFormRope(World par1World)
    {
        super(par1World);
        startX = startY = startZ = endX = endY = endZ = 0;
        ropePOWvalue = 2D;
        ignoreFrustumCheck = true;
    }
    
    @Override
    protected void entityInit()
    {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        compound.setDouble("startX", startX);
        compound.setDouble("startY", startY);
        compound.setDouble("startZ", startZ);
        compound.setDouble("endX", endX);
        compound.setDouble("endY", endY);
        compound.setDouble("endZ", endZ);
        compound.setDouble("ropePOWvalue", ropePOWvalue);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        startX = compound.getDouble("startX");
        startY = compound.getDouble("startY");
        startZ = compound.getDouble("startZ");
        endX = compound.getDouble("endX");
        endY = compound.getDouble("endY");
        endZ = compound.getDouble("endZ");
        ropePOWvalue = compound.getDouble("ropePOWvalue");
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        //EntityPlayer p = FMLClientHandler.instance().getClient().thePlayer;
        //setEndCoordinates(p.posX, p.posY-1D, p.posZ);
    }
    
    public void setStartBlock(int x, int y, int z)
    {
        setStartCoordinates(x+0.5D, y+1D, z+0.5D);
    }
    
    public void setStartCoordinates(double x, double y, double z)
    {
        startX = x;
        startY = y;
        startZ = z;
        updateEntPos();
    }
    
    public void setEndBlock(int x, int y, int z)
    {
        setEndCoordinates(x+0.5D, y+1D, z+0.5D);
    }

    public void setEndCoordinates(double x, double y, double z)
    {
        endX = x;
        endY = y;
        endZ = z;
        updateEntPos();
    }
    
    private void updateEntPos()
    {
        posX = startX+(endX-startX);
        posY = startY+(endY-startY);
        posZ = startZ+(endZ-startZ);
    }
    
    /**
     * Determines how many Segments the Entity needs to be broken into for rendering
     */
    public int getSegmentCount()
    {
        double distance = Math.sqrt((endX-startX)*(endX-startX)*(endY-startY)*(endY-startY)*(endZ-startZ)*(endZ-startZ));
        return (int) Math.rint(Math.pow(distance/SEGMENT_LENGTH, 1.5));
    }
    
    /**
     * Computes the threedimensional coordinates of any point on the Rope from the start
     * and ending coordinates, and the ease-in and out POW value.
     * 
     * @param relativeDistance on the Rope you want, from 0 to 1
     * @return array of double coordinates {x, y, z}
     */
    public double[] getCoordsAtRelativeLength(float relativeDistance)
    {
        double[] result = new double[3];
        result[0] = startX + ((endX - startX)*relativeDistance);
        result[2] = startZ + ((endZ - startZ)*relativeDistance);
        
        if ((relativeDistance*=2)<1)
        {
            result[1] = startY + ((endY - startY)*(0.5*Math.pow(relativeDistance, ropePOWvalue)));
        }
        else
        {
            result[1] = startY + ((endY - startY)*(1-0.5*Math.abs(Math.pow(2-relativeDistance, ropePOWvalue))));
        }
        
        return result;
    }

}
