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

public class HealthPacket implements IPacket {

    private String stringData;
    private int entID;
    private float health;
    private float maxHealth;

    public HealthPacket() {
    }

    public HealthPacket(String u, int i, float entHealth, float entMaxHealth) {
        stringData = u;
        entID = i;
        health = entHealth;
        maxHealth = entMaxHealth;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        HealthPacket healthPacket = (HealthPacket) msg;
        packetBuffer.writeUtf(healthPacket.stringData);
        packetBuffer.writeInt(healthPacket.entID);
        packetBuffer.writeFloat(healthPacket.health);
        packetBuffer.writeFloat(healthPacket.maxHealth);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <MSG> MSG decode(FriendlyByteBuf packetBuffer) {
        HealthPacket result = new HealthPacket();
        result.stringData = packetBuffer.readUtf(32767);
        result.entID = packetBuffer.readInt();
        result.health = packetBuffer.readFloat();
        result.maxHealth = packetBuffer.readFloat();
        return (MSG) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            // make sure health packet is health packet
            if (!(msg instanceof HealthPacket healthPacket)) {
                return;
            }

            var context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getDirection().getReceptionSide().isClient()) {
                    // handel by client
                    InfernalMobsClient.onHealthPacketForClient(healthPacket.entID, healthPacket.health, healthPacket.maxHealth);
                } else {
                    // handel by server
                    ServerPlayer serverPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(healthPacket.stringData);
                    if (serverPlayer != null) {
                        Entity entity = serverPlayer.level().getEntity(healthPacket.entID);
                        if (entity instanceof LivingEntity livingEntity) {
                            MobModifier mod = InfernalMobsCore.getMobModifiers(livingEntity);
                            if (mod != null) {
                                stringData = healthPacket.stringData;
                                entID = healthPacket.entID;
                                health = livingEntity.getHealth();
                                maxHealth = livingEntity.getMaxHealth();
                                InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new HealthPacket(stringData, entID, health, maxHealth), serverPlayer);
                            }
                        }
                    }
                }
            });
        });
        contextSupplier.get().setPacketHandled(true);
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

    public float getMaxHealth() {
        return maxHealth;
    }
}
