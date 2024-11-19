package atomicstryker.ruins.common;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;

public class ChunkLoggerData extends SavedData {
    private final ArrayList<ChunkPos> coords;

    public ChunkLoggerData() {
        super();
        coords = new ArrayList<>();
    }

    public static ChunkLoggerData load(CompoundTag nbt) {
        ChunkLoggerData data = new ChunkLoggerData();
        int[] xload = nbt.getIntArray("xcoords");
        int[] zload = nbt.getIntArray("zcoords");
        System.out.println("Ruins chunks logged: " + xload.length);
        if (xload.length > 0) {
            for (int i = 0; i < xload.length; i++) {
                data.coords.add(new ChunkPos(xload[i], zload[i]));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
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