package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.MultiMineServer;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class PartialBlockPacket implements IPacket {

    private int MAX_NAME_LENGTH = 256;

    private String user;
    private int x, y, z;
    private float value;
    private boolean regenerating;

    public PartialBlockPacket() {
    }

    public PartialBlockPacket(String username, int ix, int iy, int iz, float val, boolean regen) {
        user = username;
        x = ix;
        y = iy;
        z = iz;
        value = val;
        regenerating = regen;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        PartialBlockPacket packet = (PartialBlockPacket) msg;
        packetBuffer.writeUtf(packet.user, MAX_NAME_LENGTH);
        packetBuffer.writeInt(packet.x);
        packetBuffer.writeInt(packet.y);
        packetBuffer.writeInt(packet.z);
        packetBuffer.writeFloat(packet.value);
        packetBuffer.writeBoolean(packet.regenerating);
    }

    @Override
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        PartialBlockPacket packet = new PartialBlockPacket(packetBuffer.readUtf(MAX_NAME_LENGTH), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readFloat(), packetBuffer.readBoolean());
        return (MSG) packet;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        PartialBlockPacket packet = (PartialBlockPacket) msg;
        if (packet.user.equals("server")) {
            contextSupplier.get().enqueueWork(() -> MultiMineClient.instance().onServerSentPartialBlockData(packet.x, packet.y, packet.z, packet.value, packet.regenerating));
        } else {
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(packet.user);
            if (p != null) {
                contextSupplier.get().enqueueWork(() -> MultiMineServer.instance().onClientSentPartialBlockPacket(p, packet.x, packet.y, packet.z, packet.value));
            }
        }
        contextSupplier.get().setPacketHandled(true);
    }

}
