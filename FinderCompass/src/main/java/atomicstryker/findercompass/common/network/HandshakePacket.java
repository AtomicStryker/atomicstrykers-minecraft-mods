package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HandshakePacket(String username, String json) implements CustomPacketPayload {

    public static final Type<HandshakePacket> TYPE = new Type<>(new ResourceLocation(FinderCompassMod.MOD_ID, "handshake"));

    public static final StreamCodec<ByteBuf, HandshakePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            HandshakePacket::username,
            ByteBufCodecs.STRING_UTF8,
            HandshakePacket::json,
            HandshakePacket::new);

    @Override
    public Type<HandshakePacket> type() {
        return TYPE;
    }
}
