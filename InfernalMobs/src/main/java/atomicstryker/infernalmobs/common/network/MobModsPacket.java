package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MobModsPacket(String stringData, int entID, byte sentFromServer) implements CustomPacketPayload {

    public static final Type<MobModsPacket> TYPE = new Type<>(new ResourceLocation(InfernalMobsCore.MOD_ID, "mobmods"));

    public static final StreamCodec<ByteBuf, MobModsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MobModsPacket::stringData,
            ByteBufCodecs.INT,
            MobModsPacket::entID,
            ByteBufCodecs.BYTE,
            MobModsPacket::sentFromServer,
            MobModsPacket::new
    );

    @Override
    public Type<MobModsPacket> type() {
        return TYPE;
    }
}
