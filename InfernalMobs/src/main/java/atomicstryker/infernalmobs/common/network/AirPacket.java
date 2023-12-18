package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.OverlayChoking;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;


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
    public void handle(Object msg, CustomPayloadEvent.Context context) {
        AirPacket airPacket = (AirPacket) msg;
        // this method is async and safe to call off-thread
        OverlayChoking.onAirPacket(airPacket.air);
        context.setPacketHandled(true);
    }
}
