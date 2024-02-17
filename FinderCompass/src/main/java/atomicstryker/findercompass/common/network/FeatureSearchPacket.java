package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FeatureSearchPacket(int x, int y, int z, String username,
                                  String featureId) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(FinderCompassMod.MOD_ID, "featuresearch");
    public static final int SEARCH_RADIUS = 160;
    private static final int MAX_STRING_LENGTH = 256;

    public FeatureSearchPacket(final FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(x);
        packetBuffer.writeInt(y);
        packetBuffer.writeInt(z);
        packetBuffer.writeUtf(username, MAX_STRING_LENGTH);
        packetBuffer.writeUtf(featureId, MAX_STRING_LENGTH);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
