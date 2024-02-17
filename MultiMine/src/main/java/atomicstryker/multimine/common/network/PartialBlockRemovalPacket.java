package atomicstryker.multimine.common.network;

import atomicstryker.multimine.common.MultiMine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PartialBlockRemovalPacket(int x, int y, int z) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(MultiMine.MOD_ID, "partialblockremoval");

    public PartialBlockRemovalPacket(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(x);
        packetBuffer.writeInt(y);
        packetBuffer.writeInt(z);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
