package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class AirPacket implements IPacket {

    private int air;

    public AirPacket() {
    }

    public AirPacket(int a) {
        air = a;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        AirPacket airPacket = (AirPacket) msg;
        packetBuffer.writeInt(airPacket.air);
    }

    @Override
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        return (MSG) new AirPacket(packetBuffer.readInt());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        AirPacket airPacket = (AirPacket) msg;
        InfernalMobsClient.onAirPacket(airPacket.air);
        contextSupplier.get().setPacketHandled(true);
    }
}
