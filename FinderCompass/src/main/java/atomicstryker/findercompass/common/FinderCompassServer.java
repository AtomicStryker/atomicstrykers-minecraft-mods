package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class FinderCompassServer implements ISidedProxy {

    @Override
    public void onReceivedHandshakePacket(HandshakePacket handShakePacket) {
        // NOOP
    }

    @Override
    public void onReceivedSearchPacket(FeatureSearchPacket packet) {
        ServerLifecycleHooks.getCurrentServer().deferTask(() -> {
            ServerPlayerEntity p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(packet.getUsername());
            if (p != null) {
                BlockPos result = ((ServerWorld) p.world).getChunkProvider().getChunkGenerator().findNearestStructure(p.world, packet.getFeatureId(), new BlockPos(p), FeatureSearchPacket.SEARCH_RADIUS, false);
                FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}", packet.getFeatureId(), packet.getUsername(), result);
                if (result != null) {
                    FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new FeatureSearchPacket("server", packet.getFeatureId(), result.getX(), result.getY(), result.getZ()), p);
                }
            }
        });
    }
}
