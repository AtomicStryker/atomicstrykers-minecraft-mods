package atomicstryker.ropesplus.common;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.PacketDispatcher;

public class EntityFreeFormRope extends Entity
{
    /**
     * Determines how long a piece of Rope must be for a segment to be rendered
     */
    private final double SEGMENT_LENGTH = 0.5D;
    
    private boolean hangsTaut;
    private EntityPlayer shooter;
    private double maxLength;
    private double inertiaSpeed;
    private int swingFactor;
    private long nextSoundTime;
    private boolean jungleCall;
    
    public EntityFreeFormRope(World par1World)
    {
        super(par1World);
        ignoreFrustumCheck = true;
        hangsTaut = true;
        shooter = null;
        maxLength = 999D;
        inertiaSpeed = -1;
        swingFactor = -1;
        nextSoundTime = 0;
        jungleCall = false;
    }
    
    @Override
    protected void entityInit()
    {
        dataWatcher.addObject(10, "0"); // startX
        dataWatcher.addObject(11, "0"); // startY
        dataWatcher.addObject(12, "0"); // startZ
        dataWatcher.addObject(13, "0"); // endX
        dataWatcher.addObject(14, "0"); // endY
        dataWatcher.addObject(15, "0"); // endZ
        dataWatcher.addObject(16, "1"); // powValue
    }
    
    public void setShooter(EntityPlayer p)
    {
        shooter = p;
        maxLength = getDistanceToEntity(shooter);
    }
    
    public EntityPlayer getShooter()
    {
        return shooter;
    }
    
    public void setLoosening()
    {
        hangsTaut = false;
    }
    
