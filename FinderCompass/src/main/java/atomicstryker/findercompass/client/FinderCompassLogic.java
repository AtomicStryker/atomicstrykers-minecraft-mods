package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.CompassTargetData;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Iterator;
import java.util.Map.Entry;

public class FinderCompassLogic {

    public static BlockPos featureCoords = new BlockPos(0, 0, 0);
    public static boolean hasFeature = false;
    private final BlockPos NullChunk = new BlockPos(0, 0, 0);
    private final Minecraft mc;
    private BlockPos oldPos;
    private long nextTime;
    private int seccounter;

    public FinderCompassLogic(Minecraft minecraft) {
        mc = minecraft;
        seccounter = 0;
        nextTime = System.currentTimeMillis();
    }

    public void onTick() {
        if (mc.level != null && mc.player != null) {
            boolean isNewSecond = false;
            boolean is15SecInterval = false;
            boolean movement = false;
            if (System.currentTimeMillis() > nextTime) {
                isNewSecond = true;
                seccounter++;
                nextTime = System.currentTimeMillis() + 1000L;
            }

            BlockPos pos = new BlockPos(mc.player.getOnPos());
            if (!pos.equals(oldPos)) {
                oldPos = pos;
                movement = true;
            }

            if (isNewSecond && this.seccounter > 14) {
                seccounter = 0;
                is15SecInterval = true;

                FinderCompassLogic.hasFeature = false;
            }

            int[] configInts;
            CompassTargetData targetData;
            BlockPos coords;
            Iterator<Entry<CompassTargetData, int[]>> iter;
            Entry<CompassTargetData, int[]> iterEntry;
            if (movement || isNewSecond) {
                CompassSetting currentSetting = FinderCompassClientTicker.instance.getCurrentSetting();
                iter = currentSetting.getCustomNeedles().entrySet().iterator();
                //System.out.println("finder compass second ticker");

                if (is15SecInterval && currentSetting.getFeatureNeedle() != null) {
                    FeatureSearchPacket featureSearchPacket = new FeatureSearchPacket(mc.player.getOnPos().getX(), mc.player.getOnPos().getY(), mc.player.getOnPos().getZ(), mc.player.getName().getString(), currentSetting.getFeatureNeedle());
                    PacketDistributor.sendToServer(featureSearchPacket);
                }

                while (iter.hasNext()) {
                    iterEntry = iter.next();
                    targetData = iterEntry.getKey();
                    configInts = iterEntry.getValue();
                    if (is15SecInterval || configInts[7] == 0) {
                        coords =
                                findNearestBlockChunkOfIDInRange(currentSetting, targetData.getBlockState(),
                                        pos.getX(), pos.getY(), pos.getZ(),
                                        configInts[3], configInts[4], configInts[5], configInts[6]);
                        if (coords != null && !coords.equals(NullChunk)) {
                            currentSetting.getCustomNeedleTargets().put(targetData, coords);
                        } else {
                            currentSetting.getCustomNeedleTargets().remove(targetData);
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
                                                      BlockState blockState, int playerX, int playerY, int playerZ, int xzRange, int yRange, int minY, int maxY) {
        CompassTargetData key = new CompassTargetData(blockState);

        ThreadCompassWorker worker = currentSetting.getCompassWorkers().get(key);
        if (worker == null || !worker.isWorking()) {
            worker = new ThreadCompassWorker(mc);
            worker.setPriority(Thread.MIN_PRIORITY);
            currentSetting.getCompassWorkers().put(key, worker);

            worker.setupValues(blockState, playerX, playerY, playerZ, xzRange, yRange, minY, maxY);
            worker.start();
        }

        BlockPos result = currentSetting.getNewFoundTargets().get(key);
        if (result == null) {
            // System.out.println("Did not find saved coords for "+key.getBlockID()+", "+key.getDamage());
            result = currentSetting.getCustomNeedleTargets().get(key);
        } else {
            // System.out.println("Retrieved found coords for "+key.getBlockID()+", "+key.getDamage());
            currentSetting.getNewFoundTargets().remove(key);
        }

        return result;
    }

}
