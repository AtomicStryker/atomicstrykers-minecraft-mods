package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record AirPacket(int air) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(InfernalMobsCore.MOD_ID, "airchoke");

    public AirPacket(final FriendlyByteBuf buffer) {
        this(buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(air);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
