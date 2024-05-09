package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KnockBackPacket(float xv, float zv) implements CustomPacketPayload {

    public static final Type<KnockBackPacket> TYPE = new Type<>(new ResourceLocation(InfernalMobsCore.MOD_ID, "knockback"));

    public static final StreamCodec<ByteBuf, KnockBackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            KnockBackPacket::xv,
            ByteBufCodecs.FLOAT,
            KnockBackPacket::zv,
            KnockBackPacket::new
    );

    @Override
    public Type<KnockBackPacket> type() {
        return TYPE;
    }
}
