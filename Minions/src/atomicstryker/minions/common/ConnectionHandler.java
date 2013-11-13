package atomicstryker.minions.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import atomicstryker.ForgePacketWrapper;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler
{

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
	{
        Object[] toSend = {MinionsCore.instance.evilDeedXPCost};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.REQUESTXPSETTING.ordinal(), toSend), player);
        
        EntityPlayer p = (EntityPlayer) player;
        MinionsCore.instance.prepareMinionHolder(p.username);
        Object[] toSend2 = {MinionsCore.instance.hasPlayerMinions(p) ? 1 : 0, MinionsCore.instance.hasAllMinions(p) ? 1 : 0};
        PacketDispatcher.sendPacketToPlayer(ForgePacketWrapper.createPacket(MinionsCore.getPacketChannel(), PacketType.HASMINIONS.ordinal(), toSend2), player);
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
	{
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
	{
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
	{
	}

	@Override
	public void connectionClosed(INetworkManager manager)
	{
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{
	}
}
