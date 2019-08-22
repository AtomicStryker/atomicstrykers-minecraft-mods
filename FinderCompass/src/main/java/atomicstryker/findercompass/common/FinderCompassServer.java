package atomicstryker.findercompass.common;

import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.io.File;

public class FinderCompassServer implements ISidedProxy {

    @Override
    public File getMcFolder() {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getFile("");
    }

    @Override
    public void onReceivedHandshakePacket(HandshakePacket handShakePacket) {
        // NOOP
    }

    @Override
    public void onReceivedSearchPacket(FeatureSearchPacket packet) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        server.deferTask(() -> {
            ServerPlayerEntity p = server.getPlayerList().getPlayerByUsername(packet.getUsername());
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
