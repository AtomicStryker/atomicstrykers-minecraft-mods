package atomicstryker.infernalmobs.common.network;

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

    public MobModsPacket(String str, int i, byte ir) {
        stringData = str;
        entID = i;
        sentFromServer = ir;
    }

    @Override
    public void encode(Object msg, PacketBuffer packetBuffer) {
        MobModsPacket mobModsPacket = (MobModsPacket) msg;
        packetBuffer.writeByte(mobModsPacket.sentFromServer);
        packetBuffer.writeShort(mobModsPacket.stringData.length());
        for (char c : mobModsPacket.stringData.toCharArray()) {
            packetBuffer.writeChar(c);
        }
        packetBuffer.writeInt(entID);
    }

    @Override
    public <MSG> MSG decode(PacketBuffer packetBuffer) {
        MobModsPacket mobModsPacket = new MobModsPacket();
        mobModsPacket.sentFromServer = packetBuffer.readByte();
        short len = packetBuffer.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = packetBuffer.readChar();
        }
        mobModsPacket.stringData = String.valueOf(chars);
        mobModsPacket.entID = packetBuffer.readInt();
        return (MSG) mobModsPacket;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        MobModsPacket mobModsPacket = (MobModsPacket) msg;
        if (sentFromServer != 0) {
            // so we are on client now
            InfernalMobsCore.proxy.onMobModsPacketToClient(stringData, entID);
        } else {
            // else we are on serverside
            PlayerEntity p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(stringData);
            if (p != null) {
                Entity ent = p.world.getEntityByID(entID);
                if (ent instanceof LivingEntity) {
                    LivingEntity e = (LivingEntity) ent;
                    MobModifier mod = InfernalMobsCore.getMobModifiers(e);
                    if (mod != null) {
                        stringData = mod.getLinkedModNameUntranslated();
                        InfernalMobsCore.instance().networkHelper.sendPacketToPlayer(new MobModsPacket(stringData, entID, (byte) 1), (ServerPlayerEntity) p);
                        InfernalMobsCore.instance().sendHealthPacket(e);
                    }
                }
            }
        }
        contextSupplier.get().setPacketHandled(true);
    }
}
