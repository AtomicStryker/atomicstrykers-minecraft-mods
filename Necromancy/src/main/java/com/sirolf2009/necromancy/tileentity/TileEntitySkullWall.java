package com.sirolf2009.necromancy.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySkullWall extends TileEntity
{

    private String skullType = "Skeleton";
    private int blockColor = 0;

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setString("SkullType", skullType);
        par1NBTTagCompound.setInteger("BlockColor", blockColor);
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        skullType = par1NBTTagCompound.getString("SkullType");
        blockColor = par1NBTTagCompound.getInteger("BlockColor");
    }

    /**
     * Overriden in a sign to provide the text.
     */
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 4, nbttagcompound);
    }

    /**
     * Set the entity type for the skull
     */
    public void setSkullType(String par1)
    {
        skullType = par1;
    }

    /**
     * Get the entity type for the skull
     */
    public String getSkullType()
    {
        return skullType;
    }
}
