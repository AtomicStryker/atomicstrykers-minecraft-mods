package atomicstryker.findercompass.client;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.ISidedProxy;
import atomicstryker.findercompass.common.network.FeatureSearchPacket;
import atomicstryker.findercompass.common.network.HandshakePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Path;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FinderCompassMod.MOD_ID)
public class FinderCompassClient implements ISidedProxy {

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

    @Override
    public void onReceivedHandshakePacket(HandshakePacket handShakePacket) {
        FinderCompassMod.instance.initIfNeeded();
        FinderCompassMod.LOGGER.info("client received Finder Compass HandshakePacket, from username: {}",
                handShakePacket.getUsername());
        if (handShakePacket.getUsername().equals("server")) {
            String json = handShakePacket.getJson();
            FinderCompassMod.LOGGER.info("deferring config override task with json of length {}", json.length());
            Minecraft.getInstance().submitAsync(() -> {
                FinderCompassMod.LOGGER.info("executing deferred config override, FinderCompassClientTicker.instance is: {}", FinderCompassClientTicker.instance);
                FinderCompassClientTicker.instance.inputOverrideConfig(json);
            });
        }
    }

    @Override
    public void onReceivedSearchPacket(FeatureSearchPacket packet) {
        if (packet.getUsername().equals("server")) {
            Minecraft.getInstance().submitAsync(() -> {
                FinderCompassLogic.featureCoords = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                FinderCompassMod.LOGGER.debug("Finder Compass server sent Feature {} coords: [{}|{}|{}]",
                        packet.getFeatureId(), packet.getX(), packet.getY(), packet.getZ());
                FinderCompassLogic.hasFeature = true;
            });
        } else {
            ServerLifecycleHooks.getCurrentServer().submitAsync(() -> {
                ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().
                        getPlayerByName(packet.getUsername());
                if (p != null) {
                    BlockPos result = FinderCompassMod.instance.findLevelStructure((ServerLevel) p.level(),
                            p.getOnPos(), packet.getFeatureId());
                    FinderCompassMod.LOGGER.debug("server searched for feature {} for user {}, result {}",
                            packet.getFeatureId(), packet.getUsername(), result);
                    if (result != null) {
                        FinderCompassMod.networkChannel.send(new FeatureSearchPacket(result.getX(), result.getY(),
                                        result.getZ(), "server", packet.getFeatureId()),
                                PacketDistributor.PLAYER.with(p));
                    }
                }
            });
        }
    }
}
