package atomicstryker.minefactoryreloaded.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactory;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        // NOOP
        // if server gets a packet ... he isnt supposed to
    }

}
