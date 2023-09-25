package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.OverlayChoking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;


public record AirPacket(int air) {

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(this.air);
    }

    public static AirPacket decode(FriendlyByteBuf packetBuffer) {
        return new AirPacket(packetBuffer.readInt());
    }

    public static void handle(AirPacket airPacket, CustomPayloadEvent.Context context) {
        OverlayChoking.onAirPacket(airPacket.air);
        context.setPacketHandled(true);
    }
}
