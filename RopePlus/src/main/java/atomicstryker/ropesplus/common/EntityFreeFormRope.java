package atomicstryker.ropesplus.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import atomicstryker.ropesplus.common.network.HookshotPullPacket;
import atomicstryker.ropesplus.common.network.SoundPacket;

public class EntityFreeFormRope extends Entity
{
    /**
     * Determines how long a piece of Rope must be for a segment to be rendered
     */
    private final double SEGMENT_LENGTH = 0.5D;
    
    private final Vec3 swingStartPoint;
    private final Vec3 anchorLoc;
    private final Vec3 playerLoc;
    private final Vec3 playerToAnchorVec;
    private final Vec3 rightVec;
    
    private boolean hangsTaut;
    private EntityPlayer shooter;
    private double maxLength;
    private double inertiaSpeed;
    private long nextSoundTime;
    private boolean jungleCall;
    
    public EntityFreeFormRope(World world)
    {
        super(world);
        ignoreFrustumCheck = true;
        hangsTaut = true;
        shooter = null;
        maxLength = 999D;
        inertiaSpeed = -1;
        nextSoundTime = 0;
        jungleCall = false;
        swingStartPoint = Vec3.createVectorHelper(0, 0, 0);
        anchorLoc = Vec3.createVectorHelper(0, 0, 0);
        playerLoc = Vec3.createVectorHelper(0, 0, 0);
        playerToAnchorVec = Vec3.createVectorHelper(0, 0, 0);
        rightVec = Vec3.createVectorHelper(0, 0, 0);
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
        if (compound.getBoolean("attachedToEnt"))
        {
            setDead();
        }
        else
        {
            setStartX(compound.getDouble("startX"));
            setStartY(compound.getDouble("startY"));
            setStartZ(compound.getDouble("startZ"));
            setEndX(compound.getDouble("endX"));
            setEndY(compound.getDouble("endY"));
            setEndZ(compound.getDouble("endZ"));
            setPowValue(compound.getDouble("ropePOWvalue"));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setBoolean("attachedToEnt", shooter != null);
        compound.setDouble("startX", getStartX());
        compound.setDouble("startY", getStartY());
        compound.setDouble("startZ", getStartZ());
        compound.setDouble("endX", getEndX());
        compound.setDouble("endY", getEndY());
        compound.setDouble("endZ", getEndZ());
        compound.setDouble("ropePOWvalue", getPowValue());
    }
    
    @Override
    public void setDead()
    {
        if (shooter != null)
        {
            RopesPlusCore.proxy.setHasClientRopeOut(false);
            RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
            RopesPlusCore.proxy.setShouldRopeChangeState(0f);
            RopesPlusCore.instance.setPlayerRope(shooter, null);
        }
        
        super.setDead();
    }
       
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        final int endX = MathHelper.floor_double(getEndX());
        final int endY = MathHelper.floor_double(getEndY()) + (shooter == null ? -1 : 0);
        final int endZ = MathHelper.floor_double(getEndZ());
        if (!worldObj.getBlock(endX, endY, endZ).isNormalCube())
        {
            System.out.printf("%d %d %d is not normal cube, it is %s\n", endX, endY, endZ, worldObj.getBlock(endX, endY, endZ));
            this.setDead();
            return;
        }
        
        if (!hangsTaut && getPowValue() < 2D)
        {
            setPowValue(getPowValue()+0.05);
        }

        if (shooter != null)
        {
            if (shooter.isDead || !shooter.inventory.hasItem(RopesPlusCore.instance.itemHookShot))
            {
                setDead();
                RopesPlusCore.proxy.setHasClientRopeOut(false);
                RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
                RopesPlusCore.proxy.setShouldRopeChangeState(0f);
                return;
            }
            
            setStartCoordinates(shooter.posX, shooter.posY, shooter.posZ);
            Vec3 playerToHookVec = worldObj.getWorldVec3Pool().getVecFromPool(getEndX()-shooter.posX, getEndY()-shooter.posY, getEndZ()-shooter.posZ);
            double dist = playerToHookVec.lengthVector();

            if (worldObj.isRemote)
            {
                if (RopesPlusCore.proxy.getShouldHookShotDisconnect())
                {
                    // add a jump motion
                    shooter.playSound("random.pop", 1f, 1f);
                    shooter.motionY += 0.42;
                    shooter = null;
                    RopesPlusCore.proxy.setShouldHookShotDisconnect(false);
                    return;
                }

                if (RopesPlusCore.proxy.getShouldRopeChangeState() < 0f)
                {
                    if (dist < 3D)
                    {
                        RopesPlusCore.proxy.setHasClientRopeOut(false);
                        RopesPlusCore.proxy.setShouldHookShotDisconnect(true);
                        RopesPlusCore.proxy.setShouldRopeChangeState(0f);
                        RopesPlusCore.instance.networkHelper.sendPacketToServer(new HookshotPullPacket(shooter.getCommandSenderName(), getEntityId()));
                    }
                    else
                    {
                        playerToHookVec = playerToHookVec.normalize();
                        shooter.addVelocity(-shooter.motionX*0.5, -shooter.motionY*0.5, -shooter.motionZ*0.5);
                        shooter.addVelocity(playerToHookVec.xCoord*0.5, playerToHookVec.yCoord*0.5, playerToHookVec.zCoord*0.5);
                    }
                }
                else
                {
                    maxLength = Math.min(Settings_RopePlus.maxHookShotRopeLength, maxLength+RopesPlusCore.proxy.getShouldRopeChangeState());
                    RopesPlusCore.proxy.setShouldRopeChangeState(0f); // maxLengh extended if applicable, reset state
                    if (inertiaSpeed < 0 && !shooter.isCollidedVertically && shooter.motionY < -0.1)
                    {
                        dist -= 1d;
                        maxLength = dist;
                    }
                    
                    if (dist >= maxLength)
                    {
                        if (shooter.isCollidedVertically)
                        {
                            // hit ground, reset
                            inertiaSpeed = -1;
                        }
                        else
                        {
                            final double heightFromAnchor = getEndY()-shooter.posY;
                            
                            if (inertiaSpeed < 0)
                            {
                                inertiaSpeed = getShooterSpeed() + (maxLength-heightFromAnchor)/10;
                                
                                swingStartPoint.xCoord = shooter.posX;
                                swingStartPoint.yCoord = shooter.posY;
                                swingStartPoint.zCoord = shooter.posZ;
                                
                                anchorLoc.xCoord = getEndX();
                                anchorLoc.yCoord = getEndY();
                                anchorLoc.zCoord = getEndZ();
                            }
                            
                            if (System.currentTimeMillis() > nextSoundTime)
                            {
                                nextSoundTime = System.currentTimeMillis() + 3000l;
                                
                                if (!jungleCall && maxLength > 25 && getEndY()-shooter.posY < 5D)
                                {
                                    jungleCall = true;
                                    RopesPlusCore.instance.networkHelper.sendPacketToServer(new SoundPacket(shooter.getCommandSenderName(), "ropesplus:jungleking"));
                                }
                                else
                                {
                                    RopesPlusCore.instance.networkHelper.sendPacketToServer(new SoundPacket(shooter.getCommandSenderName(), "ropesplus:ropetension"));
                                }
                            }
                            
                            // shorten the rope back to max length, set swinger position accordingly
                            final Vec3 anchorToPlayerVec = worldObj.getWorldVec3Pool().getVecFromPool(shooter.posX-getEndX(), shooter.posY-getEndY(), shooter.posZ-getEndZ()).normalize();
                            anchorToPlayerVec.xCoord *= maxLength;
                            anchorToPlayerVec.yCoord *= maxLength;
                            anchorToPlayerVec.zCoord *= maxLength;
                            shooter.setPosition(anchorToPlayerVec.xCoord+getEndX(), anchorToPlayerVec.yCoord+getEndY(), anchorToPlayerVec.zCoord+getEndZ());
                            
                            playerToAnchorVec.xCoord = getEndX()-shooter.posX;
                            playerToAnchorVec.yCoord = heightFromAnchor;
                            playerToAnchorVec.zCoord = getEndZ()-shooter.posZ;
                            
                            playerLoc.xCoord = shooter.posX;
                            playerLoc.yCoord = shooter.posY;
                            playerLoc.zCoord = shooter.posZ;
                            
                            rightVec.xCoord = playerToAnchorVec.xCoord;
                            rightVec.zCoord = playerToAnchorVec.zCoord;
                            
                            double relativeEnergy = inertiaSpeed;
                            
                            boolean downSwing = distXZManhattan(swingStartPoint, playerLoc) < distXZManhattan(swingStartPoint, anchorLoc);
                            // down swing
                            if (downSwing)
                            {
                                rightVec.rotateAroundY(-90f);
                            }
                            // up swing
                            else
                            {
                                rightVec.rotateAroundY(90f);
                            }
                            
                            // below anchor, apply potential energy reduction
                            if (heightFromAnchor > 0)
                            {
                                relativeEnergy *= heightFromAnchor/getRopeAbsLength();
                            }
                            
                            final Vec3 tangent = playerToAnchorVec.crossProduct(rightVec).normalize();
                            
                            // option #1, just set ideal scaled motion
                            shooter.motionX = tangent.xCoord*relativeEnergy;
                            shooter.motionY = tangent.yCoord*relativeEnergy;
                            shooter.motionZ = tangent.zCoord*relativeEnergy;
                            
                            /*
                            // option #2, merge motions, normalize, scale
                            // buggy.
                            tangent.xCoord += shooter.motionX;
                            tangent.yCoord += shooter.motionY;
                            tangent.zCoord += shooter.motionZ;
                            tangent.normalize();
                            shooter.motionX = tangent.xCoord * relativeEnergy;
                            shooter.motionY = tangent.yCoord * relativeEnergy;
                            shooter.motionZ = tangent.zCoord * relativeEnergy;
                            */
                            
                            if (!downSwing && relativeEnergy < 0.15)
                            {
                                // reset swing! start new one
                                inertiaSpeed = -1;
                            }
                            
                            shooter.fallDistance = 0;
                        }
                    }
                }
            }
            else
            {
                shooter.fallDistance = 0;
            }
        }
        else // shooter == null
        {
            int startX = MathHelper.floor_double(getStartX());
            int startY = MathHelper.floor_double(getStartY());
            int startZ = MathHelper.floor_double(getStartZ());
            if (worldObj.getBlock(startX, startY, startZ) != RopesPlusCore.instance.blockZipLineAnchor)
            {
                System.out.printf("[%d,%d,%d] is not anchor: %s\n", startX, startY, startZ, worldObj.getBlock(startX, startY, startZ));
                setDead();
                return;
            }
        }
        
        updateEntPos();
    }
    
    private double distXZManhattan(Vec3 a, Vec3 b)
    {
        final double xd = a.xCoord-b.xCoord;
        final double zd = a.zCoord-b.zCoord;
        return xd*xd + zd*zd;
    }

    private double getShooterSpeed()
    {
        return Math.sqrt(shooter.motionX*shooter.motionX+shooter.motionY*shooter.motionY+shooter.motionZ*shooter.motionZ);
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
    
    public double getRopeAbsLength()
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
