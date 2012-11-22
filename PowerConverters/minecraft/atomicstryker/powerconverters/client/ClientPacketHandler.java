package atomicstryker.powerconverters.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import atomicstryker.powerconverters.common.ForgePacketWrapper;
import atomicstryker.powerconverters.common.TileEntityLiquidGenerator;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler
{

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
        int packetID = ForgePacketWrapper.readPacketID(data);
        
        if (packetID == 1)
        {
            Class[] decodeAs = {Integer.class, Integer.class, Integer.class, Integer.class};
            Object[] packetReadout = ForgePacketWrapper.readPacketData(data, decodeAs);
            
            World world = FMLClientHandler.instance().getClient().theWorld;
            int x = (Integer)packetReadout[0];
            int y = (Integer)packetReadout[1];
            int z = (Integer)packetReadout[2];
            int liquid = (Integer)packetReadout[3];
            
            TileEntity te = world.getBlockTileEntity(x, y, z);
            if (te != null && te instanceof TileEntityLiquidGenerator)
            {
                TileEntityLiquidGenerator telg = (TileEntityLiquidGenerator) te;
                telg.setStoredLiquid(liquid);
            }
        }
    }

}
