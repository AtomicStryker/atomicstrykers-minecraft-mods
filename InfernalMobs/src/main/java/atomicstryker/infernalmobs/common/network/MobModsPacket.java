package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MobModsPacket(String stringData, int entID, byte sentFromServer) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(InfernalMobsCore.MOD_ID, "mobmods");

    public MobModsPacket(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readUtf(32767), packetBuffer.readInt(), packetBuffer.readByte());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(stringData, 32767);
        packetBuffer.writeInt(entID);
        packetBuffer.writeByte(sentFromServer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
