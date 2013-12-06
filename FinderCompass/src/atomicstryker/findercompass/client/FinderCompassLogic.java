package atomicstryker.findercompass.client;

import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChunkCoordinates;
import atomicstryker.ForgePacketWrapper;
import atomicstryker.findercompass.common.CompassIntPair;
import cpw.mods.fml.common.network.PacketDispatcher;

public class FinderCompassLogic
{

    private final ChunkCoordinates NullChunk = new ChunkCoordinates(0, 0, 0);

    private int x;
    private int y;
    private int z;
    private long nextTime;
    private int seccounter;

    private final Minecraft mc;

    public static boolean serverHasFinderCompass = false;
    public static ChunkCoordinates strongholdCoords = new ChunkCoordinates(0, 0, 0);
    public static boolean hasStronghold = false;

    public FinderCompassLogic(Minecraft minecraft)
    {
        mc = minecraft;
        seccounter = 0;
        nextTime = System.currentTimeMillis();
    }

    public void onTick()
    {
        if (mc.theWorld != null && mc.thePlayer != null)
        {
            boolean isNewSecond = false;
            boolean is15SecInterval = false;
            boolean movement = false;
            if (System.currentTimeMillis() > nextTime)
            {
                isNewSecond = true;
                seccounter++;
                nextTime = System.currentTimeMillis() + 1000L;
            }

            if ((int) mc.thePlayer.posX != this.x || (int) mc.thePlayer.posY != this.y || (int) mc.thePlayer.posZ != this.z)
            {
                x = (int) mc.thePlayer.posX;
                y = (int) mc.thePlayer.posY;
                z = (int) mc.thePlayer.posZ;
                movement = true;
            }

            if (isNewSecond && this.seccounter > 14)
            {
                seccounter = 0;
                is15SecInterval = true;

                FinderCompassLogic.hasStronghold = false;
                if (serverHasFinderCompass)
                {
                    PacketDispatcher.sendPacketToServer(ForgePacketWrapper.createPacket("FindrCmps", 1, null));
                }
            }

            int[] configInts;
            CompassIntPair blockInts;
            ChunkCoordinates coords;
            Iterator<Entry<CompassIntPair, int[]>> iter;
            Entry<CompassIntPair, int[]> iterEntry;
            if (movement || isNewSecond)
            {
                CompassSetting currentSetting = FinderCompassClientTicker.instance.getCurrentSetting();
                iter = currentSetting.getCustomNeedles().entrySet().iterator();

                while (iter.hasNext())
                {
                    iterEntry = iter.next();
                    blockInts = iterEntry.getKey();
                    configInts = iterEntry.getValue();
                    if (is15SecInterval || configInts[7] == 0)
                    {
                        coords =
                                findNearestBlockChunkOfIDInRange(currentSetting, blockInts.getBlockID(), blockInts.getDamage(), x, y, z,
                                        configInts[3], configInts[4], configInts[5], configInts[6]);
                        if (coords != null && !coords.equals(NullChunk))
                        {
                            if (currentSetting.getCustomNeedleTargets().containsKey(blockInts))
                            {
                                currentSetting.getCustomNeedleTargets().remove(blockInts);
                            }

                            currentSetting.getCustomNeedleTargets().put(blockInts, coords);
                        }
                        else
                        {
                            currentSetting.getCustomNeedleTargets().remove(blockInts);
                        }
                    }
                }
            }
        }
    }

    /**
     * Is a worker setter/getter for each blockID/damage combo. If a worker is
     * present and busy, it does nothing, if a worker is not present, it makes
     * one, and if a worker found something, it retrieves and puts the found
     * target into the "display" Coordinates Map
     * 
     * @param currentSetting
     */
    private ChunkCoordinates findNearestBlockChunkOfIDInRange(CompassSetting currentSetting,
            int blockID, int meta, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY)
    {
        int[] configInts = { blockID, meta, playerX, playerY, playerZ, xzRange, yRange, minY, maxY };
        CompassIntPair key = new CompassIntPair(blockID, meta);

        ThreadCompassWorker worker = (ThreadCompassWorker) currentSetting.getCompassWorkers().get(key);
        if (worker == null || !worker.isWorking())
        {
            worker = new ThreadCompassWorker(mc, this);
            worker.setPriority(Thread.MIN_PRIORITY);
            currentSetting.getCompassWorkers().put(key, worker);

            worker.setupValues(configInts);
            worker.start();
        }

        ChunkCoordinates result = (ChunkCoordinates) currentSetting.getNewFoundTargets().get(key);
        if (result == null)
        {
            // System.out.println("Did not find saved coords for "+key.getBlockID()+", "+key.getDamage());
            result = (ChunkCoordinates) currentSetting.getCustomNeedleTargets().get(key);
        }
        else
        {
            // System.out.println("Retrieved found coords for "+key.getBlockID()+", "+key.getDamage());
            currentSetting.getNewFoundTargets().remove(key);
        }

        return result;
    }

}
