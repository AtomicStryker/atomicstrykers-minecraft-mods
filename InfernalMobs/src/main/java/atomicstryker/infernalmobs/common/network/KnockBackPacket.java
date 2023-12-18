package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class KnockBackPacket implements IPacket {

    private float xv, zv;

    public KnockBackPacket() {
    }

    public KnockBackPacket(float x, float z) {
        xv = x;
        zv = z;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        KnockBackPacket knockBackPacket = (KnockBackPacket) msg;
        packetBuffer.writeFloat(knockBackPacket.xv);
        packetBuffer.writeFloat(knockBackPacket.zv);
    }

    @Override
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        KnockBackPacket knockBackPacket = new KnockBackPacket();
        knockBackPacket.xv = packetBuffer.readFloat();
        knockBackPacket.zv = packetBuffer.readFloat();
        return (MSG) knockBackPacket;
    }

    @Override
    public void handle(Object msg, CustomPayloadEvent.Context context) {
        KnockBackPacket knockBackPacket = (KnockBackPacket) msg;
        // thread synchronization happens later
        InfernalMobsClient.onKnockBackPacket(knockBackPacket.xv, knockBackPacket.zv);
        context.setPacketHandled(true);
    }
}
