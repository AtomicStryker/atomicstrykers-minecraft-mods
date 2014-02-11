package com.sirolf2009.necromancy.tileentity;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDye;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class TileEntityScent extends TileEntity
{

    /**
     * One for each dye plus one for air
     */
    private int[] scents = new int[17];
    private int red, green, blue, alpha;
    
    private final Random rand = new Random();
    private int reach;
    private int[] sourceLocation = new int[3];
    private boolean isMarkedForDespawn;
    public IIcon[] icons = new IIcon[6];
    public int iconTimer = 0;
    
    @Override
    public void updateEntity()
    {
        if (getAir() < 200)
        {
            if (rand.nextInt(4) == 0)
                setAir(getAir() + 1);
            calculateColors();
        }
        else
        {
            worldObj.getTileEntity(xCoord, yCoord, zCoord).invalidate();
            isMarkedForDespawn = true;
            return;
        }
        spreadScents();
    }

    @Override
    public void onChunkUnload()
    {
        if (isMarkedForDespawn)
        {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }
    }

    private void spreadScents()
    {
        for (int x2 = 0; x2 < 3; x2++)
        {
            for (int y2 = 0; y2 < 3; y2++)
            {
                for (int z2 = 0; z2 < 3; z2++)
                {
                    int x = x2 + xCoord - 1;
                    int y = y2 + yCoord - 1;
                    int z = z2 + zCoord - 1;
                    Block ID = worldObj.getBlock(x, y, z);
                    if (worldObj.getTileEntity(x, y, z) == this)
                        continue;
                    if (ID == worldObj.getBlock(xCoord, yCoord, zCoord))
                    {
                        TileEntityScent tileEntityScent = (TileEntityScent) worldObj.getTileEntity(x2 + xCoord - 1, y2 + yCoord - 1, z2 + zCoord - 1);
                        spreadScentTo(tileEntityScent);
                    }
                    else if (worldObj.isRemote && reach >= Math.sqrt(getDistanceFrom(sourceLocation[0], sourceLocation[1], sourceLocation[2]))
                            && ID == Blocks.air && rand.nextInt(100) == 0)
                    {
                        worldObj.setBlock(x, y, z, worldObj.getBlock(xCoord, yCoord, zCoord));
                        TileEntityScent tileEntityScent = (TileEntityScent) worldObj.getTileEntity(x, y, z);
                        tileEntityScent.setSourceLocation(sourceLocation);
                        tileEntityScent.setAir(getAir() + 10);
                        tileEntityScent.setReach(getReach());
                        spreadScentTo(tileEntityScent);
                    }
                }
            }
        }
    }

    private void spreadScentTo(TileEntityScent tileEntityScent)
    {
        for (int i = 0; i < 16; i++)
        {
            if (getScent(i) != tileEntityScent.getScent(i))
            {
                int delta = getScent(i) - tileEntityScent.getScent(i);
                if (delta > 8)
                {
                    addScent(i, -8);
                    tileEntityScent.addScent(i, 8);
                }
            }
        }
        int delta = getScent(16) - tileEntityScent.getScent(16);
        if (delta > 8)
        {
            tileEntityScent.setAir(tileEntityScent.getAir() + 8);
            setAir(getAir() - 8);
        }
    }

    private void calculateColors()
    {
        red = 0;
        blue = 0;
        green = 0;
        alpha = 255 - 255 * scents[16] / 255;
        for (int i = 0; i < ItemDye.field_150922_c.length; i++)
        {
            red += (ItemDye.field_150922_c[i] >> 16) & 0xFF * scents[i] / 100;
            green += (ItemDye.field_150922_c[i] >> 8) & 0xFF * scents[i] / 100;
            blue += (ItemDye.field_150922_c[i] >> 0) & 0xFF * scents[i] / 100;
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    /**
     * 
     * @param scentIndex
     * @param amount
     */
    public void addScent(int scentIndex, int amount)
    {
        if (scentIndex == 16)
        {
            setAir(getAir() + amount);
            return;
        }
        scents[scentIndex] += amount;
        int oldTotal = 0;
        for (int i = 0; i < 16; i++)
        {
            oldTotal += scents[i];
        }
        if (oldTotal != 0)
        {
            for (int i = 0; i < 16; i++)
            {
                double scent = scents[i];
                scent /= oldTotal;
                scent *= 100;
                scents[i] = (int) scent;
            }
        }
    }
    
    /**
     * 
     * @param sourceLocation
     */
    public void setSourceLocation(int[] sourceLocation)
    {
        this.sourceLocation[0] = sourceLocation[0];
        this.sourceLocation[1] = sourceLocation[1];
        this.sourceLocation[2] = sourceLocation[2];
    }

    public int getRed()
    {
        return red;
    }

    public int getGreen()
    {
        return green;
    }

    public int getBlue()
    {
        return blue;
    }

    public int getAlpha()
    {
        return alpha;
    }

    private int getScent(int scentIndex)
    {
        return scents[scentIndex];
    }
    
    /**
     * 
     * @param amount
     */
    public void setAir(int amount)
    {
        scents[16] = amount;
        if (getAir() > 255)
        {
            scents[16] = 255;
        }
        if (getAir() < 0)
        {
            scents[16] = 0;
        }
    }

    public int getAir()
    {
        return scents[16];
    }

    public int getReach()
    {
        return reach;
    }

    public void setReach(int reach)
    {
        this.reach = reach;
    }
}