package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record AirPacket(int air) implements CustomPacketPayload {

    public static final Type<AirPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(InfernalMobsCore.MOD_ID, "airchoke"));

    public static final StreamCodec<ByteBuf, AirPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AirPacket::air,
            AirPacket::new
    );

    @Override
    public Type<AirPacket> type() {
        return TYPE;
    }
}
