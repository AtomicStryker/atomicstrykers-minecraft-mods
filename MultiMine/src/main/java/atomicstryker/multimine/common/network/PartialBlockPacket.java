package atomicstryker.multimine.common.network;

import atomicstryker.multimine.common.MultiMine;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PartialBlockPacket(String user, int x, int y, int z, float value,
                                 boolean regenerating) implements CustomPacketPayload {

    public static final Type<PartialBlockPacket> TYPE = new Type<>(new ResourceLocation(MultiMine.MOD_ID, "partialblock"));

    public static final StreamCodec<ByteBuf, PartialBlockPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PartialBlockPacket::user,
            ByteBufCodecs.INT,
            PartialBlockPacket::x,
            ByteBufCodecs.INT,
            PartialBlockPacket::y,
            ByteBufCodecs.INT,
            PartialBlockPacket::z,
            ByteBufCodecs.FLOAT,
            PartialBlockPacket::value,
            ByteBufCodecs.BOOL,
            PartialBlockPacket::regenerating,
            PartialBlockPacket::new
    );

    @Override
    public Type<PartialBlockPacket> type() {
        return TYPE;
    }
}
