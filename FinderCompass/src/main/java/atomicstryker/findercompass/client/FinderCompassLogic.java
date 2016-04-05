package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.StrongholdPacket;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Map.Entry;

public class FinderCompassLogic
{

    private final BlockPos NullChunk = new BlockPos(0, 0, 0);

    private int x;
    private int y;
    private int z;
    private long nextTime;
    private int seccounter;

    private final Minecraft mc;

    public static boolean serverHasFinderCompass = false;
    public static BlockPos strongholdCoords = new BlockPos(0, 0, 0);
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
                    FinderCompassMod.instance.networkHelper.sendPacketToServer(new StrongholdPacket(mc.thePlayer.getGameProfile().getName()));
                }
            }

            int[] configInts;
            CompassTargetData blockInts;
            BlockPos coords;
            Iterator<Entry<CompassTargetData, int[]>> iter;
            Entry<CompassTargetData, int[]> iterEntry;
            if (movement || isNewSecond)
            {
                CompassSetting currentSetting = FinderCompassClientTicker.instance.getCurrentSetting();
                iter = currentSetting.getCustomNeedles().entrySet().iterator();
                //System.out.println("finder compass second ticker");

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
     * @param currentSetting CompassSetting instance
     */
    private BlockPos findNearestBlockChunkOfIDInRange(CompassSetting currentSetting,
            Block blockID, int meta, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY)
    {
        int[] configInts = { meta, playerX, playerY, playerZ, xzRange, yRange, minY, maxY };
        CompassTargetData key = new CompassTargetData(blockID, meta);

        ThreadCompassWorker worker = currentSetting.getCompassWorkers().get(key);
        if (worker == null || !worker.isWorking())
        {
            worker = new ThreadCompassWorker(mc);
            worker.setPriority(Thread.MIN_PRIORITY);
            currentSetting.getCompassWorkers().put(key, worker);

            worker.setupValues(blockID, configInts);
            worker.start();
        }

        BlockPos result = currentSetting.getNewFoundTargets().get(key);
        if (result == null)
        {
            // System.out.println("Did not find saved coords for "+key.getBlockID()+", "+key.getDamage());
            result = currentSetting.getCustomNeedleTargets().get(key);
        }
        else
        {
            // System.out.println("Retrieved found coords for "+key.getBlockID()+", "+key.getDamage());
            currentSetting.getNewFoundTargets().remove(key);
        }

        return result;
    }

}
