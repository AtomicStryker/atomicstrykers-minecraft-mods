package atomicstryker.petbat.common.network;

import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.NetworkHelper.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BatNamePacket implements IPacket {

    private String user, batName;

    public BatNamePacket() {
    }

    public BatNamePacket(String bdata, String idata) {
        user = bdata;
        batName = idata;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        BatNamePacket airPacket = (BatNamePacket) msg;
        packetBuffer.writeString(airPacket.user);
        packetBuffer.writeString(airPacket.batName);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        return (MSG) new BatNamePacket(packetBuffer.readString(), packetBuffer.readString());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        BatNamePacket airPacket = (BatNamePacket) msg;
        PetBatMod.proxy.onBatNamePacket(airPacket);
        contextSupplier.get().setPacketHandled(true);
    }

    public String getUser() {
        return user;
    }

    public String getBatName() {
        return user;
    }
}
