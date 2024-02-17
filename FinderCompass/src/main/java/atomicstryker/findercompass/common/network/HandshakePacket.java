package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.GsonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record HandshakePacket(String username, String json) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(FinderCompassMod.MOD_ID, "handshake");

    private static final int MAX_NAME_LENGTH = 256;
    private static final int MAX_STRING_LENGTH_JSON = 100000;

    public HandshakePacket(final FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(username, MAX_NAME_LENGTH);
        String jsonResponse = "";
        if (username.equals("server")) {
            jsonResponse = GsonConfig.jsonFromConfig(FinderCompassMod.instance.compassConfig);
        }
        packetBuffer.writeUtf(jsonResponse, MAX_STRING_LENGTH_JSON);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
