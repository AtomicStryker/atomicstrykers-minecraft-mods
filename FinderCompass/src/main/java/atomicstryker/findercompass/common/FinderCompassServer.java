package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;

public class FinderCompassServer implements ISidedProxy {

    @Override
    public void commonSetup() {
        // NOOP
    }

    @Override
    public File getMcFolder() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getFile("");
    }

    @Override
    public void onReceivedHandshakePacket(HandshakePacket handShakePacket) {
        // NOOP
    }

    @Override
    public void onReceivedSearchPacket(FeatureSearchPacket packet) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.submitAsync(() -> {
            ServerPlayer p = server.getPlayerList().getPlayerByName(packet.getUsername());
            if (p != null) {
                BlockPos result = ((ServerLevel) p.level).getChunkSource().getGenerator().findNearestMapFeature((ServerLevel) p.level, StructureFeature.STRUCTURES_REGISTRY.get(packet.getFeatureId()), new BlockPos(p.getOnPos()), FeatureSearchPacket.SEARCH_RADIUS, false);
                FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}", packet.getFeatureId(), packet.getUsername(), result);
                if (result != null) {
                    FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new FeatureSearchPacket("server", packet.getFeatureId(), result.getX(), result.getY(), result.getZ()), p);
                }
            }
        });
    }
}
