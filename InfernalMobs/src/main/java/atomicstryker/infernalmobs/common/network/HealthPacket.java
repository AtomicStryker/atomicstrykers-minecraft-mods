package atomicstryker.infernalmobs.common.network;

import atomicstryker.infernalmobs.client.InfernalMobsClient;
import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.network.NetworkHelper.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class HealthPacket implements IPacket {

    private String stringData;
    private int entID;
    private float health;
    private float maxhealth;

    public HealthPacket() {
    }

    public HealthPacket(String u, int i, float entHealth, float entMaxHealth) {
        stringData = u;
        entID = i;
        health = entHealth;
        maxhealth = entMaxHealth;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        HealthPacket healthPacket = (HealthPacket) msg;
        packetBuffer.writeString(healthPacket.stringData);
        packetBuffer.writeInt(healthPacket.entID);
        packetBuffer.writeFloat(healthPacket.health);
        packetBuffer.writeFloat(healthPacket.maxhealth);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        HealthPacket result = new HealthPacket();
        result.stringData = packetBuffer.readString(32767);
        result.entID = packetBuffer.readInt();
        result.health = packetBuffer.readFloat();
        result.maxhealth = packetBuffer.readFloat();
        return (MSG) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            HealthPacket healthPacket = (HealthPacket) msg;
            if (healthPacket.maxhealth > 0) {
                Minecraft.getInstance().addScheduledTask(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> InfernalMobsClient.onHealthPacketForClient(healthPacket)));
            } else {
                EntityPlayerMP p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(healthPacket.stringData);
                if (p != null) {
                    Entity ent = p.world.getEntityByID(entID);
                    if (ent instanceof EntityLivingBase) {
                        EntityLivingBase e = (EntityLivingBase) ent;
                        MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                        if (mod != null) {
                            health = e.getHealth();
                            maxhealth = e.getMaxHealth();
                            InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new HealthPacket(stringData, entID, health, maxhealth), p);
                        }
                    }
                }
            }
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

    public float getMaxhealth() {
        return maxhealth;
    }
}
