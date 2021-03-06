package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.GsonConfig;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class HandshakePacket implements IPacket {

    private int MAX_STRING_LENGTH = Integer.MAX_VALUE - 1;

    private String username;
    private String json;

    public HandshakePacket() {
    }

    public HandshakePacket(String user, String json) {
        username = user;
        this.json = json;
    }

    public String getUsername() {
        return username;
    }

    public String getJson() {
        return json;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        HandshakePacket packet = (HandshakePacket) msg;
        packetBuffer.writeString(packet.username, MAX_STRING_LENGTH);
        if (packet.username.equals("server")) {
            packet.json = GsonConfig.jsonFromConfig(FinderCompassMod.instance.compassConfig);
            packetBuffer.writeString(packet.json, MAX_STRING_LENGTH);
        }
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        HandshakePacket packet = new HandshakePacket(packetBuffer.readString(MAX_STRING_LENGTH), packetBuffer.readString(MAX_STRING_LENGTH));
        return (MSG) packet;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        HandshakePacket handShakePacket = (HandshakePacket) msg;
        FinderCompassMod.proxy.onReceivedHandshakePacket(handShakePacket);
        contextSupplier.get().setPacketHandled(true);
    }
}
