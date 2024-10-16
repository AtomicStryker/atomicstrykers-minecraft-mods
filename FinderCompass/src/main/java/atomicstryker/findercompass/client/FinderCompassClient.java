package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.ISidedProxy;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Path;

public class FinderCompassClient implements ISidedProxy {

    private static final FinderCompassClient INSTANCE = new FinderCompassClient();

    public static FinderCompassClient getInstance() {
        return INSTANCE;
    }

    @Override
    public void commonSetup() {
        FinderCompassClientTicker.instance = new FinderCompassClientTicker();
        FinderCompassClientTicker.instance.onLoad();
        FinderCompassClientTicker.instance.switchSetting();
    }

    @Override
    public Path getMcFolder() {
        return Minecraft.getInstance().gameDirectory.toPath();
    }

    public void handleHandshake(final HandshakePacket handShakePacket, final IPayloadContext context) {
        FinderCompassMod.instance.initIfNeeded();
        FinderCompassMod.LOGGER.info("client received Finder Compass HandshakePacket, from username: {}", handShakePacket.username());
        if (handShakePacket.username().equals("server")) {
            String json = handShakePacket.json();
            FinderCompassMod.LOGGER.info("deferring config override task with json of length {}", json.length());
            Minecraft.getInstance().submitAsync(() -> {
                FinderCompassMod.LOGGER.info("executing deferred config override, FinderCompassClientTicker.instance is: {}", FinderCompassClientTicker.instance);
                FinderCompassClientTicker.instance.inputOverrideConfig(json);
            });
        }
    }

    public void handleFeatureSearch(final FeatureSearchPacket packet, final IPayloadContext context) {
        if (packet.username().equals("server")) {
            Minecraft.getInstance().submitAsync(() -> {
                FinderCompassLogic.featureCoords = new BlockPos(packet.x(), packet.y(), packet.z());
                FinderCompassMod.LOGGER.debug("Finder Compass server sent Feature {} coords: [{}|{}|{}]", packet.featureId(), packet.x(), packet.y(), packet.z());
                FinderCompassLogic.hasFeature = true;
            });
        } else {
            ServerLifecycleHooks.getCurrentServer().submitAsync(() -> {
                ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(packet.username());
                if (p != null) {
                    BlockPos result = FinderCompassMod.instance.findLevelStructure((ServerLevel) p.level(), p.getOnPos(), packet.featureId());
                    FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}", packet.featureId(), packet.username(), result);
                    if (result != null) {
                        FeatureSearchPacket featureSearchPacket = new FeatureSearchPacket(result.getX(), result.getY(), result.getZ(), "server", packet.featureId());
                        PacketDistributor.sendToPlayer(p, featureSearchPacket);
                    }
                }
            });
        }
    }
}
