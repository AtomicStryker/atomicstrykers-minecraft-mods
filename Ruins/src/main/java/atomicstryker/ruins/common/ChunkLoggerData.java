package atomicstryker.ruins.common;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class ChunkLoggerData extends WorldSavedData
{
    private final ArrayList<Integer> xCoords;
    private final ArrayList<Integer> zCoords;

    public ChunkLoggerData(String name)
    {
        super(name);
        xCoords = new ArrayList<>();
        zCoords = new ArrayList<>();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        int[] xload = nbt.getIntArray("xcoords");
        System.out.println("Ruins chunks logged: " + xload.length);
        if (xload.length > 0)
        {
            xCoords.clear();
            for (int i : xload)
            {
                xCoords.add(i);
            }
        }
        int[] zload = nbt.getIntArray("zcoords");
        if (zload.length > 0)
        {
            zCoords.clear();
            for (int i : zload)
            {
                zCoords.add(i);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        int[] xsave = new int[xCoords.size()];
        int[] zsave = new int[zCoords.size()];
        for (int i = 0; i < xsave.length; i++)
        {
            xsave[i] = xCoords.get(i);
            zsave[i] = zCoords.get(i);
        }
        nbt.setIntArray("xcoords", xsave);
        nbt.setIntArray("zcoords", zsave);
        return nbt;
    }

    public boolean catchChunkBug(int chunkX, int chunkZ)
    {
        for (int i = 0; i < xCoords.size(); i++)
        {
            if (chunkX == xCoords.get(i) && chunkZ == zCoords.get(i))
            {
                return true;
            }
        }

        xCoords.add(chunkX);
        zCoords.add(chunkZ);
        setDirty(true);

        return false;
    }

}