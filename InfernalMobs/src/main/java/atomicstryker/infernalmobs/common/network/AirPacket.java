package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AirPacket implements IPacket {

    private int air;

    public AirPacket() {
    }

    public AirPacket(int a) {
        air = a;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        AirPacket airPacket = (AirPacket) msg;
        packetBuffer.writeInt(airPacket.air);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        return (MSG) new AirPacket(packetBuffer.readInt());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            AirPacket airPacket = (AirPacket) msg;
            Minecraft.getInstance().addScheduledTask(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> InfernalMobsClient.overrideAir(airPacket.air)));
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
