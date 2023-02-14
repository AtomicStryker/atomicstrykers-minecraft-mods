package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        PartialBlockRemovalPacket packet = (PartialBlockRemovalPacket) msg;
        // synchronize with MC world thread...
        contextSupplier.get().enqueueWork(() -> {
            MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(packet.pos);
        });
        contextSupplier.get().setPacketHandled(true);
    }

}
