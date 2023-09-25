package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record VelocityPacket(float xv, float yv, float zv) {

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeFloat(this.xv);
        packetBuffer.writeFloat(this.yv);
        packetBuffer.writeFloat(this.zv);
    }

    public static VelocityPacket decode(FriendlyByteBuf packetBuffer) {
        return new VelocityPacket(packetBuffer.readFloat(), packetBuffer.readFloat(), packetBuffer.readFloat());
    }

    public static void handle(VelocityPacket velocityPacket, CustomPayloadEvent.Context context) {
        InfernalMobsClient.onVelocityPacket(velocityPacket.xv, velocityPacket.yv, velocityPacket.zv);
        context.setPacketHandled(true);
    }
}
