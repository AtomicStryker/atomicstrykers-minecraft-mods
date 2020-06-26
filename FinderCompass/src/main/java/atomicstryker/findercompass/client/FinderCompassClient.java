package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.ISidedProxy;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
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
            Minecraft.getInstance().deferTask(() -> {
                FinderCompassClientTicker.instance.inputOverrideConfig(handShakePacket.getJson());
            });
        }
    }

    @Override
    public void onReceivedSearchPacket(FeatureSearchPacket packet) {
        if (packet.getUsername().equals("server")) {
            Minecraft.getInstance().deferTask(() -> {
                FinderCompassLogic.featureCoords = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                FinderCompassMod.LOGGER.debug("Finder Compass server sent Feature {} coords: [{}|{}|{}]", packet.getFeatureId(), packet.getX(), packet.getY(), packet.getZ());
                FinderCompassLogic.hasFeature = true;
            });
        } else {
            ServerLifecycleHooks.getCurrentServer().deferTask(() -> {
                ServerPlayerEntity p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(packet.getUsername());
                if (p != null) {
                    // code always found in EyeOfEnderEntity, structure registry is BiMap in structure
                    BlockPos result = ((ServerWorld) p.world).getChunkProvider().getChunkGenerator().func_235956_a_((ServerWorld) p.world, Structure.field_236365_a_.get(packet.getFeatureId()), new BlockPos(p.getPositionVec()), FeatureSearchPacket.SEARCH_RADIUS, false);
                    FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}", packet.getFeatureId(), packet.getUsername(), result);
                    if (result != null) {
                        FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new FeatureSearchPacket("server", packet.getFeatureId(), result.getX(), result.getY(), result.getZ()), p);
                    }
                }
            });
        }
    }
}
