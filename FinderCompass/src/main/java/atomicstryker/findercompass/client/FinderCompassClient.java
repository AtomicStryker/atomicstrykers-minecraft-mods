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
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;

public class FinderCompassClient implements ISidedProxy {

    /**
     * constructor with event bus registering of ourselves
     */
    public FinderCompassClient() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void renderHandHook(RenderHandEvent event) {
        CompassRenderHook.renderItemInHandHook(event.getItemStack());
    }

    @Override
    public void commonSetup() {
        FinderCompassClientTicker.instance = new FinderCompassClientTicker();
        FinderCompassClientTicker.instance.onLoad();
        FinderCompassClientTicker.instance.switchSetting();
    }

    @Override
    public File getMcFolder() {
        return Minecraft.getInstance().gameDir;
    }

    @Override
    public void onReceivedHandshakePacket(HandshakePacket handShakePacket) {
        FinderCompassMod.instance.initIfNeeded();
        FinderCompassMod.LOGGER.info("client received Finder Compass HandshakePacket, from username: {}", handShakePacket.getUsername());
        if (handShakePacket.getUsername().equals("server")) {
            String json = handShakePacket.getJson();
            FinderCompassMod.LOGGER.info("deferring config override task with json of length {}", json.length());
            Minecraft.getInstance().deferTask(() -> {
                FinderCompassMod.LOGGER.info("executing deferred config override, FinderCompassClientTicker.instance is: {}", FinderCompassClientTicker.instance);
                FinderCompassClientTicker.instance.inputOverrideConfig(json);
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
