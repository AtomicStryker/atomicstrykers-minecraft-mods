package atomicstryker.multimine.common.network;

import atomicstryker.multimine.common.MultiMine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PartialBlockPacket(String user, int x, int y, int z, float value,
                                 boolean regenerating) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(MultiMine.MOD_ID, "partialblock");

    private static final int MAX_NAME_LENGTH = 256;

    public PartialBlockPacket(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readUtf(MAX_NAME_LENGTH), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readFloat(), packetBuffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(user, MAX_NAME_LENGTH);
        packetBuffer.writeInt(x);
        packetBuffer.writeInt(y);
        packetBuffer.writeInt(z);
        packetBuffer.writeFloat(value);
        packetBuffer.writeBoolean(regenerating);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
