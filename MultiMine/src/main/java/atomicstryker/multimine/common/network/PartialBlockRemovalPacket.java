package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PartialBlockRemovalPacket implements IPacket {

    private BlockPos pos;

    public PartialBlockRemovalPacket() {
    }

    public PartialBlockRemovalPacket(BlockPos p) {
        pos = p;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        PartialBlockRemovalPacket packet = (PartialBlockRemovalPacket) msg;
        packetBuffer.writeInt(packet.pos.getX());
        packetBuffer.writeInt(packet.pos.getY());
        packetBuffer.writeInt(packet.pos.getZ());
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        PartialBlockRemovalPacket packet = new PartialBlockRemovalPacket(new BlockPos(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt()));
        return (MSG) packet;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        PartialBlockRemovalPacket packet = (PartialBlockRemovalPacket) msg;
        MultiMineClient.instance().onServerSentPartialBlockDeleteCommand(packet.pos);
        contextSupplier.get().setPacketHandled(true);
    }

}
