package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HealthPacket(String stringData, int entID, float health, float maxhealth) implements CustomPacketPayload {

    public static final Type<HealthPacket> TYPE = new Type<>(new ResourceLocation(InfernalMobsCore.MOD_ID, "health"));

    public static final StreamCodec<ByteBuf, HealthPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            HealthPacket::stringData,
            ByteBufCodecs.INT,
            HealthPacket::entID,
            ByteBufCodecs.FLOAT,
            HealthPacket::health,
            ByteBufCodecs.FLOAT,
            HealthPacket::maxhealth,
            HealthPacket::new
    );

    @Override
    public Type<HealthPacket> type() {
        return TYPE;
    }
}
