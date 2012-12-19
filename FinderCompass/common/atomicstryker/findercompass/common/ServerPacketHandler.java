package atomicstryker.findercompass.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.ChunkPosition;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = ForgePacketWrapper.readPacketID(dataIn);

        if (packetType == 0)
        {
            PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("FindrCmps", 0, null), player);
        }
        else if (packetType == 1)
        {
            EntityPlayer p = (EntityPlayer) player;
            ChunkPosition result = p.worldObj.findClosestStructure("Stronghold", (int) p.posX, (int) p.posY, (int) p.posZ);
            if (result != null)
            {
                Object[] toSend = { result.x, result.y, result.z };
                PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket("FindrCmps", 1, toSend), player);
            }
        }
    }
}
