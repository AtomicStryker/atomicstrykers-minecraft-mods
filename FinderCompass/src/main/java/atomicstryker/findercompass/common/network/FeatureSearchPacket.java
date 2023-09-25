package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record FeatureSearchPacket(int x, int y, int z, String username, String featureId) {

    public static final int SEARCH_RADIUS = 160;
    private static final int MAX_STRING_LENGTH = 256;

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(this.x);
        packetBuffer.writeInt(this.y);
        packetBuffer.writeInt(this.z);
        packetBuffer.writeUtf(this.username, MAX_STRING_LENGTH);
        packetBuffer.writeUtf(this.featureId, MAX_STRING_LENGTH);
    }

    public static FeatureSearchPacket decode(FriendlyByteBuf packetBuffer) {
        return new FeatureSearchPacket(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readUtf(MAX_STRING_LENGTH), packetBuffer.readUtf(MAX_STRING_LENGTH));
    }

    public static void handle(FeatureSearchPacket packet, CustomPayloadEvent.Context context) {
        FinderCompassMod.proxy.onReceivedSearchPacket(packet);
        context.setPacketHandled(true);
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
