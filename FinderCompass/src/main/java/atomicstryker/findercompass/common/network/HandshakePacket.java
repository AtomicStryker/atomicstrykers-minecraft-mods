package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.GsonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record HandshakePacket(String username, String json) {

    private static int MAX_NAME_LENGTH = 256;
    private static int MAX_STRING_LENGTH_JSON = 100000;

    public String getUsername() {
        return username;
    }

    public String getJson() {
        return json;
    }

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(this.username, MAX_NAME_LENGTH);
        if (this.username.equals("server")) {
            String config = GsonConfig.jsonFromConfig(FinderCompassMod.instance.compassConfig);
            packetBuffer.writeUtf(config, MAX_STRING_LENGTH_JSON);
        }
    }

    public static HandshakePacket decode(FriendlyByteBuf packetBuffer) {
        return new HandshakePacket(packetBuffer.readUtf(MAX_NAME_LENGTH), packetBuffer.readUtf(MAX_STRING_LENGTH_JSON));
    }

    public static void handle(HandshakePacket handShakePacket, CustomPayloadEvent.Context context) {
        FinderCompassMod.proxy.onReceivedHandshakePacket(handShakePacket);
        context.setPacketHandled(true);
    }
}
