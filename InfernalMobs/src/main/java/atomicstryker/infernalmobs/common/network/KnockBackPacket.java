package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KnockBackPacket(float xv, float zv) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(InfernalMobsCore.MOD_ID, "knockback");

    public KnockBackPacket(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readFloat(), packetBuffer.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeFloat(xv);
        packetBuffer.writeFloat(zv);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
