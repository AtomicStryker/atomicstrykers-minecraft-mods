package atomicstryker.findercompass.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

/**
 * Helper class to wrap the 1.13 channels and packets into something
 * resembling a much older packet system. Create one instance of this for a Mod,
 * then use the helper methods to send Packets. Packet Handling is done inside
 * the packet classes themselves.
 *
 * @author AtomicStryker
 */
public class NetworkHelper {

    private final SimpleChannel packetChannel;

    private final HashSet<Class<? extends IPacket>> registeredClasses;

    /**
     * Creates an instance of the NetworkHelper with included channels for client and server communication.
     * Automatically registers the necessary channels and discriminators for the supplied Packet classes.
     *
     * @param channelName          channel name to use, anything but already taken designations goes
     * @param handledPacketClasses provide the IPacket classes you want to use for communication here
     */
    @SafeVarargs
    public NetworkHelper(String channelName, Class<? extends IPacket>... handledPacketClasses) {

        packetChannel = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(channelName)).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> "1.0.0")
                .simpleChannel();
        registeredClasses = new HashSet<>(handledPacketClasses.length);
        registeredClasses.addAll(Arrays.asList(handledPacketClasses));

        int runningIndex = 0;
        for (Class<? extends IPacket> packetClass : handledPacketClasses) {
            try {
                IPacket instance = packetClass.newInstance();
                packetChannel.registerMessage(runningIndex++, packetClass, instance::encode, instance::decode, instance::handle);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the supplied Packet from a client to the server
     *
     * @param packet to send
     */
    public void sendPacketToServer(IPacket packet) {
        checkClass(packet.getClass());
        packetChannel.sendToServer(packet);
    }

    /**
     * Sends the supplied Packet from the server to the chosen Player
     *
     * @param packet to send
     * @param player to send to
     */
    public void sendPacketToPlayer(IPacket packet, ServerPlayerEntity player) {
        checkClass(packet.getClass());
        packetChannel.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /**
     * Sends a packet from the server to all currently connected players
     *
     * @param packet to send
     */
    public void sendPacketToAllPlayers(IPacket packet) {
        checkClass(packet.getClass());
        packetChannel.send(PacketDistributor.ALL.noArg(), packet);
    }

    /**
     * Sends a packet from the server to all players in a dimension around a location
     *
     * @param packet to send
     * @param tp     targetpoint instance to pass, cannot be null
     */
    public void sendPacketToAllAroundPoint(IPacket packet, PacketDistributor.TargetPoint tp) {
        checkClass(packet.getClass());
        packetChannel.send(PacketDistributor.NEAR.with(() -> tp), packet);
    }

    /**
     * Since the crash that happens if we dont do this is complete garbage
     */
    private void checkClass(Class<? extends IPacket> clazz) {
        if (!registeredClasses.contains(clazz)) {
            throw new RuntimeException("NetworkHelper got unknown Packet type " + clazz + " to send, critical error");
        }
    }

    /**
     * Packets only need to implement this and offer a constructor with no args,
     * unless you don't have constructors with >0 args. The class MUST also be
     * statically accessible, else you will suffer an InstantiationException!
     * Note Packets don't distinguish between being sent from client to server or
     * the other way around, so be careful using them bidirectional or avoid
     * doing that altogether.
     */
    public interface IPacket {

        void encode(Object msg, PacketBuffer packetBuffer);

        <MSG> MSG decode(PacketBuffer packetBuffer);

        void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier);
    }

}
