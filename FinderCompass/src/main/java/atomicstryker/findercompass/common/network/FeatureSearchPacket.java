package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FeatureSearchPacket implements IPacket {

    public static final int SEARCH_RADIUS = 160;
    private int MAX_STRING_LENGTH = 256;
    private int x, y, z;
    private String username;
    private String featureId;

    public FeatureSearchPacket() {
    }

    /**
     *
     */
    public FeatureSearchPacket(String user, String id, int a, int b, int c) {
        username = user;
        featureId = id;
        x = a;
        y = b;
        z = c;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        FeatureSearchPacket packet = (FeatureSearchPacket) msg;
        packetBuffer.writeString(packet.username, MAX_STRING_LENGTH);
        packetBuffer.writeString(packet.featureId, MAX_STRING_LENGTH);
        packetBuffer.writeInt(packet.x);
        packetBuffer.writeInt(packet.y);
        packetBuffer.writeInt(packet.z);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        return (MSG) new FeatureSearchPacket(packetBuffer.readString(MAX_STRING_LENGTH), packetBuffer.readString(MAX_STRING_LENGTH), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        FeatureSearchPacket packet = (FeatureSearchPacket) msg;
        FinderCompassMod.proxy.onReceivedSearchPacket(packet);
        contextSupplier.get().setPacketHandled(true);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getUsername() {
        return username;
    }

    public String getFeatureId() {
        return featureId;
    }
}
