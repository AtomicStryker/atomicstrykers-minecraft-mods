package atomicstryker.multimine.common.network;

import atomicstryker.multimine.client.MultiMineClient;
import atomicstryker.multimine.common.MultiMineServer;
import atomicstryker.multimine.common.network.NetworkHelper.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class PartialBlockPacket implements IPacket {

    private int MAX_STRING_LENGTH = 256;

    private String user;
    private int x, y, z;
    private float value;
    private boolean regenerating;

    public PartialBlockPacket() {
    }

    public PartialBlockPacket(String username, int ix, int iy, int iz, float val, boolean regen) {
        user = username;
        x = ix;
        y = iy;
        z = iz;
        value = val;
        regenerating = regen;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        PartialBlockPacket packet = (PartialBlockPacket) msg;
        packetBuffer.writeString(packet.user, MAX_STRING_LENGTH);
        packetBuffer.writeInt(packet.x);
        packetBuffer.writeInt(packet.y);
        packetBuffer.writeInt(packet.z);
        packetBuffer.writeFloat(value);
        packetBuffer.writeBoolean(regenerating);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        return (MSG) new PartialBlockPacket(packetBuffer.readString(MAX_STRING_LENGTH), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readFloat(), packetBuffer.readBoolean());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            PartialBlockPacket packet = (PartialBlockPacket) msg;
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> onClientReceived(packet));
            DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> onServerReceivedQuery(packet));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private void onClientReceived(PartialBlockPacket packet) {
        Minecraft.getInstance().addScheduledTask(() -> MultiMineClient.instance().onServerSentPartialBlockData(packet.x, packet.y, packet.z, packet.value, packet.regenerating));
    }

    private void onServerReceivedQuery(PartialBlockPacket packet) {
        ServerLifecycleHooks.getCurrentServer().addScheduledTask(() -> {
            EntityPlayerMP player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(packet.user);
            if (player != null) {
                MultiMineServer.instance().onClientSentPartialBlockPacket(player, packet.x, packet.y, packet.z, packet.value);
            }
        });
    }
}
