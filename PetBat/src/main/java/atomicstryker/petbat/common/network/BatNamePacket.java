package atomicstryker.petbat.common.network;

import atomicstryker.petbat.common.PetBatMod;
import atomicstryker.petbat.common.network.NetworkHelper.IPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BatNamePacket implements IPacket {

    private String user, batName;

    public BatNamePacket() {
    }

    public BatNamePacket(String bdata, String idata) {
        user = bdata;
        batName = idata;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        BatNamePacket airPacket = (BatNamePacket) msg;
        packetBuffer.writeString(airPacket.user);
        packetBuffer.writeString(airPacket.batName);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        return (MSG) new BatNamePacket(packetBuffer.readString(), packetBuffer.readString());
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        BatNamePacket packet = (BatNamePacket) msg;

        PetBatMod.LOGGER.debug("BatNamePacket received, user {} batname {}", packet.user, packet.batName);
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        server.deferTask(() -> {
            ServerPlayerEntity p = server.getPlayerList().getPlayerByUsername(packet.user);
            if (p != null) {
                PetBatMod.LOGGER.debug("found player ent {}", p);
                if (p.getHeldItemMainhand().getItem() == PetBatMod.instance().itemPocketedBat) {
                    PetBatMod.LOGGER.debug("writing batname {} to itemstack {}", packet.batName, p.getHeldItemMainhand());
                    p.getHeldItemMainhand().setDisplayName(new TranslationTextComponent(packet.batName));
                }
            }
        });

        contextSupplier.get().setPacketHandled(true);
    }
}
