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

public record HealthPacket(String stringData, int entID, float health, float maxhealth) {

    public void encode(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(this.stringData);
        packetBuffer.writeInt(this.entID);
        packetBuffer.writeFloat(this.health);
        packetBuffer.writeFloat(this.maxhealth);
    }

    public static HealthPacket decode(FriendlyByteBuf packetBuffer) {
        return new HealthPacket(packetBuffer.readUtf(32767), packetBuffer.readInt(), packetBuffer.readFloat(), packetBuffer.readFloat());
    }

    public static void handle(HealthPacket healthPacket, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (healthPacket.maxhealth > 0) {
                InfernalMobsClient.onHealthPacketForClient(healthPacket.entID, healthPacket.health, healthPacket.maxhealth);
            } else {
                ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(healthPacket.stringData);
                if (p != null) {
                    Entity ent = p.level().getEntity(healthPacket.entID);
                    if (ent instanceof LivingEntity) {
                        LivingEntity e = (LivingEntity) ent;
                        MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                        if (mod != null) {
                            HealthPacket response = new HealthPacket(healthPacket.stringData, healthPacket.entID(), e.getHealth(), e.getMaxHealth());
                            InfernalMobsCore.networkChannel.send(response, PacketDistributor.PLAYER.with(p));
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    public String getStringData() {
        return stringData;
    }

    public int getEntID() {
        return entID;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxhealth() {
        return maxhealth;
    }
}
