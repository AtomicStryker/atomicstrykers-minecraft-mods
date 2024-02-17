package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VelocityPacket(float xv, float yv, float zv) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(InfernalMobsCore.MOD_ID, "velocity");

    public VelocityPacket(FriendlyByteBuf packetBuffer) {
        this(packetBuffer.readFloat(), packetBuffer.readFloat(), packetBuffer.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeFloat(xv);
        packetBuffer.writeFloat(yv);
        packetBuffer.writeFloat(zv);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
