package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;


public record KnockBackPacket(float xv, float zv) {

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeFloat(this.xv);
        packetBuffer.writeFloat(this.zv);
    }

    public static KnockBackPacket decode(FriendlyByteBuf packetBuffer) {
        return new KnockBackPacket(packetBuffer.readFloat(), packetBuffer.readFloat());
    }

    public static void handle(KnockBackPacket knockBackPacket, CustomPayloadEvent.Context context) {
        InfernalMobsClient.onKnockBackPacket(knockBackPacket.xv, knockBackPacket.zv);
        context.setPacketHandled(true);
    }
}
