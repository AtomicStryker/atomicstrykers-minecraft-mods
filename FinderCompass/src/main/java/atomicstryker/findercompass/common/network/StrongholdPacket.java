package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.client.FinderCompassLogic;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class StrongholdPacket implements IPacket {

    private int MAX_STRING_LENGTH = 256;
    private int STRONGHOLD_SEARCH_RADIUS = 160;
    private int x, y, z;
    private String username;

    public StrongholdPacket() {
    }

    /**
     *
     */
    public StrongholdPacket(String user, int a, int b, int c) {
        username = user;
        x = a;
        y = b;
        z = c;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        StrongholdPacket packet = (StrongholdPacket) msg;
        packetBuffer.writeString(packet.username, MAX_STRING_LENGTH);
        packetBuffer.writeInt(packet.x);
        packetBuffer.writeInt(packet.y);
        packetBuffer.writeInt(packet.z);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        return (MSG) new StrongholdPacket(packetBuffer.readString(MAX_STRING_LENGTH), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            StrongholdPacket packet = (StrongholdPacket) msg;
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> onClientReceivedResponse(packet));
            DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> onServerReceivedQuery(packet));

        });
        contextSupplier.get().setPacketHandled(true);
    }

    private void onServerReceivedQuery(StrongholdPacket packet) {

        ServerLifecycleHooks.getCurrentServer().addScheduledTask(() -> {
            EntityPlayerMP p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(packet.username);
            if (p != null) {
                BlockPos result = ((WorldServer) p.world).getChunkProvider().getChunkGenerator().findNearestStructure(p.world, "Stronghold", new BlockPos(p), STRONGHOLD_SEARCH_RADIUS, false);
                if (result != null) {
                    FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new StrongholdPacket(packet.username, result.getX(), result.getY(), result.getZ()), p);
                }
            }
        });
    }

    private void onClientReceivedResponse(StrongholdPacket packet) {
        Minecraft.getInstance().addScheduledTask(() -> {
            FinderCompassLogic.strongholdCoords = new BlockPos(packet.x, packet.y, packet.z);
            FinderCompassMod.LOGGER.info("Finder Compass server sent Stronghold coords: [{}|{}|{}]", packet.x, packet.y, packet.z);
            FinderCompassLogic.hasStronghold = true;
        });
    }

}