    public double getStartX()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(10));
    }
    
    public double getStartY()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(11));
    }
    
    public double getStartZ()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(12));
    }
    
    public double getEndX()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(13));
    }
    
    public double getEndY()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(14));
    }
    
    public double getEndZ()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(15));
    }
    
    public double getPowValue()
    {
        return Double.valueOf(dataWatcher.getWatchableObjectString(16));
    }
    
    public void setStartX(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(10, ""+input);
    }
    
    public void setStartY(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(11, ""+input);
    }
    
    public void setStartZ(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(12, ""+input);
    }
    
    public void setEndX(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(13, ""+input);
    }
    
    public void setEndY(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(14, ""+input);
    }
    
    public void setEndZ(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(15, ""+input);
    }
    
    public void setPowValue(double input)
    {
        if (!worldObj.isRemote)
            dataWatcher.updateObject(16, ""+input);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        setStartX(compound.getDouble("startX"));
        setStartY(compound.getDouble("startY"));
        setStartZ(compound.getDouble("startZ"));
        setEndX(compound.getDouble("endX"));
        setEndY(compound.getDouble("endY"));
        setEndZ(compound.getDouble("endZ"));
        setPowValue(compound.getDouble("ropePOWvalue"));
        
        this.setDead(); // TODO dont forget to remove this
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setDouble("startX", getStartX());
        compound.setDouble("startY", getStartY());
        compound.setDouble("startZ", getStartZ());
        compound.setDouble("endX", getEndX());
        compound.setDouble("endY", getEndY());
        compound.setDouble("endZ", getEndZ());
        compound.setDouble("ropePOWvalue", getPowValue());
    }
       
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        if (!isTargetBlockValid())
        {
            this.setDead();
            return;
        }
        
        if (!hangsTaut && getPowValue() < 2D)
        {
            setPowValue(getPowValue()+0.05);
        }

        if (shooter != null)
        {
            setStartCoordinates(shooter.posX, shooter.posY, shooter.posZ);
            double dist = getDistanceToEntity(shooter);

            if (worldObj.isRemote)
            {
                if (RopesPlusCore.proxy.getShouldHookShotDisconnect())
                {
                    shooter = null;
                    RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
                    return;
                }

                if (RopesPlusCore.proxy.getShouldHookShotPull())
                {
                    double distToEnd = shooter.getDistance(getEndX(), getEndY(), getEndZ());
                    if (distToEnd < 3D)
                    {
                        RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
                        RopesPlusCore.proxy.setShouldHookShotPull(false);
                        Object[] toSend = { entityId };
                        PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_Ropes", 5, toSend));
                    }
                    else
                    {
                        Vec3 playerToHookVec = worldObj.getWorldVec3Pool().getVecFromPool(getEndX()-shooter.posX, getEndY()-shooter.posY, getEndZ()-shooter.posZ);
                        playerToHookVec = playerToHookVec.normalize();
                        shooter.addVelocity(-shooter.motionX*0.5, -shooter.motionY*0.5, -shooter.motionZ*0.5);
                        shooter.addVelocity(playerToHookVec.xCoord*0.5, playerToHookVec.yCoord*0.5, playerToHookVec.zCoord*0.5);
                    }
                }
                else
                {                    
                    if (dist > maxLength)
                    {
                        if (inertiaSpeed < 0)
                        {
                            inertiaSpeed = getEntitySpeed(shooter);
                            
                            if (maxLength > 10 && getEndY()-shooter.posY < 5D)
                            {
                                double adder = maxLength-10D;
                                while (adder > 0)
                                {
                                    inertiaSpeed *= 1.4;
                                    adder -= 2D;
                                }
                            }
                        }
                        
                        if (System.currentTimeMillis() > nextSoundTime)
                        {
                            nextSoundTime = System.currentTimeMillis() + 3000l;
                            
                            if (!jungleCall && maxLength > 25 && getEndY()-shooter.posY < 5D)
                            {
                                jungleCall = true;
                                Object[] toSend = { "jungleking" };
                                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_Ropes", 8, toSend));
                            }
                            else
                            {
                                Object[] toSend = { "ropetension" };
                                PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("AS_Ropes", 8, toSend));
                            }
                        }
                        
                        /*
                         * If someone can write a beautiful smooth orthogonal swing curve, by all means do
                         */
                        
                        Vec3 playerToHookVec = worldObj.getWorldVec3Pool().getVecFromPool(getEndX()-shooter.posX, getEndY()-shooter.posY, getEndZ()-shooter.posZ);
                        playerToHookVec = playerToHookVec.normalize();
                        Vec3 mergedVec = playerToHookVec.addVector(shooter.motionX, shooter.motionY, shooter.motionZ);
                        mergedVec = mergedVec.normalize();
                        Vec3 playerFacingVec = shooter.getLookVec();
                        mergedVec.addVector(playerFacingVec.xCoord+shooter.motionX, playerFacingVec.yCoord, playerFacingVec.zCoord+shooter.motionZ);
                        mergedVec = mergedVec.normalize();
                        shooter.addVelocity(-shooter.motionX*0.5, -shooter.motionY*0.5, -shooter.motionZ*0.5);
                        shooter.addVelocity(mergedVec.xCoord*0.5, mergedVec.yCoord*0.5, mergedVec.zCoord*0.5);

                        while (getEntitySpeed(shooter) < inertiaSpeed)
                        {
                            shooter.motionX *= 1.1;
                            shooter.motionY *= 1.1;
                            shooter.motionZ *= 1.1;
                        }

                    }
                }
            }
            else
            {
                shooter.fallDistance = 0;
            }
        }
    }
    
    private boolean isTargetBlockValid()
    {
        return worldObj.isBlockOpaqueCube(MathHelper.floor_double(getEndX()), MathHelper.floor_double(getEndY()-0.5D), MathHelper.floor_double(getEndZ()))
        || worldObj.isBlockOpaqueCube(MathHelper.floor_double(getEndX()), MathHelper.floor_double(getEndY()+0.5D), MathHelper.floor_double(getEndZ()))
        || worldObj.getBlockMaterial(MathHelper.floor_double(getEndX()), MathHelper.floor_double(getEndY()-0.5D), MathHelper.floor_double(getEndZ())) == Material.leaves
        || worldObj.getBlockMaterial(MathHelper.floor_double(getEndX()), MathHelper.floor_double(getEndY()+0.5D), MathHelper.floor_double(getEndZ())) == Material.leaves;
    }

    private double getEntitySpeed(Entity ent)
    {
        return Math.sqrt(ent.motionX*ent.motionX+ent.motionY*ent.motionY+ent.motionZ*ent.motionZ);
    }
    
    /**
     * Attaches the Rope Start to the TOP of target block
     * @param x block coordinate
     * @param y block coordinate
     * @param z block coordinate
     */
    public void setStartBlock(int x, int y, int z)
    {
        setStartCoordinates(x+0.5D, y+1D, z+0.5D);
    }
    
    public void setStartCoordinates(double x, double y, double z)
    {
        setStartX(x);
        setStartY(y);
        setStartZ(z);
        updateEntPos();
    }
    
    /**
     * Makes the rope end attach to the BOTTOM of target Block
     * 
     * @param x block coordinate
     * @param y block coordinate
     * @param z block coordinate
     */
    public void setEndBlock(int x, int y, int z)
    {
        setEndCoordinates(x+0.5D, y, z+0.5D);
    }

    public void setEndCoordinates(double x, double y, double z)
    {
        setEndX(x);
        setEndY(y);
        setEndZ(z);
        updateEntPos();
    }
    
    private void updateEntPos()
    {
        posX = getStartX()+(getEndX()-getStartX());
        posY = getStartY()+(getEndY()-getStartY());
        posZ = getStartZ()+(getEndZ()-getStartZ());
    }
    
    private double getRopeAbsLength()
    {
        return Math.sqrt((getEndX()-getStartX())*(getEndX()-getStartX()) + (getEndY()-getStartY())*(getEndY()-getStartY()) + (getEndZ()-getStartZ())*(getEndZ()-getStartZ()));
    }
    
    /**
     * Determines how many Segments the Entity needs to be broken into for rendering
     */
    public int getSegmentCount()
    {
        return (int) Math.rint(getRopeAbsLength()/SEGMENT_LENGTH);
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
        result[0] = getStartX() + ((getEndX() - getStartX())*relativeDistance);
        result[2] = getStartZ() + ((getEndZ() - getStartZ())*relativeDistance);
        
        if ((relativeDistance*=2)<1)
        {
            result[1] = getStartY() + ((getEndY() - getStartY())*(0.5*Math.pow(relativeDistance, getPowValue())));
        }
        else
        {
            result[1] = getStartY() + ((getEndY() - getStartY())*(1-0.5*Math.abs(Math.pow(2-relativeDistance, getPowValue()))));
        }
        
        return result;
    }

}
