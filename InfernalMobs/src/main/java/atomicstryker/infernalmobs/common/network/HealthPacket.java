package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HealthPacket(String stringData, int entID, float health, float maxhealth) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(InfernalMobsCore.MOD_ID, "health");

//    @Override
//    public void handle(Object msg, CustomPayloadEvent.Context context) {
//        LogicalSidedProvider.WORKQUEUE.get(context.getDirection().getReceptionSide()).submit(() -> {
//            HealthPacket healthPacket = (HealthPacket) msg;
//            if (healthPacket.maxhealth > 0) {
//                InfernalMobsClient.onHealthPacketForClient(healthPacket.entID, healthPacket.health, healthPacket.maxhealth);
//            } else {

//            }
//        });
//        context.setPacketHandled(true);
//    }

    public HealthPacket(final FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readUtf(32767), packetBuffer.readInt(), packetBuffer.readFloat(), packetBuffer.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(stringData, 32767);
        packetBuffer.writeInt(entID);
        packetBuffer.writeFloat(health);
        packetBuffer.writeFloat(maxhealth);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
