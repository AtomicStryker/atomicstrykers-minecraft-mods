package atomicstryker.findercompass.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkPosition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import atomicstryker.findercompass.client.FinderCompassLogic;
import atomicstryker.findercompass.common.FinderCompassMod;
import atomicstryker.findercompass.common.network.NetworkHelper.IPacket;

public class StrongholdPacket implements IPacket
{
    
    public StrongholdPacket() {}
    
    private int x,y,z;
    private String username;
    
    public StrongholdPacket(int a, int b, int c)
    {
        x = a;
        y = b;
        z = c;
    }
    
    public StrongholdPacket(String s)
    {
        username = s;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) // server answering coords query
        {
            // write coords
            bytes.writeInt(x);
            bytes.writeInt(y);
            bytes.writeInt(z);
        }
        else // client requesting strongold coords
        {
            //encode username into outgoing bytestream
            bytes.writeShort(username.length());
            for (char c : username.toCharArray()) bytes.writeChar(c);
        }
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) // client received stronghold answer
        {
            // retrieve coords
            x = bytes.readInt();
            y = bytes.readInt();
            z = bytes.readInt();
            FinderCompassLogic.strongholdCoords = new ChunkCoordinates(x, y, z);
            //System.out.printf("Finder Compass server sent Stronghold coords: [%d|%d|%d]\n", x, y, z);
            FinderCompassLogic.hasStronghold = true;
        }
        else // server received stronghold request
        {
            // retrieve username from incoming bytestream
            short len = bytes.readShort();
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) chars[i] = bytes.readChar();
            username = String.valueOf(chars);
            
            EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
            if (p != null)
            {
                ChunkPosition result = p.worldObj.findClosestStructure("stronghold", (int) p.posX, (int) p.posY, (int) p.posZ);
                if (result != null)
                {
                    FinderCompassMod.instance.networkHelper.sendPacketToPlayer(new StrongholdPacket(result.chunkPosY, result.chunkPosZ, result.chunkPosX), p);
                }
            }
        }
    }

}
