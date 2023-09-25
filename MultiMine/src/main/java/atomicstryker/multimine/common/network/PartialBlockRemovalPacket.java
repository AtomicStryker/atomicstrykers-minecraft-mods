package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record PartialBlockRemovalPacket(BlockPos pos) {

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(this.pos.getX());
        packetBuffer.writeInt(this.pos.getY());
        packetBuffer.writeInt(this.pos.getZ());
    }

    public static PartialBlockRemovalPacket decode(FriendlyByteBuf packetBuffer) {
        return new PartialBlockRemovalPacket(new BlockPos(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt()));
    }

    public static void handle(PartialBlockRemovalPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> context.enqueueWork(() -> MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(packet.pos)));
        context.setPacketHandled(true);
    }

}
