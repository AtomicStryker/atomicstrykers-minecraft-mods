package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class KnockBackPacket implements IPacket {

    private float xv, zv;

    public KnockBackPacket() {
    }

    public KnockBackPacket(float x, float z) {
        xv = x;
        zv = z;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        KnockBackPacket knockBackPacket = (KnockBackPacket) msg;
        packetBuffer.writeFloat(knockBackPacket.xv);
        packetBuffer.writeFloat(knockBackPacket.zv);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        KnockBackPacket knockBackPacket = new KnockBackPacket();
        knockBackPacket.xv = packetBuffer.readFloat();
        knockBackPacket.zv = packetBuffer.readFloat();
        return (MSG) knockBackPacket;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        KnockBackPacket knockBackPacket = (KnockBackPacket) msg;
        InfernalMobsClient.onKnockBackPacket(knockBackPacket.xv, knockBackPacket.zv);
        contextSupplier.get().setPacketHandled(true);
    }
}
