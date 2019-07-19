package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.ISidedProxy;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;

public class FinderCompassClient implements ISidedProxy {

    @Override
    public File getMcFolder() {
        return Minecraft.getInstance().gameDir;
    }

    @Override
    public void onReceivedHandshakePacket(HandshakePacket handShakePacket) {
        if (handShakePacket.getUsername().equals("server")) {
            Minecraft.getInstance().addScheduledTask(() -> {
                FinderCompassClientTicker.instance.inputOverrideConfig(handShakePacket.getJson());
            });
        }
    }

    @Override
    public void onReceivedSearchPacket(FeatureSearchPacket packet) {
        if (packet.getUsername().equals("server")) {
            Minecraft.getInstance().addScheduledTask(() -> {
                FinderCompassLogic.featureCoords = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                FinderCompassMod.LOGGER.debug("Finder Compass server sent Feature {} coords: [{}|{}|{}]", packet.getFeatureId(), packet.getX(), packet.getY(), packet.getZ());
                FinderCompassLogic.hasFeature = true;
            });
        } else {
            ServerLifecycleHooks.getCurrentServer().addScheduledTask(() -> {
                EntityPlayerMP p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(packet.getUsername());
                if (p != null) {
                    BlockPos result = ((WorldServer) p.world).getChunkProvider().getChunkGenerator().findNearestStructure(p.world, packet.getFeatureId(), new BlockPos(p), FeatureSearchPacket.SEARCH_RADIUS, false);
                    FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}", packet.getFeatureId(), packet.getUsername(), result);
                    if (result != null) {
                        FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new FeatureSearchPacket("server", packet.getFeatureId(), result.getX(), result.getY(), result.getZ()), p);
                    }
                }
            });
        }
    }
}
