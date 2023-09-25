package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.MultiMineServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public record PartialBlockPacket(String user, int x, int y, int z, float value, boolean regenerating) {

    private final static int MAX_NAME_LENGTH = 256;

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(this.user, MAX_NAME_LENGTH);
        packetBuffer.writeInt(this.x);
        packetBuffer.writeInt(this.y);
        packetBuffer.writeInt(this.z);
        packetBuffer.writeFloat(this.value);
        packetBuffer.writeBoolean(this.regenerating);
    }

    public static PartialBlockPacket decode(FriendlyByteBuf packetBuffer) {
        return new PartialBlockPacket(packetBuffer.readUtf(MAX_NAME_LENGTH), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readFloat(), packetBuffer.readBoolean());
    }

    public static void handle(PartialBlockPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (packet.user.equals("server")) {
                context.enqueueWork(() -> MultiMineClient.instance().onServerSentPartialBlockData(packet.x, packet.y, packet.z, packet.value, packet.regenerating));
            } else {
                ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(packet.user);
                if (p != null) {
                    context.enqueueWork(() -> MultiMineServer.instance().onClientSentPartialBlockPacket(p, packet.x, packet.y, packet.z, packet.value));
                }
            }
        });
        context.setPacketHandled(true);
    }

}
