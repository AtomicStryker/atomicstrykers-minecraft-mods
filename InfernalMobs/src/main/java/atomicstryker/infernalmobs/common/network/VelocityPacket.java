package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VelocityPacket(float xv, float yv, float zv) implements CustomPacketPayload {

    public static final Type<VelocityPacket> TYPE = new Type<>(new ResourceLocation(InfernalMobsCore.MOD_ID, "velocity"));

    public static final StreamCodec<ByteBuf, VelocityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            VelocityPacket::xv,
            ByteBufCodecs.FLOAT,
            VelocityPacket::yv,
            ByteBufCodecs.FLOAT,
            VelocityPacket::zv,
            VelocityPacket::new
    );

    @Override
    public Type<VelocityPacket> type() {
        return TYPE;
    }
}
