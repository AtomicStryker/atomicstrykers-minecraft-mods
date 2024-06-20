package atomicstryker.multimine.common.network;

import atomicstryker.multimine.common.MultiMine;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PartialBlockRemovalPacket(int x, int y, int z) implements CustomPacketPayload {

    public static final Type<PartialBlockRemovalPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MultiMine.MOD_ID, "partialblockremoval"));

    public static final StreamCodec<ByteBuf, PartialBlockRemovalPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PartialBlockRemovalPacket::x,
            ByteBufCodecs.INT,
            PartialBlockRemovalPacket::y,
            ByteBufCodecs.INT,
            PartialBlockRemovalPacket::z,
            PartialBlockRemovalPacket::new);

    @Override
    public Type<PartialBlockRemovalPacket> type() {
        return TYPE;
    }
}
