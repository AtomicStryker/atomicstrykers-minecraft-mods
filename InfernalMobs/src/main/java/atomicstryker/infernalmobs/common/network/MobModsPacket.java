package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class MobModsPacket implements IPacket {

    private String stringData;
    private int entID;
    private byte sentFromServer;

    public MobModsPacket() {
    }

    public MobModsPacket(String stringdata, int entIdToQuery, byte serverByte) {
        stringData = stringdata;
        entID = entIdToQuery;
        sentFromServer = serverByte;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        MobModsPacket mobModsPacket = (MobModsPacket) msg;
        packetBuffer.writeByte(mobModsPacket.sentFromServer);
        packetBuffer.writeInt(mobModsPacket.entID);
        packetBuffer.writeString(mobModsPacket.stringData, 32767);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        MobModsPacket mobModsPacket = new MobModsPacket();
        mobModsPacket.sentFromServer = packetBuffer.readByte();
        mobModsPacket.entID = packetBuffer.readInt();
        mobModsPacket.stringData = packetBuffer.readString(32767);
        return (MSG) mobModsPacket;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        MobModsPacket mobModsPacket = (MobModsPacket) msg;
        if (mobModsPacket.sentFromServer != 0) {
            // so we are on client now
            InfernalMobsClient.onMobModsPacketToClient(mobModsPacket.stringData, mobModsPacket.entID);
            InfernalMobsCore.LOGGER.debug("client received serverside mods {} for ent-ID {}", mobModsPacket.stringData, mobModsPacket.entID);
        } else {
            // else we are on serverside
            PlayerEntity p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(mobModsPacket.stringData);
            InfernalMobsCore.LOGGER.debug("player {} from string {} querying server for mods of entity id {}", p, mobModsPacket.stringData, mobModsPacket.entID);
            if (p != null) {
                Entity ent = p.world.getEntityByID(mobModsPacket.entID);
                if (ent instanceof LivingEntity) {
                    LivingEntity e = (LivingEntity) ent;
                    MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                    InfernalMobsCore.LOGGER.debug("resolves to entity {} modifiers {}", ent, mod);
                    if (mod != null) {
                        mobModsPacket.stringData = mod.getLinkedModNameUntranslated();
                        InfernalMobsCore.LOGGER.debug("server sending mods {} for ent-ID {}", mobModsPacket.stringData, mobModsPacket.entID);
                        InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new MobModsPacket(mobModsPacket.stringData, mobModsPacket.entID, (byte) 1), (ServerPlayerEntity) p);
                        InfernalMobsCore.instance().sendHealthPacket(e);
                    }
                }
            }
        }
        contextSupplier.get().setPacketHandled(true);
    }
}
