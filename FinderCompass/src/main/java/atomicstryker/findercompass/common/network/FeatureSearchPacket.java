package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FeatureSearchPacket(int x, int y, int z, String username,
                                  String featureId) implements CustomPacketPayload {

    public static final Type<FeatureSearchPacket> TYPE = new Type<>(new ResourceLocation(FinderCompassMod.MOD_ID, "featuresearch"));
    public static final int SEARCH_RADIUS = 160;

    public static final StreamCodec<ByteBuf, FeatureSearchPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            FeatureSearchPacket::x,
            ByteBufCodecs.INT,
            FeatureSearchPacket::y,
            ByteBufCodecs.INT,
            FeatureSearchPacket::z,
            ByteBufCodecs.STRING_UTF8,
            FeatureSearchPacket::username,
            ByteBufCodecs.STRING_UTF8,
            FeatureSearchPacket::featureId,
            FeatureSearchPacket::new
    );

    @Override
    public Type<FeatureSearchPacket> type() {
        return TYPE;
    }
}
