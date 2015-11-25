package atomicstryker.findercompass.common.network;

import atomicstryker.findercompass.client.FinderCompassLogic;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class StrongholdPacket implements IPacket
{

    public StrongholdPacket() {}
    
    private int x,y,z;
    private String username;
    
    /**
     * Server responding with stronghold location
     * @param a x coord
     * @param b y coord
     * @param c z coord
     */
    public StrongholdPacket(int a, int b, int c)
    {
        x = a;
        y = b;
        z = c;
        username = "";
    }
    
    /**
     * User requesting stronghold location
     * @param s username
     */
    public StrongholdPacket(String s)
    {
        x = y = z = 0;
        username = s;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        // write coords
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
        //encode username into outgoing bytestream
        bytes.writeShort(username.length());
        for (char c : username.toCharArray()) bytes.writeChar(c);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        // retrieve coords
        x = bytes.readInt();
        y = bytes.readInt();
        z = bytes.readInt();
        // retrieve username from incoming bytestream
        short len = bytes.readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
        username = String.valueOf(chars);
        
        if (username.equals("")) // client received stronghold answer
        {
            FinderCompassLogic.strongholdCoords = new BlockPos(x, y, z);
            //System.out.printf("Finder Compass server sent Stronghold coords: [%d|%d|%d]\n", x, y, z);
            FinderCompassLogic.hasStronghold = true;
        }
        else // server received stronghold request
        {
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
            if (p != null)
            {
                BlockPos result = p.worldObj.getStrongholdPos("Stronghold", new BlockPos(p));
                if (result != null)
                {
                    FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new StrongholdPacket(result.getX(), result.getY(), result.getZ()), p);
                }
            }
        }
    }

}
