package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PartialBlockRemovalPacket implements IPacket {

    private BlockPos pos;

    public PartialBlockRemovalPacket() {
    }

    public PartialBlockRemovalPacket(BlockPos p) {
        pos = p;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        PartialBlockRemovalPacket packet = (PartialBlockRemovalPacket) msg;
        packetBuffer.writeInt(packet.pos.getX());
        packetBuffer.writeInt(packet.pos.getY());
        packetBuffer.writeInt(packet.pos.getZ());
    }

    @Override
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        PartialBlockRemovalPacket packet = new PartialBlockRemovalPacket(new BlockPos(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt()));
        return (MSG) packet;
    }

    @Override
    public void handle(Object msg, CustomPayloadEvent.Context context) {
        PartialBlockRemovalPacket packet = (PartialBlockRemovalPacket) msg;
        LogicalSidedProvider.WORKQUEUE.get(context.getDirection().getReceptionSide()).submit(() -> MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(packet.pos));
        context.setPacketHandled(true);
    }

}
