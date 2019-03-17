package atomicstryker.ruins.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;

public class ChunkLoggerData extends WorldSavedData {
    private final ArrayList<ChunkPos> coords;

    public ChunkLoggerData(String name) {
        super(name);
        coords = new ArrayList<>();
    }

    @Override
    public void read(NBTTagCompound nbt) {
        int[] xload = nbt.getIntArray("xcoords");
        int[] zload = nbt.getIntArray("zcoords");
        System.out.println("Ruins chunks logged: " + xload.length);
        if (xload.length > 0) {
            coords.clear();
            for (int i = 0; i < xload.length; i++) {
                coords.add(new ChunkPos(xload[i], zload[i]));
            }
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound nbt) {
        int[] xsave = new int[coords.size()];
        int[] zsave = new int[coords.size()];
        for (int i = 0; i < xsave.length; i++) {
            xsave[i] = coords.get(i).x;
            zsave[i] = coords.get(i).z;
        }
        nbt.putIntArray("xcoords", xsave);
        nbt.putIntArray("zcoords", zsave);
        return nbt;
    }

    public boolean catchChunkBug(ChunkPos chunkPos) {
        if (coords.contains(chunkPos)) {
            return true;
        }
        coords.add(chunkPos);
        setDirty(true);

        return false;
    }

}