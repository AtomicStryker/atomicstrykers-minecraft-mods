package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public record MobModsPacket(String stringData, int entID, byte sentFromServer) {

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(this.stringData, 32767);
        packetBuffer.writeInt(this.entID);
        packetBuffer.writeByte(this.sentFromServer);
    }

    public static MobModsPacket decode(FriendlyByteBuf packetBuffer) {
        return new MobModsPacket(packetBuffer.readUtf(32767), packetBuffer.readInt(), packetBuffer.readByte());
    }

    public static void handle(MobModsPacket mobModsPacket, CustomPayloadEvent.Context context) {
        if (mobModsPacket.sentFromServer != 0) {
            // so we are on client now
            InfernalMobsClient.onMobModsPacketToClient(mobModsPacket.stringData, mobModsPacket.entID);
            InfernalMobsCore.LOGGER.debug("client received serverside mods {} for ent-ID {}", mobModsPacket.stringData, mobModsPacket.entID);
        } else {
            // else we are on serverside
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(mobModsPacket.stringData);
            InfernalMobsCore.LOGGER.debug("player {} from string {} querying server for mods of entity id {}", p, mobModsPacket.stringData, mobModsPacket.entID);
            if (p != null) {
                Entity ent = p.level().getEntity(mobModsPacket.entID);
                if (ent instanceof LivingEntity) {
                    LivingEntity e = (LivingEntity) ent;
                    MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                    InfernalMobsCore.LOGGER.debug("resolves to entity {} modifiers {}", ent, mod);
                    if (mod != null) {
                        MobModsPacket response = new MobModsPacket(mod.getLinkedModNameUntranslated(), mobModsPacket.entID, (byte) 1);
                        InfernalMobsCore.LOGGER.debug("server sending mods {} for ent-ID {}", response.stringData, response.entID);

                        InfernalMobsCore.networkChannel.send(response, PacketDistributor.PLAYER.with(p));
                        InfernalMobsCore.instance().sendHealthPacket(e);
                    }
                }
            }
        }
        context.setPacketHandled(true);
    }
}
