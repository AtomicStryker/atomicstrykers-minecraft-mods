package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class MobModsPacket implements IPacket {

    private String stringData;
    private int entID;

    public MobModsPacket() {
    }

    public MobModsPacket(String stringdata, int entIdToQuery) {
        stringData = stringdata;
        entID = entIdToQuery;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        MobModsPacket mobModsPacket = (MobModsPacket) msg;
        packetBuffer.writeInt(mobModsPacket.entID);
        packetBuffer.writeUtf(mobModsPacket.stringData, 32767);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        MobModsPacket mobModsPacket = new MobModsPacket();
        mobModsPacket.entID = packetBuffer.readInt();
        mobModsPacket.stringData = packetBuffer.readUtf(32767);
        return (MSG) mobModsPacket;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (!(msg instanceof MobModsPacket mobModsPacket)) {
            return;
        }
        var context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                // so we are on client now
                InfernalMobsClient.onMobModsPacketToClient(mobModsPacket.stringData, mobModsPacket.entID);
                InfernalMobsCore.getLogger().debug("client received serverside mods {} for ent-ID {}", mobModsPacket.stringData, mobModsPacket.entID);
            } else {
                // else we are on serverside
                ServerPlayer serverPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(mobModsPacket.stringData);
                InfernalMobsCore.getLogger().debug("player {} from string {} querying server for mods of entity id {}", serverPlayer, mobModsPacket.stringData, mobModsPacket.entID);
                if (serverPlayer != null) {
                    Entity entity = serverPlayer.level().getEntity(mobModsPacket.entID);
                    if (entity instanceof LivingEntity livingEntity) {
                        MobModifier mod = InfernalMobsCore.getMobModifiers(livingEntity);
                        InfernalMobsCore.getLogger().debug("resolves to entity {} modifiers {}", entity, mod);
                        if (mod != null) {
                            mobModsPacket.stringData = mod.getLinkedModNameUntranslated();
                            InfernalMobsCore.getLogger().debug("server sending mods {} for ent-ID {}", mobModsPacket.stringData, mobModsPacket.entID);
                            InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new MobModsPacket(mobModsPacket.stringData, mobModsPacket.entID), serverPlayer);
                            InfernalMobsCore.instance().sendHealthPacket(livingEntity);
                        }
                    }
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
