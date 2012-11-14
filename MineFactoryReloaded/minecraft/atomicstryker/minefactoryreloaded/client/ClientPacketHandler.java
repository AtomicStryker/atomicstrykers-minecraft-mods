package atomicstryker.minefactoryreloaded.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.minefactoryreloaded.common.PacketWrapper;
import atomicstryker.minefactoryreloaded.common.tileentities.TileEntityFactory;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetType = PacketWrapper.readPacketID(data);
        
        if (packetType == 1) // server -> client; server propagating machine rotation; args X Y Z rotation
        {
            Class[] decodeAs = { Integer.class, Integer.class, Integer.class, Integer.class };
            Object[] packetReadout = PacketWrapper.readPacketData(data, decodeAs);
            
            TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity((Integer)packetReadout[0], (Integer)packetReadout[1], (Integer)packetReadout[2]);
            if (te instanceof TileEntityFactory)
            {
                TileEntityFactory tef = (TileEntityFactory) te;
                tef.rotateDirectlyTo((Integer)packetReadout[3]);
            }
        }
    }

}
